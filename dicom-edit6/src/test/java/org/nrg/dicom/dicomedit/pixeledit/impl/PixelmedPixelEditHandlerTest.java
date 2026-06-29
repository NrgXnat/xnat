package org.nrg.dicom.dicomedit.pixeledit.impl;

import org.junit.Test;
import org.junit.Ignore;
import org.nrg.dicom.dicomedit.pixeledit.validation.PixelDataValidator;
import org.nrg.dicom.dicomedit.pixeledit.validation.RedactedPixelValueTest;
import org.nrg.dicom.dicomedit.pixeledit.validation.impl.BasePixelDataValidator;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import static org.junit.Assert.fail;


public class PixelmedPixelEditHandlerTest {

    @Test
    public void singleframe_ivle_mono2_12bits() {
        try {
            String src = "dicom/single-frame/CT-ivle-mono2-12bits.dcm";
            String dst = "/tmp/decompressed/single-frame/CT-ivle-mono2-12bits.dcm";
            PixelEditTestParams params = new PixelEditTestParams(src, dst, new Rectangle2D.Float(200, 200, 100, 100), new Color(128, 128, 128));

            PixelEditTestProcessor processor = new PixelEditTestProcessor();

            processor.process( params);
            PixelDataValidator validator = new BasePixelDataValidator( src, dst);
            validator.testPixelData(new RedactedPixelValueTest() {
                @Override
                public boolean isRedactedRegion(int x, int y, int z) {
                    return  x >= 200 && x < 300 && y >= 200 && y < 300 ;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e);
        }
    }

    @Test
    public void singleframe_evbe_rgb_8bits() {
        try {
            String src = "dicom/single-frame/US-evbe-rgb-8bits.dcm";
            String dst = "/tmp/decompressed/single-frame/US-evbe-rgb-8bits.dcm";
            PixelEditTestParams params = new PixelEditTestParams(src, dst, new Rectangle2D.Float(200, 200, 100, 100), new Color(128, 128, 128));

            PixelEditTestProcessor processor = new PixelEditTestProcessor();

            processor.process( params);
            PixelDataValidator validator = new BasePixelDataValidator( src, dst);
            validator.testPixelData(new RedactedPixelValueTest() {
                @Override
                public boolean isRedactedRegion(int x, int y, int z) {
                    return  x >= 200 && x < 300 && y >= 200 && y < 300 ;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e);
        }
    }

    @Test
    public void singleframe_evle_mono2_8bits() {
        try {
            String src = "dicom/single-frame/US-evle-mono2-8bits.dcm";
            String dst = "/tmp/decompressed/single-frame/US-evle-mono2-8bits.dcm";
            PixelEditTestParams params = new PixelEditTestParams(src, dst, new Rectangle2D.Float(700, 400, 200, 200), new Color(128, 128, 128));

            PixelEditTestProcessor processor = new PixelEditTestProcessor();

            processor.process( params);
            PixelDataValidator validator = new BasePixelDataValidator( src, dst);
            validator.testPixelData(new RedactedPixelValueTest() {
                @Override
                public boolean isRedactedRegion(int x, int y, int z) {
                    return  x >= 700 && x < 900 && y >= 400 && y < 600 ;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e);
        }
    }

    @Test
    public void singleframe_evle_rgb_8bits() {
        try {
            String src = "dicom/single-frame/US-evle-rgb-8bits.dcm";
            String dst = "/tmp/decompressed/single-frame/US-evle-rgb-8bits.dcm";
            PixelEditTestParams params = new PixelEditTestParams(src, dst, new Rectangle2D.Float(100, 10, 50, 50), new Color(128, 128, 128));

            PixelEditTestProcessor processor = new PixelEditTestProcessor();

            processor.process( params);
            PixelDataValidator validator = new BasePixelDataValidator( src, dst);
            validator.testPixelData(new RedactedPixelValueTest() {
                @Override
                public boolean isRedactedRegion(int x, int y, int z) {
                    return  x >= 100 && x < 150 && y >= 10 && y < 60 ;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e);
        }
    }

    @Test
    @Ignore
        // ImageJ can not open the compressed source image to do validation.
        // Manually viewing the processed image shows success.
    public void multiframe_rle_8bit() {
        try {
            String src = "dicom/multi-frame/us-rle-8bit.dcm";
            String dst = "/tmp/decompressed/multi-frame/us-rle-8bit.dcm";
            PixelEditTestParams params = new PixelEditTestParams(src, dst, new Rectangle2D.Float(50, 50, 400, 200), new Color(128, 128, 128));

            PixelEditTestProcessor processor = new PixelEditTestProcessor();

            processor.process( params);
            PixelDataValidator validator = new BasePixelDataValidator( src, dst);
            validator.testPixelData(new RedactedPixelValueTest() {
                @Override
                public boolean isRedactedRegion(int x, int y, int z) {
                    return  x >= 50 && x < 450 && y >= 50 && y < 250 ;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e);
        }
    }

    @Test
    @Ignore
        // ImageJ can not open the compressed source image to do validation.
        // Manually viewing the processed image shows success.
    public void multiframe_rle_pal_8bit() {
        try {
            String src = "dicom/multi-frame/US-rle-pal-8bits.dcm";
            String dst = "/tmp/decompressed/multi-frame/US-rle-pal-8bits.dcm";
            PixelEditTestParams params = new PixelEditTestParams(src, dst, new Rectangle2D.Float(200, 200, 100, 100), new Color(128, 128, 128));

            PixelEditTestProcessor processor = new PixelEditTestProcessor();

            processor.process( params);
            PixelDataValidator validator = new BasePixelDataValidator( src, dst);
            validator.testPixelData(new RedactedPixelValueTest() {
                @Override
                public boolean isRedactedRegion(int x, int y, int z) {
                    return  x >= 200 && x < 300 && y >= 200 && y < 300 ;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e);
        }
    }

    @Test
    @Ignore
        // ImageJ can not open the compressed source image to do validation.
        // Manually viewing the processed image shows success.
    public void multiframe_jpeg1() {
        try {
            String src = "dicom/multi-frame/xa-jpeg1.dcm";
            String dst = "/tmp/decompressed/multi-frame/xa-jpeg1.dcm";
            PixelEditTestParams params = new PixelEditTestParams(src, dst, new Rectangle2D.Float(200, 200, 100, 100), new Color(128, 128, 128));

            PixelEditTestProcessor processor = new PixelEditTestProcessor();

            processor.process( params);
            PixelDataValidator validator = new BasePixelDataValidator( src, dst);
            validator.testPixelData(new RedactedPixelValueTest() {
                @Override
                public boolean isRedactedRegion(int x, int y, int z) {
                    return  x >= 200 && x < 300 && y >= 200 && y < 300 ;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e);
        }
    }

    @Test
    @Ignore("Requires us-evle-rgb-8bit.dcm (453 MB). See method body for download URL.")
    public void multiframe_evle_rgb_8bit() {
        // To run this test, download the zip from s3://xnatworks-sample-data/multi-frame-dicom.zip
        // extract us-evle-rgb-8bit.dcm from the zip
        // and place it at dicom-edit6/src/test/resources/dicom/multi-frame/
        try {
            String src = "dicom/multi-frame/us-evle-rgb-8bit.dcm";
            String dst = "/tmp/decompressed/multi-frame/us-evle-rgb-8bit.dcm";
            PixelEditTestParams params = new PixelEditTestParams(src, dst, new Rectangle2D.Float(200, 200, 100, 100), new Color(128, 128, 128));

            PixelEditTestProcessor processor = new PixelEditTestProcessor();

            processor.process( params);
            PixelDataValidator validator = new BasePixelDataValidator( src, dst);
            validator.testPixelData(new RedactedPixelValueTest() {
                @Override
                public boolean isRedactedRegion(int x, int y, int z) {
                    return  x >= 200 && x < 300 && y >= 200 && y < 300 ;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e);
        }
    }

}