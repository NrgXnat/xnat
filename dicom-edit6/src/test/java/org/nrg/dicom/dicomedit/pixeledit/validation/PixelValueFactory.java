package org.nrg.dicom.dicomedit.pixeledit.validation;

public class PixelValueFactory {

    public PixelValue createPixelValue( int[] values, boolean isColor) {
        if( isColor) {
            int r = values[0];
            int b = values[1];
            int g = values[2];
            return ( r == 0 && b == 0 && g == 0)? PixelValue.ZERO_COLOR_PIXEL: new PixelValue(r,g,b);
        } else {
            int gray = values[0];
            return (gray == 0)? PixelValue.ZERO_GRAY_PIXEL: new PixelValue(gray);
        }
    }
}
