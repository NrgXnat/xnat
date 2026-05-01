package org.nrg.dicom.dicomedit.pixeledit.impl;

import org.junit.Test;
import org.nrg.dicom.dicomedit.pixeledit.validation.PixelDataValidator;
import org.nrg.dicom.dicomedit.pixeledit.validation.RedactedPixelValueTest;
import org.nrg.dicom.dicomedit.pixeledit.validation.impl.BasePixelDataValidator;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

public class RegionWrapTest {

    @Test
    public void regionInBounds() {
        try {
            String src = "dicom/flat-field/grey.dcm";
            String dst = "/tmp/flat-field/grey100x100.dcm";
            PixelEditTestParams params = new PixelEditTestParams(src, dst, new Rectangle2D.Float(20, 20, 100, 100), new Color(128, 128, 128));
            PixelEditTestProcessor processor = new PixelEditTestProcessor();

            processor.process( params);
            PixelDataValidator validator = new BasePixelDataValidator( src, dst);
            validator.testPixelData(new RedactedPixelValueTest() {
                @Override
                public boolean isRedactedRegion(int x, int y, int z) {
                    return  x >= 20 && x < 120 && y >= 20 && y < 120 ;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e);
        }
    }

    @Test
    public void regionInAndOutOfBounds() {
        try {
            String src = "dicom/flat-field/grey.dcm";
            String dst = "/tmp/flat-field/grey250x250.dcm";
            PixelEditTestProcessor processor = new PixelEditTestProcessor();
            PixelEditTestParams params = new PixelEditTestParams(src, dst, new Rectangle2D.Float(20, 20, 250, 250), new Color(128,128,128));

            processor.process( params);
            PixelDataValidator validator = new BasePixelDataValidator( src, dst);
            validator.testPixelData(new RedactedPixelValueTest() {
                @Override
                public boolean isRedactedRegion(int x, int y, int z) {
                    return  x >= 20 && x < 256 && y >= 20 && y < 256 ;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e);
        }
    }

    @Test
    // Silently does nothing to image.
    public void regionOutOfBounds() {
        try {
            String src = "dicom/flat-field/grey.dcm";
            String dst = "/tmp/flat-field/grey_oob.dcm";
            PixelEditTestProcessor processor = new PixelEditTestProcessor();
            PixelEditTestParams params = new PixelEditTestParams(src, dst, new Rectangle2D.Float(300, 20, 250, 80), new Color(128,128,128));

            processor.process( params);
            PixelDataValidator validator = new BasePixelDataValidator( src, dst);
            validator.testPixelData(new RedactedPixelValueTest() {
                @Override
                public boolean isRedactedRegion(int x, int y, int z) {
                    return  x >= 300 && x < 550 && y >= 20 && y < 100 ;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e);
        }
    }

}