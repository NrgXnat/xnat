package org.nrg.dicom.dicomedit.pixels.impl;

import com.pixelmed.dicom.*;
import com.pixelmed.display.ImageEditUtilities;
import com.pixelmed.display.SourceImage;
import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;
import com.pixelmed.utils.FileUtilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Vector;

/**
 * <p>This class allows the user to black out burned-in annotation, and save the result.</p>
 *
 * This code is the functional part, separated from the UI part, of the original and then made more suitable
 * to be a utility in a library.
 * @author dmaffitt
 *
 * Original code is com.pixelmed.display.DicomImageBlackout, by
 * @author dclunie
 */
public class DicomImageBlackout {

    protected String srcFileName;

    protected AttributeList list;
    protected SourceImage sImg;
    protected boolean changesWereMade;
    protected boolean deferredDecompression;

    protected boolean compressOutput = false;

    /**
     * Add overlays to pixel data before blanking?
     *
     * Default is false.
     */
    private boolean burnInOverlays = false;

    /**
     * Policy for handling BurnedInAnnotation attribute.
     *
     * Default is ADD_AS_NO_IF_CHANGED
     */
    private BurnedInAnnotationFlagAction burnedInFlagAction = BurnedInAnnotationFlagAction.ADD_AS_NO_IF_CHANGED;

    /**
     * Value for FileMetaAttribute AETitle.
     *
     * Default is "DicomImageBlackout"
     */
    private String aETitle = "DicomImageBlackout";

    /**
     * use the PixelPadding value as the blackout fill value?
     *
     * Default is false.
     */
    private boolean usePixelPaddingBlackoutValue = false;

    /**
     * Use zero as the blackout fill value?
     *
     * Default is true.
     */
    private boolean useZeroBlackoutValue = true;

    /**
     * Use this blackout fill value if usePixelPaddingBlackoutValue is false and useZeroBlackoutValuen is false.
     *
     * Default value is 0.
     */
    private int specifiedBlackoutValue = 0;

    private static final Logger logger = LoggerFactory.getLogger(DicomImageBlackout.class);

    protected void process( AttributeList list, File dstDicom, Vector shapes)  throws Exception {
        load( list);
        edit( shapes);
        save( dstDicom);
    }

    protected void process( File srcDicom, File dstDicom, Vector shapes)  throws Exception {
        load( srcDicom);
        edit( shapes);
        save( dstDicom);
    }

    protected void load( String srcFileName) throws Exception {
        if( srcFileName != null) {
            this.srcFileName = srcFileName;
            File currentFile = FileUtilities.getFileFromNameInsensitiveToCaseIfNecessary( srcFileName);
            load( currentFile);
        }
        else {
            String msg = "Source file name is null.";
            logger.error( msg);
            throw new Exception( msg);
        }
    }

    /**
     * <p>Load the named DICOM file.</p>
     *
     * @param    currentFile
     */
    protected void load( File currentFile) throws Exception {
        changesWereMade = false;
        try {
            logger.info("loadDicomFileOrDirectory(): Open {}", currentFile);
            srcFileName = currentFile.getAbsolutePath();        // set to what we actually used, used for later save
            deferredDecompression = CompressedFrameDecoder.canDecompress( currentFile);
            logger.info("loadDicomFileOrDirectory(): deferredDecompression {}", deferredDecompression);
            DicomInputStream i = new DicomInputStream(currentFile);
            AttributeList list = new AttributeList();
            list.setDecompressPixelData(!deferredDecompression);
            list.read(i);
            i.close();

            load( list);
        } catch (Exception e) {
            String msg = String.format("Read failed: %s", currentFile.getAbsolutePath());
            logger.error(msg, e);
            throw new Exception( msg, e);
        }
    }

    /**
     * Load the pixel data from the AttributeList.
     *
     * Throws an Exception if:
     *  1. The SOP Class UID is not an image type.
     *
     * @param list
     * @throws Exception
     */
    protected void load( AttributeList list) throws Exception {
        this.list = list;
        changesWereMade = false;
        try {
            deferredDecompression = CompressedFrameDecoder.canDecompress( list);
            logger.info("loadDicomFileOrDirectory(): deferredDecompression {}", deferredDecompression);
            list.setDecompressPixelData(!deferredDecompression);        // we don't want to decompress it during read if we can decompress it on the fly during display (000784)
            String useSOPClassUID = Attribute.getSingleStringValueOrEmptyString( list, TagFromName.SOPClassUID);
            String transferSynataxUID = Attribute.getSingleStringValueOrEmptyString( list, TagFromName.TransferSyntaxUID);
            if (SOPClass.isImageStorage(useSOPClassUID)) {
                sImg = new SourceImage(list);
            } else {
                String msg = String.format("Unsupported SOP Class (non image storage): %s", useSOPClassUID);
                logger.error(msg);
                throw new DicomException( msg);
            }
        } catch (Exception e) {
            String msg = "Load failed.";
            logger.error( msg, e);
            throw new Exception( msg, e);
        }
    }

