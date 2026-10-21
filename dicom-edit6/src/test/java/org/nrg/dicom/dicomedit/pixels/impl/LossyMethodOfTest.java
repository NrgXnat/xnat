package org.nrg.dicom.dicomedit.pixels.impl;

import org.dcm4che3.data.UID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Pins the Lossy Image Compression Method defined terms (PS3.3 C.7.6.1.1.5.1) for each lossy syntax,
 * including the ones TransferSyntaxType lumps together: HTJ2K with JPEG 2000, and every video
 * syntax under MPEG.
 */
public class LossyMethodOfTest {
    @Test
    public void mapsEachLossySyntaxToItsDefinedTerm() {
        assertEquals("ISO_10918_1", EncapsulatedPixelRedactor.lossyMethodOf(UID.JPEGBaseline8Bit));
        assertEquals("ISO_10918_1", EncapsulatedPixelRedactor.lossyMethodOf(UID.JPEGExtended12Bit));
        assertEquals("ISO_10918_1", EncapsulatedPixelRedactor.lossyMethodOf(UID.JPEGXLJPEGRecompression));
        assertEquals("ISO_14495_1", EncapsulatedPixelRedactor.lossyMethodOf(UID.JPEGLSNearLossless));
        assertEquals("ISO_15444_1", EncapsulatedPixelRedactor.lossyMethodOf(UID.JPEG2000));
        assertEquals("ISO_15444_1", EncapsulatedPixelRedactor.lossyMethodOf(UID.JPEG2000MC));
        assertEquals("ISO_15444_15", EncapsulatedPixelRedactor.lossyMethodOf(UID.HTJ2K));
        assertEquals("ISO_18181_1", EncapsulatedPixelRedactor.lossyMethodOf(UID.JPEGXL));
        assertEquals("ISO_13818_2", EncapsulatedPixelRedactor.lossyMethodOf(UID.MPEG2MPHLF));
        assertEquals("ISO_14496_10", EncapsulatedPixelRedactor.lossyMethodOf(UID.MPEG4HP41));
        assertEquals("ISO_14496_10", EncapsulatedPixelRedactor.lossyMethodOf(UID.MPEG4HP42STEREOF));
        assertEquals("ISO_23008_2", EncapsulatedPixelRedactor.lossyMethodOf(UID.HEVCM10P51));
    }
}
