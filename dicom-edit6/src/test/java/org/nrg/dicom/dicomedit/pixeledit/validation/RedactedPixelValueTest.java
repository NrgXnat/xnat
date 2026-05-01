package org.nrg.dicom.dicomedit.pixeledit.validation;

public abstract class RedactedPixelValueTest implements PixelValueTest{
    private boolean isColor;
    public boolean testPixel( int x, int y, int z, PixelData sourcePixelData, PixelData generatedPixelData) {
        this.isColor = sourcePixelData.isColor();
        if( isRedactedRegion(x,y,z)) {
            return generatedPixelData.getValue(x,y,z).equals( redactedPixelValue(x,y,z));
        }
        return generatedPixelData.getValue(x,y,z).equals( sourcePixelData.getValue(x,y,z));
    }

    public abstract boolean isRedactedRegion(int x, int y, int z);

    protected PixelValue redactedPixelValue(int x, int y, int z) {
        return (isColor)? PixelValue.ZERO_COLOR_PIXEL: PixelValue.ZERO_GRAY_PIXEL;
    }
}
