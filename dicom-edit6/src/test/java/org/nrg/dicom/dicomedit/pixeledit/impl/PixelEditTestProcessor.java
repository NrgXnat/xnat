package org.nrg.dicom.dicomedit.pixeledit.impl;

import org.nrg.dicom.dicomedit.pixels.impl.PixelmedPixelEditHandler;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

public class PixelEditTestProcessor {
    private final ClassLoader loader = PixelmedPixelEditHandler.class.getClassLoader();
    private final PixelmedPixelEditHandler dbpeh = new PixelmedPixelEditHandler();

    public void process( PixelEditTestParams data) throws Exception {
        File src = data.getResourceInputFile();
        File dst = data.getOutputFile();
        DicomObjectI dobj = DicomObjectFactory.newInstance( src);
        Rectangle2D rect = (Rectangle2D) data.shape;
        dbpeh.process(rect, data.color, dobj);
        if( ! dst.getParentFile().exists()) {
            Files.createDirectories( dst.getParentFile().toPath());
        }
        try (final FileOutputStream output = new FileOutputStream(dst)) {
            dobj.write(output);
        }
    }

}
