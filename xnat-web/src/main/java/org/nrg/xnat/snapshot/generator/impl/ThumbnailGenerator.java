package org.nrg.xnat.snapshot.generator.impl;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Rescale a BufferedImage.
 *
 * The default interpolation operation is TYPE_BICUBIC
 *
 */
public class ThumbnailGenerator {
    private int affineTransformOp;

    public ThumbnailGenerator() {
        this.affineTransformOp = AffineTransformOp.TYPE_BICUBIC;
    }

    public int getAffineTransformOp() {
        return affineTransformOp;
    }

    public void setAffineTransformOp(int affineTransformOp) {
        this.affineTransformOp = affineTransformOp;
    }

    public BufferedImage rescale(BufferedImage bi, float scaleRows, float scaleCols) throws IOException {
        AffineTransformOp op = new AffineTransformOp( AffineTransform.getScaleInstance(scaleCols, scaleRows), affineTransformOp);
        return op.filter(bi, null);
    }

    public BufferedImage rescale(BufferedImage bi, int r, int c, float sy) throws IOException {
        if (r == 0 && c == 0 && sy == 1f)
            return bi;

        float sx = 1f;
        if (r != 0 || c != 0) {
            if (r != 0 && c != 0)
                if (r * bi.getWidth() > c * bi.getHeight() * sy)
                    r = 0;
                else
                    c = 0;
            sx = r != 0 ? r / (bi.getHeight() * sy) : c / (float)bi.getWidth();
            sy *= sx;
        }
        AffineTransformOp op = new AffineTransformOp(
                AffineTransform.getScaleInstance(sx, sy),
                affineTransformOp);
        return op.filter(bi, null);
    }

    public String scale( int ow, int oh, int r, int c, float sy) {
        String s =  String.format("ow: %d  oh: %d  r: %d  c: %d  sy: %f", ow, oh, r, c, sy);
        float sx = 1f;
        if (r != 0 || c != 0) {
            if (r != 0 && c != 0)
                if (r * ow > c * oh * sy)
                    r = 0;
                else
                    c = 0;
            sx = r != 0 ? r / (oh * sy) : c / (float)ow;
            sy *= sx;
        }
        return s + "\n" +  String.format("ow: %d  oh: %d  r: %d  c: %d  sy: %f  sx: %f", ow, oh, r, c, sy, sx);
    }

}
