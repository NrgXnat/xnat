package org.nrg.dicom.dicomedit.pixeledit.validation;

@FunctionalInterface
public interface PixelValueTest {
    boolean testPixel( int x, int y, int z, PixelData sourcePixelData, PixelData generatedPixelData);
}
