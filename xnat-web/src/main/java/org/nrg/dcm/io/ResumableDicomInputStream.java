package org.nrg.dcm.io;

import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ItemPointer;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.BulkDataDescriptor;
import org.dcm4che3.io.DicomInputStream;
import org.nrg.dicom.mizer.objects.BufferedBulkDataCreator;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * DicomInputStream that calls mark() before reading each header, so that if a stop predicate
 * stops the read, calling reset() makes it possible to resume reading.
 */
@Slf4j
public final class ResumableDicomInputStream extends DicomInputStream {
    // 12 should be enough, but more might be needed someday to not interfere with readSequence.
    final int markSize = 12;

    /**
     * Where bulk data read by {@link #openWithBulkDataOffHeap} is spooled. Sized for the pixel data, so not
     * always suited to java.io.tmpdir, which may be smaller than an image. Unset, the spool directory is made
     * under java.io.tmpdir.
     */
    public static final String SCRATCH_DIR_PROPERTY = "dicom.import.scratch.dir";

    /** @see #scratchDirectory() */
    private static File scratchDirectory;

    /** The {@link #SCRATCH_DIR_PROPERTY} value {@link #scratchDirectory} was made for, so a change re-resolves. */
    private static String scratchDirectoryFor;

    /**
     * Matches pixel data in any of its three forms, and nothing else.
     * <p>
     * dcm4che's own {@link BulkDataDescriptor#PIXELDATA} matches only (7FE0,0010), so float (7FE0,0008) and
     * double float (7FE0,0009) pixel data would still be read onto the heap and would still hit the 2 GiB
     * ceiling -- sooner, in the double float case, at eight bytes per sample. {@link BulkDataDescriptor#DEFAULT}
     * covers all three but also covers palette colour lookup tables and overlay data, which sit below
     * (0032,4000) and so fall inside even an ordinary read window; matching those would make every ordinary
     * import spool files it does not today.
     * <p>
     * Item pointers are not consulted, which matches {@code PIXELDATA}: pixel data nested in a sequence, an
     * icon image for instance, is referenced like any other.
     */
    static final BulkDataDescriptor PIXEL_DATA_OF_ANY_FORM = ResumableDicomInputStream::isPixelData;

    private static boolean isPixelData(final List<ItemPointer> itemPointers, final String privateCreator,
                                       final int tag, final VR vr, final int length) {
        return Tag.PixelData == tag || Tag.FloatPixelData == tag || Tag.DoubleFloatPixelData == tag;
    }

    public ResumableDicomInputStream(BufferedInputStream in) throws IOException {
        super(in);
    }

    public ResumableDicomInputStream(BufferedInputStream in, String transferSyntaxUid) throws IOException {
        super(in, transferSyntaxUid);
    }

    @Override
    public void readHeader(final Predicate<DicomInputStream> stopPredicate) throws IOException {
        mark(markSize);
        super.readHeader(stopPredicate);
    }

    /**
     * Opens a stream that keeps pixel data off the heap.
     * <p>
     * A caller that reads past (7FE0,0010) gets the pixel data in a {@code byte[]} under the default
     * {@link IncludeBulkData#YES}, which costs heap per concurrent read and fails outright above 2 GiB --
     * dcm4che throws "tag value too large, must be less than 2Gib". {@link IncludeBulkData#URI URI} stores a
     * reference instead. When the source is a file the reference points into it, but here it is a stream, so
     * the value is spooled to a file and the reference points there. The spooling is
     * {@link BufferedBulkDataCreator}'s rather than dcm4che's, which writes 2 KB at a time to an unbuffered
     * stream and hands back references that read the same way: a gigabyte of pixel data would cost a million
     * system calls each way. Here every value of an object goes into one spool file through a
     * {@link DicomObjectFactory#BULK_DATA_BUFFER_SIZE} buffer, and the references read ahead by the same
     * amount.
     * <p>
     * Nothing else owns the spool files and nothing else removes them: the caller must pass
     * {@link #getSpoolFiles()} to {@link #deleteBulkDataFiles} once everything holding a reference is done
     * with them. The spool is complete, flushed and closed, when the top-level dataset read returns.
     * <p>
     * The descriptor is restricted to the pixel data deliberately; see {@link #PIXEL_DATA_OF_ANY_FORM}.
     *
     * Where the spool lands is {@link #SCRATCH_DIR_PROPERTY configurable}.
     *
     * @param in the object's bytes.
     *
     * @return a resumable stream that references bulk data rather than loading it.
     *
     * @throws IOException if the stream cannot be opened. A configured scratch directory that does not exist
     *                     and cannot be created fails the read instead, at the first value that has to be
     *                     spooled.
     */
    public static ResumableDicomInputStream openWithBulkDataOffHeap(final BufferedInputStream in) throws IOException {
        return openWithBulkDataOffHeap(in, null);
    }

