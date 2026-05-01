package org.nrg.dicom.dicomedit.pixeledit.impl;

import java.awt.Color;
import java.awt.Shape;
import java.io.File;

public class PixelEditTestParams {
    String inputFileName;
    String outputFileName;
    Shape shape;
    Color color;

    public PixelEditTestParams(String i, String o, Shape s, Color c) {
        inputFileName = i;
        outputFileName = o;
        shape = s;
        color = c;
    }

    public File getResourceInputFile() throws Exception {
        ClassLoader loader = PixelEditTestParams.class.getClassLoader();
        return new File(loader.getResource(inputFileName).toURI());
    }

    public File getOutputFile() {
        return new File( outputFileName);
    }

}
