package org.nrg.xnat.snapshot.generator.impl;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to generate montage images of variable dimensions from a list of DICOM files.
 *
 * This implementation hardcodes the ImageReader to dcm4che3's DICOM image reader due to problems with collisions
 * with the 2.x version that is also on the classpath.
 *
 * The imageWriter is hardcoded to GIF but this could be made configureable fairly easily.
 *
 * Assumes all images are of the same type. Does not center smaller images in montage panel.
 */
@Slf4j
public class MontageGenerator extends DicomImageRenderer {

    private SliceCoordinateCalculator sliceCoordinateCalculator;

    public MontageGenerator() {
        super();
        this.sliceCoordinateCalculator = new SliceCoordinateCalculator();
    }

    /**
     * Save the montage image created from the list of files to the destination file.
     *
     * @param files The list of files from which to draw the montage images.
     * @param nSlices The total number of image slices present in the images. (Some of the images can be multi-framed)
     * @param rows The number of rows in the montage image.
     * @param cols The number of columns in the montage image.
     * @param dst The file in which to write the montage image.
     * @return The montage-image file. (The default format is gif).
     * @throws Exception
     */
    public File createMontage( List<String> files, int nSlices, int rows, int cols, File dst) throws Exception {
        writeImage( dst, generate( files, nSlices, rows, cols));
        return dst;
    }

    /**
     * Return the montage image created from the list of files as a BufferedImage.
     * Useful if you want to do continued violence to the image.
     *
     * @param files The list of files from which to draw the montage images.
     * @param nSlices The total number of image slices present in the images. (Some of the images can be multi-framed)
     * @param rows The number of rows in the montage image.
     * @param cols The number of columns in the montage image.
     * @return The montage-image BufferedImage.
     * @throws Exception
     */
    public BufferedImage generate( List<String> files, int nSlices, int rows, int cols) throws Exception {
        try {
            int nPanels = rows * cols;
            // The slices in the files may be spread out in mutliple possibly multi-frame images.
            // Select the images and frames to fill the montage panel.
            List<SliceCoordinate> sliceCoordinates = sliceCoordinateCalculator.getSliceCoordinates(nPanels, nSlices, files);

            // Read the selected files into BufferedImages.
            List<BufferedImage> bis = new ArrayList<>();
            for (SliceCoordinate sc : sliceCoordinates) {
                bis.add( readImage(new File(files.get(sc.getFileNumber())), sc.getFrameNumber()));
            }

            // Create the BufferedImage for the montage.
            BufferedImage montageBufferedImage;
            // The montage is just the selected image if only one panel is requested.
            if (nPanels == 1) {
                montageBufferedImage = bis.get(0);
            } else {
                // The panels will be equal in size to the largest image in the set.
                Dimensions srcDimensions = getMaxDimenions(bis);

                Dimensions montageDimensions = new Dimensions(rows * srcDimensions.rows, cols * srcDimensions.cols);
                // All of the images are assumed to be of the same type.
                // TODO: Pick a lowest-common image type and convert image types if needed.
                montageBufferedImage = new BufferedImage(montageDimensions.cols, montageDimensions.rows, bis.get(0).getType());

                // Write the individual images into the panels.
                // TODO: This puts images smaller than the panael in the upper left corner of the panel instead of the more aesthetically pleasing center.
                int ib = 0;
                for (int ir = 0; ir < rows; ir++) {
                    for (int ic = 0; ic < cols; ic++) {
                        if (ib < bis.size()) {
                            BufferedImage bi = bis.get(ib);
                            addPanel(bi, srcDimensions, montageBufferedImage, ir, ic);
                            ib++;
                        } else {
                            break;
                        }
                    }
                }
            }
            return montageBufferedImage;
        }
        catch( Exception e) {
            log.error( "Error generating montage image.", e);
            throw e;
        }
    }

    /**
     * A convenient local class to fling dimensions around.
     */
    private class Dimensions {
        public int rows = 0, cols = 0;
        public Dimensions( int rows, int cols) {
            this.rows = rows;
            this.cols = cols;
        }
    }

    /**
     * Draw the source image into the destination image at panel x, y.
     *
     * @param sbi The source image.
     * @param panelDimension The dimension of the panels in the destination image
     * @param dbi The destination image.
     * @param ic The column index of the panel.
     * @param ir The row index of the panel.
     */
    private void addPanel( BufferedImage sbi, Dimensions panelDimension, BufferedImage dbi, int ir, int ic) {
        Graphics2D g2d = dbi.createGraphics();
        g2d.setComposite( AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2d.drawImage( sbi, ic * panelDimension.cols, ir * panelDimension.rows, null);
        g2d.dispose();
    }

    /**
     * Troll through the images and find the largest x and y dimensions.
     *
     * @param bis The list of images to troll.
     * @return The dimensions of largest x and y.
     */
    private Dimensions getMaxDimenions( List<BufferedImage> bis) {
        Dimensions maxd = new Dimensions(0, 0);
        for( BufferedImage bi: bis) {
            maxd.cols = Math.max( bi.getWidth(), maxd.cols);
            maxd.rows = Math.max( bi.getHeight(), maxd.rows);
        }
        return maxd;
    }

}
