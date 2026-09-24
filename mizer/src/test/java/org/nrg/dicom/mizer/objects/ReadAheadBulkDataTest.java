package org.nrg.dicom.mizer.objects;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * The read-ahead buffer is sized to the value: dcm4che opens one stream per fragment, so a fixed
 * megabyte per open would allocate far more than a multi-frame object's pixel data to write it.
 */
public class ReadAheadBulkDataTest {

    @Test
    public void readsAheadByTheValueSizeForSmallValues() {
        assertEquals(40_000, ReadAheadBulkData.readAheadFor(40_000));
    }

    @Test
    public void capsTheReadAheadAtTheBulkDataBuffer() {
        assertEquals(DicomObjectFactory.BULK_DATA_BUFFER_SIZE, ReadAheadBulkData.readAheadFor(3L << 30));
    }

    @Test
    public void neverAsksForAnEmptyBuffer() {
        assertEquals(1, ReadAheadBulkData.readAheadFor(0));
    }
}