    /**
     * Blackout the shapes in the loaded image.
     *
     * @param shapes
     * @throws Exception
     */
    protected void edit( Vector  shapes) throws Exception {
        long startTime = System.currentTimeMillis();
        if (sImg != null && list != null) {
            if ((shapes != null && shapes.size() > 0)) {

                String transferSyntaxUID = Attribute.getSingleStringValueOrEmptyString(list, TagFromName.TransferSyntaxUID);
                try {
                    logger.debug("blackout: Blackout decompressed image");
                    // may not have been decompressed during AttributeList reading when CompressedFrameDecoder.canDecompress(currentFile) is true,
                    // but ImageEditUtilities.blackout() can decompress on the fly because it calls SourceImage.getBufferedImage(frame);
                    // we like that because it uses less (esp. contiguous) memory
                    // but we need to make sure when writing it during Save that PhotometricInterpretation is corrected, etc. vide infra
                    ImageEditUtilities.blackout(sImg, list, shapes, burnInOverlays, usePixelPaddingBlackoutValue, useZeroBlackoutValue, specifiedBlackoutValue);
                    changesWereMade = true;
                    // do NOT need to recreate SourceImage from list ... already done by blackout()
                } catch (Exception e) {
                    String msg = "Blackout failed.";
                    logger.error( msg, e);
                    throw new Exception( msg, e);
                }
            } else {
                logger.info( "blackout: no shapes or burning in of overlays to do.");
            }
        } else {
            logger.info("blackout: no image or list to process.");
        }
        logger.info("blackout: total time = {}", (System.currentTimeMillis() - startTime));
    }

    /**
     * Save the edited image to the specified file.
     *
     * @param dstFile
     * @throws Exception
     */
    protected void save( File dstFile) throws Exception {
        logger.debug("Save to {}", dstFile);

        long startTime = System.currentTimeMillis();
        boolean success = true;
        try {
            sImg.close();        // in case memory-mapped pixel data open; would inhibit Windows rename or copy/reopen otherwise
            sImg = null;
//            System.gc();         // cannot guarantee that buffers will be released, causing problems on Windows, but try ... http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4715154 :(
//            System.runFinalization();
//            System.gc();
        } catch (Throwable t) {
            logger.error("Save: unable to close image - not saving modifications");
            success = false;
        }
        if( ! dstFile.getParentFile().exists()) {
            dstFile.getParentFile().mkdirs();
        }
//        File newFile = new File(srcFileName + ".new");
        File tmpFile = Files.createTempFile("pixanon", ".dcm").toFile();
        logger.debug("Save: tmpFile = " + tmpFile);

        if (success) {
            String transferSyntaxUID = Attribute.getSingleStringValueOrEmptyString(list, TagFromName.TransferSyntaxUID);
            try {
                String outputTransferSyntaxUID = null;
                list.correctDecompressedImagePixelModule(deferredDecompression);                    // make sure to correct even if decompression was deferred
                list.insertLossyImageCompressionHistoryIfDecompressed(deferredDecompression);
                if (burnedInFlagAction != BurnedInAnnotationFlagAction.LEAVE_ALONE) {
                    list.remove(TagFromName.BurnedInAnnotation);
                    if (burnedInFlagAction == BurnedInAnnotationFlagAction.ADD_AS_NO_IF_SAVED
                            || (burnedInFlagAction == BurnedInAnnotationFlagAction.ADD_AS_NO_IF_CHANGED && changesWereMade)) {
                        Attribute a = new CodeStringAttribute(TagFromName.BurnedInAnnotation);
                        a.addValue("NO");
                        list.put(a);
                    }
                }
                if (changesWereMade) {
                    {
                        Attribute aDeidentificationMethod = list.get(TagFromName.DeidentificationMethod);
                        if (aDeidentificationMethod == null) {
                            aDeidentificationMethod = new LongStringAttribute(TagFromName.DeidentificationMethod);
                            list.put(aDeidentificationMethod);
                        }
                        if (burnInOverlays) {
                            aDeidentificationMethod.addValue("Overlays burned in then blacked out");
                        }
                        aDeidentificationMethod.addValue("Burned in text blacked out");
                    }
                    {
                        SequenceAttribute aDeidentificationMethodCodeSequence = (SequenceAttribute) (list.get(TagFromName.DeidentificationMethodCodeSequence));
                        if (aDeidentificationMethodCodeSequence == null) {
                            aDeidentificationMethodCodeSequence = new SequenceAttribute(TagFromName.DeidentificationMethodCodeSequence);
                            list.put(aDeidentificationMethodCodeSequence);
                        }
                        aDeidentificationMethodCodeSequence.addItem(new CodedSequenceItem("113101", "DCM", "Clean Pixel Data Option").getAttributeList());
                    }
                }
                list.removeGroupLengthAttributes();
                list.removeMetaInformationHeaderAttributes();
                list.remove(TagFromName.DataSetTrailingPadding);

                if( compressOutput) {
                    Compressor compressor = new Compressor();
                    compressor.compress( list, TransferSyntax.JPEGBaseline, dstFile.getAbsolutePath());
//                    compressor.compress( list, TransferSyntax.JPEGLossless, dstFile.getAbsolutePath());
                }
                else {
                    outputTransferSyntaxUID = TransferSyntax.ExplicitVRLittleEndian;
                    FileMetaInformation.addFileMetaInformation(list, outputTransferSyntaxUID, aETitle);
                    list.write(tmpFile, outputTransferSyntaxUID, true/*useMeta*/, true/*useBufferedStream*/);

                    list = null;
                    try {
                        logger.debug("Replace dstFile {} with tmpFile {}", dstFile, tmpFile);
                        FileUtilities.renameElseCopyTo(tmpFile, dstFile);
                    } catch (IOException e) {
                        String msg = "save: unable to rename " + tmpFile + " to " + dstFile + " - not saving modifications";
                        logger.error(msg);
                        success = false;
                        throw new Exception(msg, e);
                    }
                }

                changesWereMade = false;
                logger.debug("Save of " + dstFile.getAbsolutePath() + " succeeded");
            } catch (DicomException | IOException e) {
                String msg = "Save failed.";
                logger.error( msg, e);
                throw new Exception( msg, e);
            }
        }
        logger.info("Save: total time = {}", (System.currentTimeMillis() - startTime));
    }

