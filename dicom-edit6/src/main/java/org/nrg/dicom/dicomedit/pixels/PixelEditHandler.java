package org.nrg.dicom.dicomedit.pixels;

import org.nrg.dicom.mizer.objects.DicomObjectI;

/**
 * Interface for the Pixel Editing Service.
 *
 */
public interface PixelEditHandler {

    boolean handles(String shape,
                    String shapeProperties,
                    String algorithm,
                    String algorithmProperties,
                    DicomObjectI dicomObject);

    void apply(String shape,
               String shapeProperties,
               String algorithm,
               String algorithmProperties,
               DicomObjectI dicomObject) throws Exception;
}
