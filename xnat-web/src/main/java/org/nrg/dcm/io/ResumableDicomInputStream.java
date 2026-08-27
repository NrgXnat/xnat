package org.nrg.dcm.io;

import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.ItemPointer;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.BulkDataDescriptor;
import org.dcm4che3.io.DicomInputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
     * dcm4che spools the value to a temporary file of its own. Nothing else owns those files and dcm4che does
     * not remove them: the caller must pass {@link #getBulkDataFiles()} to {@link #deleteBulkDataFiles} once
     * everything holding a reference is done with it.
     * <p>
     * The descriptor is restricted to the pixel data deliberately; see {@link #PIXEL_DATA_OF_ANY_FORM}.
     *
     * Where they land is {@link #SCRATCH_DIR_PROPERTY configurable}.
     *
     * @param in the object's bytes.
     *
     * @return a resumable stream that references bulk data rather than loading it.
     *
     * @throws IOException if the stream cannot be opened, or the configured scratch directory does not exist
     *                     and cannot be created.
     */
    public static ResumableDicomInputStream openWithBulkDataOffHeap(final BufferedInputStream in) throws IOException {
        final ResumableDicomInputStream dis = new ResumableDicomInputStream(in);
        dis.setIncludeBulkData(IncludeBulkData.URI);
        dis.setBulkDataDescriptor(PIXEL_DATA_OF_ANY_FORM);
        dis.setBulkDataDirectory(scratchDirectory());
        return dis;
    }

    /**
     * A directory for dcm4che's spool files that only this user can read, under
     * {@link #SCRATCH_DIR_PROPERTY} when it is set.
     * <p>
     * Those files hold pixel data, and dcm4che creates them through the legacy {@code File.createTempFile},
     * which takes its mode from the umask and typically leaves them rw-r--r-- where {@code Files.createTempFile}
     * would give rw-------. Their own mode is not ours to set, so they go somewhere nobody else can list or
     * open: createTempDirectory gives owner-only permissions and an unguessable name, which also rules out
     * anyone planting a directory at a predictable path first. One per JVM, since it holds nothing once the
     * files are released.
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
     * Deletes the files dcm4che spooled bulk data to.
     *
     * @param bulkDataFiles the spool files, as reported by {@link #getBulkDataFiles()}. Empty when the read
     *                      never reached any bulk data, which is the ordinary case.
     */
    public static void deleteBulkDataFiles(final List<File> bulkDataFiles) {
        for (final File bulkDataFile : bulkDataFiles) {
            if (bulkDataFile.exists() && !bulkDataFile.delete()) {
                log.warn("Unable to delete bulk data spool file {}", bulkDataFile);
            }
        }
    }
}
