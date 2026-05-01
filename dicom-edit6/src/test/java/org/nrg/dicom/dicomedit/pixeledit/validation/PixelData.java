package org.nrg.dicom.dicomedit.pixeledit.validation;

import javax.validation.constraints.NotNull;

public class PixelData {
    private PixelValue[][][] pixel;

    public PixelData() {
        this.pixel = new PixelValue[0][0][0];
    }

    public PixelData( @NotNull PixelValue[][][] pixel) {
        this.pixel = pixel;
    }

    public int getNSlices() {
        return (pixel == null)?  0: pixel.length;
    }
    public int getNRows() {
        return (pixel[0] == null)? 0: pixel[0].length;
    }
    public int getNCols() {
        return (pixel[0][0] == null)? 0: pixel[0][0].length;
    }
    public PixelValue getValue(int x, int y, int z) {
        return pixel[z][y][x];
    }
    public boolean isColor() {
        return (pixel[0][0][0] == null)? false: pixel[0][0][0].isColor();
    }
}
