package org.nrg.dcm.io;

import lombok.extern.slf4j.Slf4j;
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
     * The descriptor is restricted to the pixel data deliberately. Under the default descriptor every standard
     * bulk data element becomes a reference, and some of those -- palette colour lookup tables, overlay data --
     * sit low enough to fall inside an ordinary read window, so an ordinary read would start spooling files it
     * does not today. The pixel data is the only value large enough to be worth keeping off the heap.
     *
     * @param in the object's bytes.
     *
     * @return a resumable stream that references bulk data rather than loading it.
     *
     * @throws IOException if the stream cannot be opened.
     */
    public static ResumableDicomInputStream openWithBulkDataOffHeap(final BufferedInputStream in) throws IOException {
        final ResumableDicomInputStream dis = new ResumableDicomInputStream(in);
        dis.setIncludeBulkData(IncludeBulkData.URI);
        dis.setBulkDataDescriptor(BulkDataDescriptor.PIXELDATA);
        return dis;
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
