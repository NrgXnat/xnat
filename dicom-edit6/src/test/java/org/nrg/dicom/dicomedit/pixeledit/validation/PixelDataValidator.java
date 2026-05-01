package org.nrg.dicom.dicomedit.pixeledit.validation;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class PixelDataValidator {
    private File sourceFile;
    private File generatedFile;
    private PixelData sourcePixelData;
    private PixelData generatedPixelData;
    private int nSlices, nCols, nRows;

    public PixelDataValidator( String srcFileString, String genFileString) throws ImageProcessingException, DataValidationException, IOException {
        File srcFile = stringToFile( srcFileString);
        File genFile = stringToFile( genFileString);
        init( srcFile, genFile);
    }

    public PixelDataValidator(File sourceFile, File generatedFile) throws DataValidationException, ImageProcessingException {
        init( sourceFile, generatedFile);
    }

    private void init( File sourceFile, File generatedFile) throws ImageProcessingException, DataValidationException {
        this.sourceFile = sourceFile;
        this.generatedFile = generatedFile;
        this.sourcePixelData = readPixelData( sourceFile);
        this.generatedPixelData = readPixelData( generatedFile);
        this.nSlices = sourcePixelData.getNSlices();
        this.nRows = sourcePixelData.getNRows();
        this.nCols = sourcePixelData.getNCols();
        validateDimensions();
    }

    public void testPixelData(PixelValueTest pixelValueTest) throws DataValidationException {
        for( int z = 0; z < nSlices; z++) {
            for( int x = 0; x < nCols; x++) {
                for( int y = 0; y < nRows; y++) {
                    if (!pixelValueTest.testPixel(x, y, z, sourcePixelData, generatedPixelData)) {
                        throw new DataValidationException(String.format("Failed pixel test at (nslices,nrows,ncols) (%d,%d,%d)",z,y,x));
                    }
                }
            }
        }
    }

    protected abstract PixelData readPixelData( File file) throws ImageProcessingException;

    private void validateDimensions() throws DataValidationException {
        int gSlices = generatedPixelData.getNSlices();
        int gRows = generatedPixelData.getNRows();
        int gCols = generatedPixelData.getNCols();
        if( nSlices != gSlices || nCols != gCols || nRows != gRows) {
            String msg = String.format("Source-image dimensions (nslices, nrows, ncols) (%d,%d,%d) does not match generated-image dimensions (%d,%d,%d).",nSlices,nRows,nCols,gSlices,gRows,gCols);
            throw new DataValidationException(msg);
        }
    }

    private File stringToFile( String filePathString) throws IOException {
        File file;
        Path srcPath = Paths.get(filePathString);
        if( srcPath.isAbsolute()) {
            file = srcPath.toFile();
        } else {
            final ClassLoader loader = this.getClass().getClassLoader();
            try {
                file = new File(loader.getResource(filePathString).toURI());
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }
        return file;
    }
}
