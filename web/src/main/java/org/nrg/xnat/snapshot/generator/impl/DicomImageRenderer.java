package org.nrg.xnat.snapshot.generator.impl;

import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReader;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReaderSpi;

import javax.imageio.*;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

/**
 * This is a basic DICOM image reader and Renderer.
 *
 * It is dcm4che3's Dcm2jpg class stripped of command line utility and hardcoded to gif.
 * The reader is hardcoded to dcm4che3's DicomImageReader due to collisions with v2.
 */
@Slf4j
public class DicomImageRenderer {
    public ImageReader getImageReader() {
        // Don't discover the ImageReader because the dcm4che v2 is still present and incompatible if picked.
        // imageReader  = ImageIO.getImageReadersByFormatName("DICOM").next();
        return new DicomImageReader(new DicomImageReaderSpi());
    }

    public ImageWriter getImageWriter() {
        final Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByFormatName(FORMAT_DEFAULT);
        if (!imageWriters.hasNext()) {
            throw new IllegalArgumentException(MessageFormat.format("output image format: {0} not supported", FORMAT_DEFAULT));
        }
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(imageWriters, Spliterator.CONCURRENT), true)
                            .filter(clazz -> clazz.getClass().getName().startsWith(ENCODER_DEFAULT))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException(MessageFormat.format("No Image Writer: {0} for format {1} found", ENCODER_DEFAULT, FORMAT_DEFAULT)));
    }

    /**
     * Read the specified frame from the DICOM image in the submitted file.
     *
     * @param file  The file from which to read.
     * @param frame The frame to read.
     *
     * @return The contents of the specified frame in an image buffer.
     *
     * @throws IOException When an error occurs reading the DICOM frame.
     */
    protected BufferedImage readImage(final File file, final int frame) throws IOException {
        try (final ImageInputStream iis = new FileImageInputStream(file)) {
            final ImageReader imageReader = getImageReader();
            imageReader.setInput(iis);
            return imageReader.read(frame, readParam(imageReader));
        }
    }

    /**
     * Write the data in the image buffer to the specified file.
     *
     * @param destination The file in which to write the image.
     * @param image       The image data to be written.
     *
     * @throws IOException When an error occurs writing the DICOM data.
     */
    public void writeImage(final File destination, final BufferedImage image) throws IOException {
        log.info("Preparing to write image to file {}", destination.getAbsolutePath());
        try (final RandomAccessFile output = new RandomAccessFile(destination, "rw")) {
            output.setLength(0);
            final ImageWriter     imageWriter = getImageWriter();
            final ImageWriteParam parameters  = imageWriter.getDefaultWriteParam();
            parameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            parameters.setCompressionType(COMPRESSION_DEFAULT);
            imageWriter.setOutput(new FileImageOutputStream(output));
            imageWriter.write(null, new IIOImage(image, null, null), parameters);
        } catch (IOException e) {
            log.error("An error occurred trying to write to the file {}", destination.getAbsolutePath(), e);
        }
    }

    private static ImageReadParam readParam(final ImageReader imageReader) {
        final DicomImageReadParam param = (DicomImageReadParam) imageReader.getDefaultReadParam();
        param.setPreferWindow(PREFER_WINDOW);
        param.setAutoWindowing(AUTO_WINDOWING);
        param.setWindowCenter(WINDOW_CENTER);
        param.setWindowWidth(WINDOW_WIDTH);
        param.setWindowIndex(WINDOW_INDEX);
        param.setVOILUTIndex(VOI_LUT_INDEX);
        param.setOverlayActivationMask(OVERLAY_ACTIVATION_MASK);
        param.setOverlayGrayscaleValue(OVERLAY_GRAYSCALE_VALUE);
        param.setOverlayRGBValue(OVERLAY_RGB_VALUE);
        return param;
    }

    private static final boolean PREFER_WINDOW           = true;
    private static final boolean AUTO_WINDOWING          = true;
    private static final int     WINDOW_CENTER           = 0;
    private static final int     WINDOW_WIDTH            = 0;
    private static final int     WINDOW_INDEX            = 0;
    private static final int     VOI_LUT_INDEX           = 0;
    private static final int     OVERLAY_ACTIVATION_MASK = 0xffff;
    private static final int     OVERLAY_GRAYSCALE_VALUE = 0xffff;
    private static final int     OVERLAY_RGB_VALUE       = 0xffffff;
    private static final String  FORMAT_DEFAULT          = "GIF";
    private static final String  ENCODER_DEFAULT         = "com.sun.imageio.plugins.";
    private static final String  COMPRESSION_DEFAULT     = null;
}