    /**
     * As {@link #openWithBulkDataOffHeap(BufferedInputStream)}, for a stream that reads
     * <b>sourceFile</b> from its first byte: bulk data is referenced straight into the file
     * instead of copied to the spool, except under a Deflated transfer syntax, where stream
     * positions are not file positions and the creator spools as it must. The references are
     * followed when the object is written, so the file must outlive that write; nothing here
     * alters or deletes it -- {@link #getSpoolFiles()} never includes it.
     *
     * @param in         the object's bytes.
     * @param sourceFile the file the stream reads from its beginning, or null when the source is
     *                   not a file.
     *
     * @return a resumable stream that references bulk data rather than loading it.
     *
     * @throws IOException if the stream cannot be opened. A configured scratch directory that does
     *                     not exist and cannot be created fails the read instead, at the first value
     *                     that has to be spooled.
     */
    public static ResumableDicomInputStream openWithBulkDataOffHeap(final BufferedInputStream in, final File sourceFile) throws IOException {
        final ResumableDicomInputStream dis = new ResumableDicomInputStream(in);
        dis.setIncludeBulkData(IncludeBulkData.URI);
        dis.setBulkDataDescriptor(PIXEL_DATA_OF_ANY_FORM);
        if (sourceFile != null) {
            dis.setURI(sourceFile.toURI().toString());
        }
        // A method reference, not a call: scratchDirectory() stats the directory under a
        // class-level lock, and the ordinary import stops short of the pixel data and spools
        // nothing, so resolving it per object would be a serialized syscall for nothing. A
        // configured directory that cannot be created still fails the read -- now when the first
        // value has to be spooled rather than when the stream opens.
        dis._creator = new BufferedBulkDataCreator(ResumableDicomInputStream::scratchDirectory);
        dis.setBulkDataCreator(dis._creator);
        return dis;
    }

    /**
     * The files bulk data was spooled to by a stream from {@link #openWithBulkDataOffHeap}: at most one, and
     * none when the read never reached any bulk data, which is the ordinary case.
     */
    public List<File> getSpoolFiles() {
        final List<File> files = _creator == null ? new ArrayList<>() : _creator.getSpoolFiles();
        files.addAll(getBulkDataFiles());
        return files;
    }

    /**
     * Once the top-level read is done, the spool is closed so that everything written to it can be read back
     * through the references. Nested reads, of sequence items, return here too, at a deeper level.
     */
    @Override
    public void readAttributes(final Attributes attrs, final long len, final Predicate<DicomInputStream> stopPredicate) throws IOException {
        super.readAttributes(attrs, len, stopPredicate);
        if (level() == 0) {
            closeSpool();
        }
    }

    @Override
    public void close() throws IOException {
        try {
            closeSpool();
        } finally {
            super.close();
        }
    }

    private void closeSpool() throws IOException {
        if (_creator != null) {
            _creator.close();
        }
    }

    /**
     * A directory for spool files that only this user can read, under {@link #SCRATCH_DIR_PROPERTY} when it
     * is set.
     * <p>
     * Those files hold pixel data. They are created with owner-only permissions, and they go somewhere nobody
     * else can list or open either: createTempDirectory gives owner-only permissions and an unguessable name,
     * which also rules out anyone planting a directory at a predictable path first. One per JVM, since it
     * holds nothing once the files are released.
     * <p>
     * A configured directory that cannot be created is an error rather than a fall back to the default: the
     * reason for setting it may be that the pixel data must not go there.
     *
     * @return the directory to spool into.
     *
     * @throws IOException if the directory cannot be created.
     */
    private static synchronized File scratchDirectory() throws IOException {
        final String configured = System.getProperty(SCRATCH_DIR_PROPERTY);
        if (null != scratchDirectory && scratchDirectory.isDirectory()
            && Objects.equals(configured, scratchDirectoryFor)) {
            return scratchDirectory;
        }
        try {
            if (null == configured) {
                scratchDirectory = Files.createTempDirectory("xnat-import-bulkdata").toFile();
            } else {
                final Path parent = Paths.get(configured);
                // createDirectories rather than mkdirs: it succeeds when the directory is already there, so
                // two imports creating it at the same moment cannot race one of them into a failure.
                Files.createDirectories(parent);
                scratchDirectory = Files.createTempDirectory(parent, "xnat-import-bulkdata").toFile();
            }
        } catch (IOException e) {
            throw new IOException("DICOM import scratch directory"
                                  + (null == configured ? "" : " " + configured + ", from " + SCRATCH_DIR_PROPERTY)
                                  + " does not exist and cannot be created", e);
        }
        scratchDirectory.deleteOnExit();
        scratchDirectoryFor = configured;
        return scratchDirectory;
    }

    /**
     * Deletes the files bulk data was spooled to.
     *
     * @param bulkDataFiles the spool files, as reported by {@link #getSpoolFiles()}. Empty when the read
     *                      never reached any bulk data, which is the ordinary case.
     */
    public static void deleteBulkDataFiles(final List<File> bulkDataFiles) {
        for (final File bulkDataFile : bulkDataFiles) {
            if (bulkDataFile.exists() && !bulkDataFile.delete()) {
                log.warn("Unable to delete bulk data spool file {}", bulkDataFile);
            }
        }
    }

    private BufferedBulkDataCreator _creator;
}