    /**
     * <p>Enum of values for the Burned in Annotation action
     */
    public enum BurnedInAnnotationFlagAction {
        /**
         * <p>Leave any existing Burned in Annotation attribute value alone.</p>
         */
        LEAVE_ALONE,
        /**
         * <p>Always remove the Burned in Annotation attribute when the file is saved, without replacing it.</p>
         */
        ALWAYS_REMOVE,
        /**
         * <p>Always remove the Burned in Annotation attribute when the file is saved, only replacing it and using a value of NO when regions have been blacked out.</p>
         */
        ADD_AS_NO_IF_CHANGED,
        /**
         * <p>Always remove the Burned in Annotation attribute when the file is saved, always replacing it with a value of NO,
         * regardless of whether when regions have been blacked out, such as when visual inspection confirms that there is no
         * burned in annotation.</p>
         */
        ADD_AS_NO_IF_SAVED
    }

    public boolean isBurnInOverlays() {
        return burnInOverlays;
    }

    public void setBurnInOverlays(boolean burnInOverlays) {
        this.burnInOverlays = burnInOverlays;
    }

    public BurnedInAnnotationFlagAction getBurnedInFlagAction() {
        return burnedInFlagAction;
    }

    public void setBurnedInFlagAction(BurnedInAnnotationFlagAction burnedInFlagAction) {
        this.burnedInFlagAction = burnedInFlagAction;
    }

    public String getAETitle() {
        return aETitle;
    }

    public void setAETitle(String aETitle) {
        this.aETitle = aETitle;
    }

    public boolean isUsePixelPaddingBlackoutValue() {
        return usePixelPaddingBlackoutValue;
    }

    public void setUsePixelPaddingBlackoutValue(boolean usePixelPaddingBlackoutValue) {
        this.usePixelPaddingBlackoutValue = usePixelPaddingBlackoutValue;
    }

    public boolean isUseZeroBlackoutValue() {
        return useZeroBlackoutValue;
    }

    public void setUseZeroBlackoutValue(boolean useZeroBlackoutValue) {
        this.useZeroBlackoutValue = useZeroBlackoutValue;
    }

    public int getSpecifiedBlackoutValue() {
        return specifiedBlackoutValue;
    }

    public void setSpecifiedBlackoutValue(int specifiedBlackoutValue) {
        this.specifiedBlackoutValue = specifiedBlackoutValue;
    }

    @Override
    public String toString() {
        return "DicomImageBlackout{" +
                "burnInOverlays=" + burnInOverlays +
                ", burnedInFlagAction=" + burnedInFlagAction +
                ", aETitle='" + aETitle + '\'' +
                ", usePixelPaddingBlackoutValue=" + usePixelPaddingBlackoutValue +
                ", useZeroBlackoutValue=" + useZeroBlackoutValue +
                ", specifiedBlackoutValue=" + specifiedBlackoutValue +
                '}';
    }
}
