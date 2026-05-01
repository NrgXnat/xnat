package org.nrg.dicom.dicomedit.pixeledit.validation.impl;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;
import ij.ImagePlus;
import org.nrg.dicom.dicomedit.pixeledit.validation.DataValidationException;
import org.nrg.dicom.dicomedit.pixeledit.validation.ImageProcessingException;
import org.nrg.dicom.dicomedit.pixeledit.validation.PixelData;
import org.nrg.dicom.dicomedit.pixeledit.validation.PixelDataValidator;
import org.nrg.dicom.dicomedit.pixeledit.validation.PixelValue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static ij.IJ.openImage;

public class BasePixelDataValidator extends PixelDataValidator {
    private static final Logger logger = LoggerFactory.getLogger(BasePixelDataValidator.class);

    public BasePixelDataValidator(String sourceFileString, String generatedFileString) throws DataValidationException, ImageProcessingException, IOException {
        super(sourceFileString, generatedFileString);
    }
    public BasePixelDataValidator(File sourceFile, File generatedFile) throws DataValidationException, ImageProcessingException {
        super(sourceFile, generatedFile);
    }

    protected PixelData readPixelData1(File file) {
        int nSlices = 2;
        int nCols = 3;
        int nRows = 4;

        PixelValue[][] slice1 =  new PixelValue[nRows][nCols];
        PixelValue[] nrow1 = new PixelValue[]{new PixelValue(1),new PixelValue(2),new PixelValue(3)};
        PixelValue[] nrow2 = new PixelValue[]{new PixelValue(1),new PixelValue(2),new PixelValue(3)};
        PixelValue[] nrow3 = new PixelValue[]{new PixelValue(1),new PixelValue(2),new PixelValue(3)};
        PixelValue[] nrow4 = new PixelValue[]{new PixelValue(1),new PixelValue(2),new PixelValue(3)};
        slice1[0] = nrow1;
        slice1[1] = nrow2;
        slice1[2] = nrow3;
        slice1[3] = nrow4;
        PixelValue[][][] data = new PixelValue[nSlices][nRows][nCols];
        data[0] = slice1;
        data[1] = slice1;
        return new PixelData(data);
    }

    protected PixelData readPixelData( File file) throws ImageProcessingException {
        ImagePlus imagePlus = null;
        try {
            logger.debug("Read pixeldata from " + file.getAbsolutePath());
            imagePlus = openImage( file.getAbsolutePath());
            return readPixelData(imagePlus);
        }
        finally {
            if( imagePlus != null) {
                imagePlus.close();
            }
        }
    }

    private boolean isColor( ImagePlus ip) throws ImageProcessingException {
        int[] grayTypes = {ImagePlus.GRAY8, ImagePlus.GRAY16, ImagePlus.GRAY32};
        int[] colorTypes = {ImagePlus.COLOR_256, ImagePlus.COLOR_RGB};
        if( Arrays.stream(grayTypes).filter(t-> ip.getType() == t).findFirst().isPresent()) {
            return false;
        } else if (Arrays.stream(colorTypes).filter(t-> ip.getType() == t).findFirst().isPresent()) {
            return true;
        } else {
            throw new ImageProcessingException("Unknown color type for file: ${generatedImageFile.name}");
        }
    }

    private PixelData readPixelData(ImagePlus image) throws ImageProcessingException {
        final int nSlices = image.getNSlices();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final boolean isColor = isColor( image);
        PixelValue[][][] pixelValues = new PixelValue[nSlices][height][width];
        logger.debug(String.format("Creating pixel data image with %d slice(s) and 2-D resolution %d,%d (x,y)...",nSlices,width,height));

        for( int z = 0; z < nSlices; z++) {
            image.setSliceWithoutUpdate(z + 1);
            for( int y = 0; y < height; y++) {
                for( int x = 0; x < width; x++) {
                    pixelValues[z][y][x] = readPixelValue(x,y,z,isColor,image);
                }
            }
        }
        return new PixelData( pixelValues);
    }

    private PixelValue readPixelValue( int x, int y, int z, boolean isColor, ImagePlus image) {
        int[] values = image.getPixel(x,y);
        return createPixelValue(values, isColor);
    }

    public PixelValue createPixelValue( int[] values, boolean isColor) {
        if( isColor) {
            int r = values[0];
            int g = values[1];
            int b = values[2];
            return ( r == 0 && g == 0 && b == 0)? PixelValue.ZERO_COLOR_PIXEL: new PixelValue(r,g,b);
        } else {
            int gray = values[0];
            return (gray == 0)? PixelValue.ZERO_GRAY_PIXEL: new PixelValue(gray);
        }
    }

}
