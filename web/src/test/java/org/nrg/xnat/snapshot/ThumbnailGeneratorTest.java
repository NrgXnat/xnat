package org.nrg.xnat.snapshot;

import org.junit.Ignore;
import org.junit.Test;
import org.nrg.xnat.snapshot.generator.impl.ThumbnailGenerator;

import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.fail;

public class ThumbnailGeneratorTest {
    @Test
    @Ignore
    public void test() {
        // This test is useful for experimenting with the thumbnail generator but the test dicom was not put into the repo,
        // plus it requires a user to look at the resulting images and thus is not an automated test.
        try {
//            File f = new File("/Users/drm/Box/DataSets/Test/MR/Breast-MRI-NACT-Pilot/Breast-MRI-NACT-Pilot/UCSF-BR-09/01-27-1987-283948-MR\\ BODY\\ RESEARC-11580/2.000000-BREASTPASag2DSpin\\ EchoSA-39921/1-10.dcm");
            File f = new File("/tmp/tn/in.dcm");

            Files.createDirectories(Paths.get("/tmp/tn"));

            ThumbnailGenerator generator = new ThumbnailGenerator();
            BufferedImage bufferedImage = generator.rescale(f, 0, 0.5f, 0.5f);
            File dest = new File("/tmp/tn/bicube.gif");
            generator.writeImage(dest, bufferedImage);

            generator.setAffineTransformOp( AffineTransformOp.TYPE_BILINEAR);
            bufferedImage = generator.rescale(f, 0, 0.5f, 0.5f);
            dest = new File("/tmp/tn/bilinear.gif");
            generator.writeImage(dest, bufferedImage);

            generator.setAffineTransformOp( AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            bufferedImage = generator.rescale(f, 0, 0.5f, 0.5f);
            dest = new File("/tmp/tn/nn.gif");
            generator.writeImage(dest, bufferedImage);

        } catch (IOException e) {
            fail("Unexpected exception: " + e);
        }

    }

    @Test
    @Ignore
    public void testScale() {

//        ThumbnailGenerator generator = new ThumbnailGenerator();
//
//        System.out.println( generator.scale( 128, 128, 0, 0, 1f));
//        System.out.println( generator.scale( 128, 128, 64, 0, 2f));
//        System.out.println( generator.scale( 128, 128, 64, 32, 1f));
//        System.out.println( generator.scale( 128, 128, 64, 32, 2f));
    }
}
