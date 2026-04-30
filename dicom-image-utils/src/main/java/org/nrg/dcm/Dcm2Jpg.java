/*
 * DicomImageUtils: org.nrg.dcm.Dcm2Jpg
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm;

import org.dcm4che3.io.DicomInputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Dcm2Jpg {

    public static boolean canConvert() {
        return ImageIO.getImageReadersByFormatName("DICOM").hasNext();
    }

    public static ImageReader getDicomImageReader() {
        if (canConvert()) {
            return ImageIO.getImageReadersByFormatName("DICOM").next();
        }
        return null;
    }

    public static boolean isDicom(final File file) {
        try (final DicomInputStream input = new DicomInputStream(new FileInputStream(file))) {
            input.readFileMetaInformation();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static byte[] convert(final File file) throws IOException {
        if (!Dcm2Jpg.rescannedForPlugins) {
            ImageIO.scanForPlugins();
            Dcm2Jpg.rescannedForPlugins = true;
        }
        final ImageReader reader = getDicomImageReader();
        if (reader == null) {
            throw new IOException("Could not find an image reader for the DICOM format!");
        }
        try (final ImageInputStream input = ImageIO.createImageInputStream(file)) {
            final ImageReadParam param = reader.getDefaultReadParam();
            reader.setInput(input, false);
            final BufferedImage image = reader.read(frame - 1, param);
            if (image == null) {
                throw new IOException("Could not read from input stream");
            }
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", output);
            return output.toByteArray();
        }
    }

    private static boolean rescannedForPlugins = false;
    private static final int frame = 1;
}
