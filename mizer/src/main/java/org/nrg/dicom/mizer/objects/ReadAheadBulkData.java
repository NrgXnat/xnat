package org.nrg.dicom.mizer.objects;

import org.dcm4che3.data.BulkData;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A reference to bulk data in a file whose stream reads ahead when opened.
 * <p>
 * dcm4che copies a value out of a {@link BulkData} reference 2 KB at a time, straight from a bare
 * {@code FileInputStream}, so writing an object streams its pixel data with a read call for every
 * 2 KB. Reading ahead turns that into one call per value, or one per
 * {@link DicomObjectFactory#BULK_DATA_BUFFER_SIZE} for values larger than that.
 */
public final class ReadAheadBulkData extends BulkData {
    /** Kept as a long: {@link BulkData#length()} truncates to int, which goes negative past 2 GiB. */
    private final long length;

    public ReadAheadBulkData(final String uri, final long offset, final long length, final boolean bigEndian) {
        super(uri, offset, length, bigEndian);
        this.length = length;
    }

    @Override
    public InputStream openStream() throws IOException {
        return new BufferedInputStream(super.openStream(), readAheadFor(length));
    }

    /**
     * The read-ahead for a value of the given length: the value's own size, capped at
     * {@link DicomObjectFactory#BULK_DATA_BUFFER_SIZE}. dcm4che opens a stream per value -- per
     * fragment, for encapsulated pixel data -- so a fixed megabyte would allocate a megabyte for
     * every 40 KB frame of a multi-frame object and discard it straight after.
     */
    static int readAheadFor(final long length) {
        return (int) Math.max(1, Math.min(length, DicomObjectFactory.BULK_DATA_BUFFER_SIZE));
    }
}
