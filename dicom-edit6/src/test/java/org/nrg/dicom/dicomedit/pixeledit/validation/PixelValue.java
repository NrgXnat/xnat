package org.nrg.dicom.dicomedit.pixeledit.validation;

import java.util.Objects;

public class PixelValue {
    private int red, green, blue, gray;
    private boolean isColor;
    public final static PixelValue ZERO_COLOR_PIXEL = new PixelValue(0,0,0);
    public final static PixelValue ZERO_GRAY_PIXEL = new PixelValue(0);
    public PixelValue(int red, int green, int blue) {
        this.red = red;
        this.blue = blue;
        this.green = green;
        isColor = true;
        this.gray = 0;
    }

    public PixelValue(int gray) {
        this.red = 0;
        this.blue = 0;
        this.green = 0;
        isColor = false;
        this.gray = gray;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PixelValue that = (PixelValue) o;
        if( isColor() != that.isColor()) return false;
        if( isColor()) {
            return red == that.red && green == that.green && blue == that.blue;
        }
        else {
            return gray == that.gray;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(red, green, blue, gray, isColor());
    }

    public boolean isColor() {
        return isColor;
    }
}
