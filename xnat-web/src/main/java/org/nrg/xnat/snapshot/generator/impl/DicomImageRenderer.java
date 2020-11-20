package org.nrg.xnat.snapshot.generator.impl;

import org.dcm4che3.image.ICCProfile;
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
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

/**
 * This is a basic DICOM image reader and Renderer.
 *
 * It is dcm4che3's Dcm2jpg class stripped of command line utility and hardcoded to gif.
 * The reader is hardcoded to dcm4che3's DicomImageReader due to collistions with v2.
 *
 */
public class DicomImageRenderer {
    private int windowIndex;
    private int voiLUTIndex;
    private boolean preferWindow = true;
    private float windowCenter;
    private float windowWidth;
    private boolean autoWindowing = true;
    private ImageReader imageReader;
    private ImageWriter imageWriter;
    private ImageWriteParam imageWriteParam;
    private int overlayActivationMask = 0xffff;
    private int overlayGrayscaleValue = 0xffff;
    private int overlayRGBValue = 0xffffff;
    private ICCProfile.Option iccProfile = ICCProfile.Option.none;

    private static String FORMAT_DEFAULT = "GIF";
    private static String ENCODER_DEFAULT = "com.sun.imageio.plugins.*";
    private static String COMPRESSIONTYPE_DEFAULT = null;
    private static Number QUALITY_DEFAULT = null;

    public DicomImageRenderer() {
        initImageReader();
        // Discovering the imageWriter hasn't been a problem since it is the generic gif writer.
        initImageWriter( FORMAT_DEFAULT, ENCODER_DEFAULT, COMPRESSIONTYPE_DEFAULT, QUALITY_DEFAULT);
    }

    public void initImageReader() {
        // Don't discover the ImageReader because the dcm4che v2 is still present and incompatible if picked.
        // imageReader  = ImageIO.getImageReadersByFormatName("DICOM").next();
        imageReader = new DicomImageReader( new DicomImageReaderSpi());

    }
    public void initImageWriter(String formatName, String clazz, String compressionType, Number quality) {
        Iterator<ImageWriter> imageWriters =
                ImageIO.getImageWritersByFormatName(formatName);
        if (!imageWriters.hasNext())
            throw new IllegalArgumentException(
                    MessageFormat.format("output image format: {0} not supported", formatName));
        Iterable<ImageWriter> iterable = () -> imageWriters;
        imageWriter = StreamSupport.stream(iterable.spliterator(), false)
                .filter(matchClassName(clazz))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        MessageFormat.format("no Image Writer: {0} for format {1} found", clazz, formatName)));
        imageWriteParam = imageWriter.getDefaultWriteParam();
        if (compressionType != null || quality != null) {
            imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            if (compressionType != null)
                imageWriteParam.setCompressionType(compressionType);
            if (quality != null)
                imageWriteParam.setCompressionQuality(quality.floatValue());
        }
    }

    private static Predicate<Object> matchClassName(String clazz) {
        Predicate<String> predicate = clazz.endsWith("*")
                ? startsWith(clazz.substring(0, clazz.length() - 1))
                : clazz::equals;
        return w -> predicate.test(w.getClass().getName());
    }

    private static Predicate<String> startsWith(String prefix) {
        return s -> s.startsWith(prefix);
    }

    public final void setWindowCenter(float windowCenter) {
        this.windowCenter = windowCenter;
    }

    public final void setWindowWidth(float windowWidth) {
        this.windowWidth = windowWidth;
    }

    public final void setWindowIndex(int windowIndex) {
        this.windowIndex = windowIndex;
    }

    public final void setVOILUTIndex(int voiLUTIndex) {
        this.voiLUTIndex = voiLUTIndex;
    }

    public final void setPreferWindow(boolean preferWindow) {
        this.preferWindow = preferWindow;
    }

    public final void setAutoWindowing(boolean autoWindowing) {
        this.autoWindowing = autoWindowing;
    }

    public void setOverlayActivationMask(int overlayActivationMask) {
        this.overlayActivationMask = overlayActivationMask;
    }

    public void setOverlayGrayscaleValue(int overlayGrayscaleValue) {
        this.overlayGrayscaleValue = overlayGrayscaleValue;
    }

    public void setOverlayRGBValue(int overlayRGBValue) {
        this.overlayRGBValue = overlayRGBValue;
    }

    public final void setICCProfile(ICCProfile.Option iccProfile) {
        this.iccProfile = Objects.requireNonNull(iccProfile);
    }

    private ImageReadParam readParam() {
        DicomImageReadParam param = (DicomImageReadParam) imageReader.getDefaultReadParam();
        param.setWindowCenter(windowCenter);
        param.setWindowWidth(windowWidth);
        param.setAutoWindowing(autoWindowing);
        param.setWindowIndex(windowIndex);
        param.setVOILUTIndex(voiLUTIndex);
        param.setPreferWindow(preferWindow);
        param.setOverlayActivationMask(overlayActivationMask);
        param.setOverlayGrayscaleValue(overlayGrayscaleValue);
        param.setOverlayRGBValue(overlayRGBValue);
        return param;
    }

    /**
     * Read the frame from the specified DICOM image.
     * The imageReader is currently hardcoded to dcm4che3 Dicom reader because
     *
     * @param file
     * @param frame
     * @return
     * @throws IOException
     */
    protected BufferedImage readImage(File file, int frame) throws IOException {
        try (ImageInputStream iis = new FileImageInputStream(file)) {
            imageReader.setInput(iis);
            return imageReader.read( frame, readParam());
        }
    }

    /**
     * Write the bufferedImage to a File.
     * The imageWriter is currently set to be GIF.
     * @param dest
     * @param bi
     * @throws IOException
     */
    protected void writeImage(File dest, BufferedImage bi) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(dest, "rw")) {
            raf.setLength(0);
            imageWriter.setOutput(new FileImageOutputStream(raf));
            imageWriter.write(null, new IIOImage(bi, null, null), imageWriteParam);
        }
    }

}
