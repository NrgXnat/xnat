package org.nrg.dicom.dicomedit.pixels.impl;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.display.SourceImage;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.nio.file.Files;
import java.util.Vector;

/**
 * A Pixelmed-library-based PixelEditHandler that writes solid rectangular regions into images.
 *
 * Streams intermediate results to and from disk.
 * Uses pixelmed's ImageEditUtilities.blackout() which always uses zero as the fill color.
 * Logs warning if dicom object does not contain pixel data.
 *
 */
public class PixelmedPixelEditHandler extends SimpleRectanglePixelEditHandler {
    private static final Logger logger = LoggerFactory.getLogger( PixelmedPixelEditHandler.class);

    private DicomImageBlackout dicomImageBlackout;

    public PixelmedPixelEditHandler() {
        this.dicomImageBlackout = new DicomImageBlackout();
    }

    @Override
    public void process(Rectangle2D rect, Color color, DicomObjectI dobj) throws MizerException {
        try {
            AttributeList attributeList = loadAttributes( dobj);

            if( attributeList.isImage()) {
                Vector shapes = new Vector();
                Rectangle2D redactedRegion = clip( rect, dobj);
                if( redactedRegion.isEmpty()) {
                    logger.warn("Redacted region {} is not on image with SOP_INSTANCE_UID = {}", rect, dobj.getString(TAG_SOP_INSTANCE_UID));
                } else {
                    shapes.add( redactedRegion);
                }

                File tmpFile = File.createTempFile( "pixanon", ".dcm");
                dicomImageBlackout.process( attributeList, tmpFile, shapes);
                update( tmpFile, dobj);
            }
            else {
                logger.warn("Skipping non-image dicom.");
            }
        }
        catch( Exception e) {
            throw new MizerException("Error editing rectangular pixel region.", e);
        }
    }

    private Rectangle2D clip( Rectangle2D rect, DicomObjectI dobj) {
        Area blackedArea = new Area( rect);
        Area imageArea = new Area( getBounds( dobj));
        blackedArea.intersect( imageArea);
        return blackedArea.getBounds2D();
    }

    private int TAG_SOP_INSTANCE_UID = 0x00080018;
    private int TAG_NROWS = 0x00280010;
    private int TAG_NCOLS = 0x00280011;
    private Shape getBounds(DicomObjectI dobj) {
        try {
            return new Rectangle2D.Float(0, 0,
                    Integer.parseInt(dobj.getString(TAG_NCOLS)),
                    Integer.parseInt(dobj.getString(TAG_NROWS)));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("DICOM object is not an image.", e);
        }
    }

    /**
     * Convert from DicomObjectI object to Pixelmed object.
     *
     * Has to stream to and from disk.
     *
     * @param dobj
     * @throws IOException
     * @throws MizerException
     */
    public AttributeList loadAttributes( DicomObjectI dobj) throws IOException, MizerException {
        AttributeList attributeList = new AttributeList();
        File tmpFile = Files.createTempFile("pe.", ".dcm").toFile();
        try (OutputStream os = new FileOutputStream(tmpFile)) {
            dobj.write(os);
        }

        DicomInputStream dis = null;
        try {
            dis = new DicomInputStream( new FileInputStream( tmpFile));
            attributeList.read( dis);

            return attributeList;
        } catch( DicomException e) {
            throw new MizerException( e);
        }
        finally {
            try {dis.close();} catch( IOException ignore) {}
            tmpFile.delete();
        }
    }

    public void update( File tmpFile, DicomObjectI dobj) throws IOException, DicomException, MizerException {
        try {
            dobj.deleteAllTags();
            dobj.read( tmpFile);
        }
        finally {
            tmpFile.delete();
        }
    }

    private int pickFillValue( SourceImage img, AttributeList attributes) {
        return (int) img.getMinimum();
    }

    @Override
    public String toString() {
        return "PixelmedPixelEditHandler{" +
                "dicomImageBlackout=" + dicomImageBlackout +
                '}';
    }
}
