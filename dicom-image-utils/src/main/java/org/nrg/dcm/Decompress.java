/*
 * DicomImageUtils: org.nrg.dcm.Decompress
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import org.apache.commons.lang3.StringUtils;
import org.dcm4che2.data.*;
import org.dcm4che2.image.ColorModelFactory;
import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReader;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReaderSpi;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageWriter;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageWriterSpi;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.net.PDVInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.*;


/**
 * Decompresses a dicom image. Accepts the dicom image from a file
 * or a dicom network stream.
 * Currently only the following transfer syntax UIDs are supported:
 * 1.2.840.10008.1.2.5
 * 1.2.840.10008.1.2.4.50
 * 1.2.840.10008.1.2.4.57
 * 1.2.840.10008.1.2.4.70
 * 1.2.840.10008.1.2.4.80
 * 1.2.840.10008.1.2.4.81
 * 1.2.840.10008.1.2.4.90
 * 1.2.840.10008.1.2.4.91
 * 1.2.840.10008.1.2.4.100
 *
 * @author Aditya Siram &lt;aditya.siram@gmail.com&gt;
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
@SuppressWarnings("unused")
public class Decompress {
    public static        TransferSyntax destinationSyntax = TransferSyntax.valueOf(UID.ExplicitVRLittleEndian);
    private static final Logger         logger            = LoggerFactory.getLogger(Decompress.class);

    /**
     * Are the underlying image i/o libraries available?
     *
     * @return true if library support is available
     */
    public static boolean isSupported() {
        try {
            new DicomImageReaderSpi().createReaderInstance();
            logger.debug("Library support for decompressing DICOM images is available.");
            return true;
        } catch (IOException e) {
            logger.info("Unable to load reader instance for decompressing DICOM images");
            logger.debug("", e);
            return false;
        } catch (NoClassDefFoundError e) {
            logger.info("No library support for decompressing DICOM images.");
            logger.debug("", e);
            return false;
        }
    }

    //--------------------------
    // Utility Functions
    //--------------------------

    public static String getTsuid(final DicomObject dicomObject) {
        return dicomObject.getString(Tag.TransferSyntaxUID);
    }

    public static boolean needsDecompress(final String tsuid) {
        return StringUtils.isNotBlank(tsuid) && StringUtils.equalsAny(UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndian);
    }

    //--------------------------
    // Conversion functions
    //--------------------------

    public static byte[] pdv2Bytes(PDVInputStream in, String tsuid) throws IOException {
        DicomObject ds = in.readDataset();
        return dicomObject2ByteStream(ds, tsuid).toByteArray();
    }

    public static byte[] file2Bytes(final File file) throws IOException {
        try (FileInputStream fileInput = new FileInputStream(file); DicomInputStream dicomInput = new DicomInputStream(fileInput)) {
            return dicomObject2Bytes(dicomInputStream2DicomObject(dicomInput));
        }
    }


    /**
     * Reads the DICOM object from the input stream. The stream mark is advanced.
     *
     * @param dicomInput The DICOM input to be read.
     *
     * @return The copy of the incoming DICOM object.
     *
     * @throws IOException When an error occurs during I/O operations.
     */
    public static DicomObject dicomInputStream2DicomObject(final DicomInputStream dicomInput) throws IOException {
        final DicomObject incoming = dicomInput.readDicomObject();
        final DicomObject outgoing = new BasicDicomObject();
        incoming.copyTo(outgoing);
        dicomInput.close();
        return outgoing;
    }

    /**
     * Reads a set of bytes back in to a DicomObject
     *
     * @param input The bytes to convert to DICOM.
     *
     * @return The created DICOM object.
     *
     * @throws IOException When an error occurs during I/O operations.
     */
    public static DicomObject bytes2DicomObject(final byte[] input) throws IOException {
        boolean successful = false;
        try (final ByteArrayInputStream byteInputStream = new ByteArrayInputStream(input);
             final DicomInputStream dicomInputStream = new DicomInputStream(byteInputStream)) {
            return dicomInputStream2DicomObject(dicomInputStream);
        }
    }


    private static final int[] FILE_METAINFO_FIELDS = new int[]{
            Tag.ImplementationClassUID,
            Tag.ImplementationVersionName,
            Tag.SourceApplicationEntityTitle,
            Tag.PrivateInformationCreatorUID,
            Tag.PrivateInformation
    };

    /**
     * Writes the given DICOM object to an OutputStream.
     *
     * @param outputStream OutputStream destination
     * @param dicomObject  DicomObject to be written
     * @param tsuid        Transfer Syntax UID; if null, will be taken from
     *
     * @throws IOException When an error occurs during I/O operations.
     */
    public static void writeDicomObjectTo(final OutputStream outputStream, final DicomObject dicomObject, final String tsuid) throws IOException {
        try (final DicomOutputStream dicomOutputStream = new DicomOutputStream(outputStream)) {
            // if no TS UID was specified, use the DICOM default (Implicit VR LE) as a default.
            final String ts = dicomObject.getString(Tag.TransferSyntaxUID, null == tsuid ? UID.ImplicitVRLittleEndian : tsuid);
            if (null != tsuid && !tsuid.equals(ts)) {
                throw new IllegalArgumentException("The specified transfer syntax UID " + tsuid
                                                   + " is different from object transfer syntax " + ts);
            }
            final DicomObject fmi = new BasicDicomObject();
            fmi.initFileMetaInformation(dicomObject.getString(Tag.SOPClassUID),
                                        dicomObject.getString(Tag.SOPInstanceStatus),
                                        ts);
            for (final int tag : FILE_METAINFO_FIELDS) {
                final DicomElement e = dicomObject.get(tag);
                if (null != e) {
                    fmi.add(e);
                }
            }
            dicomOutputStream.writeFileMetaInformation(fmi);
            dicomOutputStream.writeDataset(dicomObject.dataset(), ts);
        }
    }

    /**
     * Converts a DicomObject to bytes. NOTE: Before converting it added the given
     * transfer syntax to the dicom object.
     *
     * @param in    The DicomObject to be converted
     * @param tsuid Transfer syntax UID.
     *
     * @return The DICOM object as a byte array.
     *
     * @throws IOException When an error occurs during I/O operations.
     */
    private static ByteArrayOutputStream dicomObject2ByteStream(DicomObject in, String tsuid) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writeDicomObjectTo(bos, in, tsuid);
        return bos;
    }

    /**
     * Converts a DicomObject into an array of bytes
     *
     * @param in    The DICOM object to convert.
     * @param tsuid The transfer syntax UID for byte conversion.
     *
     * @return The DICOM object as a byte array.
     *
     * @throws IOException When an error occurs during I/O operations.
     */
    public static byte[] dicomObject2Bytes(final DicomObject in, final String tsuid)
            throws IOException {
        return dicomObject2ByteStream(in, tsuid).toByteArray();
    }

    /**
     * Converts a DicomObject into an array of bytes
     *
     * @param in The DICOM object to convert.
     *
     * @return The DICOM object as a byte array.
     *
     * @throws IOException When an error occurs during I/O operations.
     */
    public static byte[] dicomObject2Bytes(final DicomObject in) throws IOException {
        return dicomObject2ByteStream(in, null).toByteArray();
    }

    /**
     * Writes a dicom object to a file
     *
     * @param in          The DICOM object to convert.
     * @param destination The destination file.
     *
     * @return The file written out.
     *
     * @throws IOException When an error occurs during I/O operations.
     */
    public static File dicomObject2File(DicomObject in, File destination) throws IOException {
        try (final FileOutputStream fos = new FileOutputStream(destination)) {
            writeDicomObjectTo(fos, in, null);
            return destination;
        }
    }

    //---------------------------------------------------------
    // Decompress function stack.
    // All dicom input is converted to a byte stream which is then
    // decompressed and returned as a DicomObject
    //---------------------------------------------------------

    public static DicomObject decompress_image(File src) throws IOException {
        return decompress_image(file2Bytes(src));
    }

    public static DicomObject decompress_image(PDVInputStream in, String tsuid) throws IOException {
        return decompress_image(new ByteArrayInputStream(pdv2Bytes(in, tsuid)), tsuid);
    }

    public static DicomObject decompress_image(byte[] in) throws IOException {
        String tsuid = getTsuid(bytes2DicomObject(in));
        return decompress_image(in, tsuid);
    }

    public static DicomObject decompress_image(byte[] in, String tsuid) throws IOException {
        return decompress_image(new ByteArrayInputStream(in), tsuid);
    }

    public static DicomObject decompress_image(ByteArrayInputStream in, String tsuid) throws IOException {
        // create a reader and set the input to the input stream just created.
        final DicomImageReader reader     = (DicomImageReader) new DicomImageReaderSpi().createReaderInstance();
        boolean                successful = false;
        try (final ImageInputStream reader_in = ImageIO.createImageInputStream(in);
             final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             final DicomOutputStream dicomOutputStream = new DicomOutputStream(byteArrayOutputStream);
             final MemoryCacheImageOutputStream memoryOutputStream = new MemoryCacheImageOutputStream(dicomOutputStream)) {
            reader.setInput(reader_in, true);

            // hook the output stream into the image writer
            final DicomImageWriter writer = (DicomImageWriter) new DicomImageWriterSpi().createWriterInstance();
            writer.setOutput(memoryOutputStream);

            // copy the metadata from the incoming dicom object replacing the transfer
            // syntax with the desired outgoing transfer syntax
            final DicomStreamMetaData in_meta   = (DicomStreamMetaData) reader.getStreamMetadata();
            final DicomObject         ds        = in_meta.getDicomObject();
            final DicomStreamMetaData writeMeta = new DicomStreamMetaData();
            final DicomObject         newDs     = new BasicDicomObject();
            ds.copyTo(newDs);
            newDs.putString(Tag.TransferSyntaxUID, VR.UI, destinationSyntax.uid());
            writeMeta.setDicomObject(newDs);
            writer.prepareWriteSequence(writeMeta);

            // read and interpret the incoming image
            int frames = ds.getInt(Tag.NumberOfFrames, 1);
            for (int i = 0; i < frames; i++) {
                final WritableRaster r   = (WritableRaster) reader.readRaster(i, null);
                final ColorModel     cm  = ColorModelFactory.createColorModel(ds);
                final BufferedImage  bi  = new BufferedImage(cm, r, false, null);
                final IIOImage       iio = new IIOImage(bi, null, null);
                writer.writeToSequence(iio, null);
            }

            // close off the image with a Delimitation item.
            writer.endWriteSequence();

            //return a new dicom object created using the bytes in the output stream
            return bytes2DicomObject(byteArrayOutputStream.toByteArray());
        } finally {
            try {
                reader.dispose();
            } catch (Throwable e) {
                logger.error("Unable to decompress this dicom file ", e);
            }
        }
    }
}
