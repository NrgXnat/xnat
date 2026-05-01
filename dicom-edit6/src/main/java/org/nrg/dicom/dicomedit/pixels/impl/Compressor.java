package org.nrg.dicom.dicomedit.pixels.impl;

import com.pixelmed.apps.CompressDicomFiles;
import com.pixelmed.dicom.*;
import com.pixelmed.display.SourceImage;
import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;

public class Compressor {

    private static final Logger logger = LoggerFactory.getLogger(CompressDicomFiles.class);

    protected void compress( AttributeList list, String transferSyntaxUID, String outputPath ) {
        String outputFormat = CompressedFrameEncoder.chooseOutputFormatForTransferSyntax( transferSyntaxUID);
        logger.info("compress(): outputFormat = {} transferSyntaxUID = {}", outputFormat, transferSyntaxUID);
        try {
            boolean deferredDecompression = CompressedFrameDecoder.canDecompress( list);
            list.removeGroupLengthAttributes();
            list.remove(TagFromName.DataSetTrailingPadding);

            // NB. do NOT remove meta information until AFTER deferred decompression of pixel data for recompression, else SourceImage will fail to find the necessary TransferSyntax during deferred decompression

            SourceImage sImg = new SourceImage(list);

            int numberOfFrames = sImg.getNumberOfFrames();
            File[] frameFiles = new File[numberOfFrames];
            for (int f=0; f<numberOfFrames; ++f) {
                BufferedImage renderedImage = sImg.getBufferedImage(f);
                if (renderedImage == null) {
                    throw new DicomException("Could not get image for frame "+f);
                }
                File tmpFrameFile = File.createTempFile("CompressDicomFiles_tmp",".tmp");
                tmpFrameFile.deleteOnExit();
                frameFiles[f] = CompressedFrameEncoder.getCompressedFrameAsFile(list,renderedImage,outputFormat,tmpFrameFile);
            }

            OtherByteAttributeMultipleCompressedFrames aPixelData = new OtherByteAttributeMultipleCompressedFrames(TagFromName.PixelData,frameFiles);
            list.put(aPixelData);

            list.correctDecompressedImagePixelModule(deferredDecompression);					// make sure to correct even if decompression was deferred
            list.insertLossyImageCompressionHistoryIfDecompressed(deferredDecompression);

            // set compressed pixel data characteristics AFTER correctDecompressedImagePixelModule() ...
            String photometricInterpretation = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PhotometricInterpretation);

            if (photometricInterpretation.equals("YBR_FULL_422")) {
                // not converted during reading of lossy JPEG, e.g., really was uncompressed YBR_FULL_422
                // will be upsampled (but not color space converted) by SourceImage.getBufferedImage() called during frame compression so say so ... (000997)
                photometricInterpretation = "YBR_FULL";
                Attribute a = new CodeStringAttribute(TagFromName.PhotometricInterpretation); a.addValue(photometricInterpretation); list.put(a);
            }

            if (outputFormat.equals("jpeg2000")) {
                if (photometricInterpretation.equals("RGB")) {
                    photometricInterpretation = "YBR_RCT";
                    Attribute a = new CodeStringAttribute(TagFromName.PhotometricInterpretation); a.addValue(photometricInterpretation); list.put(a);
                }
                else {
                    // (000981)
                    // would be better to throw this earlier :(
                    throw new DicomException("Cannot encode "+photometricInterpretation+" using JPEG 2000, only RGB transformed to YBR_RCT is permitted");
                }
            }
            // else leave it alone, since neither JPEG lossless nor JPEG-LS codec nor RLE does a color space transformation

            if (Attribute.getSingleIntegerValueOrDefault(list,TagFromName.SamplesPerPixel,0) > 1) {
                // when RLE, always seperate bands; decompressed input may not have been
                // when JPEG family, output of JIIO codecs is always interleaved; decompressed RLE input may (should?) have been 1 though sometimes isn't, and uncompressed input could have been either
                { Attribute a = new UnsignedShortAttribute(TagFromName.PlanarConfiguration); a.addValue(outputFormat.equals("rle") ? 1 : 0); list.put(a); }
            }
            else {
                list.remove(TagFromName.PlanarConfiguration);	// just in case, since it shouldn't be there
            }

            list.removeMetaInformationHeaderAttributes();
            FileMetaInformation.addFileMetaInformation(list,transferSyntaxUID,"OURAETITLE");

            File outputFile = new File(outputPath);
            logger.info("doSomethingWithDicomFileOnMedia(): writing compressed file {}",outputFile);
            list.write(outputFile,transferSyntaxUID,true,true);

            for (int f=0; f<numberOfFrames; ++f) {
                frameFiles[f].delete();
                frameFiles[f] = null;
            }
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
            logger.error("While processing {}", outputPath, e);
        }
    }

}
