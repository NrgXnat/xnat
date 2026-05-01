package org.nrg.dicom.dicomedit.pixeledit.impl;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TransferSyntaxFromName;
import com.pixelmed.display.ImageEditUtilities;
import com.pixelmed.display.SourceImage;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;
import static org.junit.Assert.fail;

// Handy during dev.
public class PixelmedLibTest {

//    @Test
//    @Disabled("Loads DICOM files from local folders")
    public void anon() {
        String srcData = "/Users/drm/projects/nrg/dicomedit6/test-pixanon-us/nema_us/orig/view0002";
        String dstData = "/Users/drm/projects/nrg/dicomedit6/test-pixanon-us/nema_us/anon/view0002";
        process( srcData, dstData);
    }

    public void process( String srcData, String dstData ) {
        try {
            AttributeList attributeList = read( srcData);

            if( attributeList.isImage()) {
                SourceImage image = new SourceImage( attributeList);

                Rectangle2D rect = new Rectangle2D.Float(50, 50, 100, 50);
                Color color = new Color(128, 128, 128);

                edit( image, attributeList, rect, color);

                write( attributeList, dstData);
            }
            else {
                System.out.println("Skipping non-image dicom: " + srcData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e);
        }
    }

    public void edit( SourceImage image, AttributeList attributes, Rectangle2D rect, Color color) throws DicomException, IOException {
        Vector<Shape> shapes = new Vector<>();
        shapes.add( rect);
//        ImageEditUtilities.blackout( image, attributes, shapes);
        ImageEditUtilities.blackout( image, attributes, shapes, false, false, true, 0);
    }

    public AttributeList read( String fileString) throws IOException, DicomException {
        AttributeList attributeList = new AttributeList();
        DicomInputStream dis = null;
        try {
            dis = new DicomInputStream( new FileInputStream( fileString));
            attributeList.read( dis);

            return attributeList;
        }
        finally {
            try {dis.close();} catch( IOException ignore) {}
        }
    }

    public void write( AttributeList attributes, String dst) throws IOException, DicomException {
        File dstFile = new File( dst);
        File parentDir = dstFile.getParentFile();
        if( ! parentDir.exists()) {
            parentDir.mkdirs();
        }
//        String tsuid = Attribute.getSingleStringValueOrEmptyString( attributes, TagFromName.TransferSyntaxUID);
        TransferSyntaxFromName.getUID("ExplicitVRLittleEndian");
        String tsuid = Attribute.getSingleStringValueOrEmptyString( attributes, TagFromName.TransferSyntaxUID);
        attributes.write( dstFile, tsuid, true, true);
    }

}