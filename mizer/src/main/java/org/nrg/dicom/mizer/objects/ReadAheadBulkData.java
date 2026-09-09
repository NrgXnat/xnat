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
 * 2 KB. Reading ahead turns that into one call per {@link DicomObjectFactory#BULK_DATA_BUFFER_SIZE}.
 */
public final class ReadAheadBulkData extends BulkData {
    public ReadAheadBulkData(final String uri, final long offset, final long length, final boolean bigEndian) {
        super(uri, offset, length, bigEndian);
    }

    @Override
    public InputStream openStream() throws IOException {
        return new BufferedInputStream(super.openStream(), DicomObjectFactory.BULK_DATA_BUFFER_SIZE);
    }
}
