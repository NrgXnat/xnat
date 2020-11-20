package org.nrg.xnat.snapshot;

import org.junit.Test;
import org.nrg.xnat.snapshot.generator.impl.MontageGenerator;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.fail;

public class MontageGeneratorTests {

    @Test
    public void test10_1x1() {
        try {
            List<String> files = Arrays.asList(
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.90.20061214.091206.156000.8960719755.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.95.20061214.091206.156000.9278819775.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.91.20061214.091206.156000.2012419759.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.96.20061214.091206.156000.3558719779.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.92.20061214.091206.156000.2982419763.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.97.20061214.091206.156000.5264219783.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.93.20061214.091206.156000.7442419769.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.98.20061214.091206.156000.5286319787.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.94.20061214.091206.156000.1465319771.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.99.20061214.091206.156000.9673919791.dcm");
            MontageGenerator generator = new MontageGenerator();
            File dst = new File( "/tmp/mont10_1x1.gif");
            generator.createMontage( files, 10, 1, 1, dst);
        }
        catch( Exception e) {
            fail( "Unexpected exception: " + e);
        }
    }

    @Test
    public void test5_1x1() {
        try {
            List<String> files = Arrays.asList(
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.97.20061214.091206.156000.5264219783.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.93.20061214.091206.156000.7442419769.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.98.20061214.091206.156000.5286319787.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.94.20061214.091206.156000.1465319771.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.99.20061214.091206.156000.9673919791.dcm");
            MontageGenerator generator = new MontageGenerator();
            File dst = new File( "/tmp/mont5_1x1.gif");
            generator.createMontage( files, 5, 1, 1, dst);
        }
        catch( Exception e) {
            fail( "Unexpected exception: " + e);
        }
    }

    @Test
    public void test10_2x2() {
        try {
            List<String> files = Arrays.asList(
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.90.20061214.091206.156000.8960719755.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.95.20061214.091206.156000.9278819775.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.91.20061214.091206.156000.2012419759.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.96.20061214.091206.156000.3558719779.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.92.20061214.091206.156000.2982419763.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.97.20061214.091206.156000.5264219783.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.93.20061214.091206.156000.7442419769.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.98.20061214.091206.156000.5286319787.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.94.20061214.091206.156000.1465319771.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.99.20061214.091206.156000.9673919791.dcm");
            MontageGenerator generator = new MontageGenerator();
            File dst = new File( "/tmp/mont10_2x2.gif");
            generator.createMontage( files, 10, 2, 2, dst);
        }
        catch( Exception e) {
            fail( "Unexpected exception: " + e);
        }
    }

    @Test
    public void test5_2x3() {
        try {
            List<String> files = Arrays.asList(
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.97.20061214.091206.156000.5264219783.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.93.20061214.091206.156000.7442419769.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.98.20061214.091206.156000.5286319787.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.94.20061214.091206.156000.1465319771.dcm",
                    "/Users/drm/projects/nrg/data/sample1/small/1.MR.head_DHead.6.99.20061214.091206.156000.9673919791.dcm");
            MontageGenerator generator = new MontageGenerator();
            File dst = new File( "/tmp/mont5_2x3.gif");
            generator.createMontage( files, 5, 2, 3, dst);
        }
        catch( Exception e) {
            fail( "Unexpected exception: " + e);
        }
    }

}
