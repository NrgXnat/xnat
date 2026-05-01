/*
 * DicomEdit: TestFunctions
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.tags.TagPath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Run tests of TagPathFactory.
 *
 */
public class TestTagPathFactory {

    @Test
    public void testDE6SimpleTag() throws MizerException {

        final TagPath tp = TagPathFactory.createDE6Instance( "(0020,0020)");

        assertEquals( "00200020", tp.getPath());
    }

    @Test
    public void testDE6PrivateTag() throws MizerException {

        final TagPath tp = TagPathFactory.createDE6Instance( "(0029,{SIEMENS CSA}XX)");

        assertEquals( "0029\"SIEMENS CSA\"10XX", tp.getPath());
    }

    @Test
    public void testDE6SimpleSequenceTag() throws MizerException {

        final TagPath tp = TagPathFactory.createDE6Instance( "(0032,1024)/(0008,1032)");

        assertEquals( "00321024[%]/00081032", tp.getPath());
    }

    @Test
    public void testTagPathSimplePublic() throws MizerException {

        final int[] ia = TagPathFactory.createTagPathInstance("(0032,1024)");
        assertEquals( 1, ia.length);
        assertEquals( Integer.parseInt("00321024", 16), ia[0]);
    }

    @Test
    public void testTagPathSimplePublicHex() throws MizerException {

        final int[] ia = TagPathFactory.createTagPathInstance("(0C32,002f)");
        assertEquals( 1, ia.length);
        assertEquals( Integer.parseInt("0C32002F", 16), ia[0]);
    }

    @Test
    public void testTagPathSimplePrivate() throws MizerException {

        final int[] ia = TagPathFactory.createTagPathInstance("(0029,1024)");
        assertEquals( 1, ia.length);
        assertEquals( Integer.parseInt("00291024", 16), ia[0]);
    }

    @Test
    public void testTagPathSequence() throws MizerException {

        final int[] ia = TagPathFactory.createTagPathInstance("(0032,1024)[100]/(0008,1032)");
        assertEquals( 3, ia.length);
        assertEquals( Integer.parseInt("00321024", 16), ia[0]);
        assertEquals( 100, ia[1]);
        assertEquals( Integer.parseInt("00081032", 16), ia[2]);
    }

    @Test
    public void testTagPathSpaces() throws MizerException {

        final int[] ia = TagPathFactory.createTagPathInstance(" ( 0032 , 1024 ) [ 100 ] / ( 0008 , 1032 ) ");
        assertEquals( 3, ia.length);
        assertEquals( Integer.parseInt("00321024", 16), ia[0]);
        assertEquals( 100, ia[1]);
        assertEquals( Integer.parseInt("00081032", 16), ia[2]);
    }

    @Test
    public void testTagPathMissingItemNumber() throws MizerException {

        try {
            final int[] ia = TagPathFactory.createTagPathInstance("(0032,1024)(0008,1032)");
            fail("Failed to throw exception.");
        }
        catch (Exception e) {
            if( ! e.getMessage().contains("Failed to parse")) {
                fail("Unexpected exception: " + e);
            }
        }
    }

    @Test
    public void testTagPathValidHexWordItemNumber() throws MizerException {

        final int[] ia = TagPathFactory.createTagPathInstance("(0032,1024)[2222]/(0008,1032)");
        assertEquals( 3, ia.length);
        assertEquals( Integer.parseInt("00321024", 16), ia[0]);
        assertEquals( 2222, ia[1]);
        assertEquals( Integer.parseInt("00081032", 16), ia[2]);
    }

    @Test
    public void testTagPathInvalidHexItemNumber() throws MizerException {

        try {
            final int[] ia = TagPathFactory.createTagPathInstance("(0032,1024)[022A]/(0008,1032)");
            fail("Failed to throw exception.");
        }
        catch (Exception e) {
            if( ! (e instanceof NumberFormatException)) {
                fail("Unexpected exception: " + e);
            }
        }
    }

}
