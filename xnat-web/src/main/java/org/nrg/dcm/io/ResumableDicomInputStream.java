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
import java.util.List;
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
     * always suited to java.io.tmpdir, which may be smaller than an image and need not carry the archive's
     * access controls. Unset leaves dcm4che at its default, which is java.io.tmpdir.
     */
    public static final String SCRATCH_DIR_PROPERTY = "dicom.import.scratch.dir";

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
        final File scratchDirectory = getScratchDirectory();
        if (null != scratchDirectory) {
            dis.setBulkDataDirectory(scratchDirectory);
        }
        return dis;
    }

    /**
     * The configured spool directory, created if it does not exist.
     *
     * @return the directory, or null to leave dcm4che at its default.
     *
     * @throws IOException if the configured directory does not exist and cannot be created. Failing rather
     *                     than falling back is deliberate: silently spooling to java.io.tmpdir would defeat
     *                     the reason for configuring it, which may be that the pixel data must not go there.
     */
    private static File getScratchDirectory() throws IOException {
        final String configured = System.getProperty(SCRATCH_DIR_PROPERTY);
        if (null == configured) {
            return null;
        }
        final File directory = new File(configured);
        if (!directory.isDirectory() && !directory.mkdirs()) {
            throw new IOException("DICOM import scratch directory " + directory
                                  + " does not exist and cannot be created");
        }
        return directory;
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
