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
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Run tests for issue DE-11 (not removing elements in sequences: '*' wild card).
 *
 */
public class TestDeleteEverywhere {

    /**
     * Test for DE11.
     *
     * @throws MizerException
     */
    @Test
    public void testDE11() throws MizerException {

        String script = 
                "version \"6.1\"\n" +
                "-*/(0008,0100)";

        final DicomObjectI src_dobj = createTestObject();

        assertTrue( src_dobj.contains(0x00080100));
        assertTrue( src_dobj.contains(s00));
        assertEquals( "MR", src_dobj.getString(0x00080060));
        assertEquals( "toplevel", src_dobj.getString(0x00080100));
        assertEquals( "value0", src_dobj.getString(s00));
        assertEquals( "designator0", src_dobj.getString(s01));
        assertEquals( "version0", src_dobj.getString(s02));
        assertEquals( "meaning0", src_dobj.getString(s03));
        assertEquals( "value1", src_dobj.getString(s10));
        assertEquals( "designator1", src_dobj.getString(s11));
        assertEquals( "version1", src_dobj.getString(s12));
        assertEquals( "meaning1", src_dobj.getString(s13));
        assertEquals( "value0", src_dobj.getString(p00));
        assertEquals( "designator0", src_dobj.getString(p01));
        assertEquals( "version0", src_dobj.getString(p02));
        assertEquals( "meaning0", src_dobj.getString(p03));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( "MR", src_dobj.getString(0x00080060));
        assertFalse( src_dobj.contains(0x00080100));
        assertFalse( src_dobj.contains(s00));
        assertEquals( "designator0", src_dobj.getString(s01));
        assertEquals( "version0", src_dobj.getString(s02));
        assertEquals( "meaning0", src_dobj.getString(s03));
        assertFalse( src_dobj.contains(s10));
        assertEquals( "designator1", src_dobj.getString(s11));
        assertEquals( "version1", src_dobj.getString(s12));
        assertEquals( "meaning1", src_dobj.getString(s13));
        assertFalse( src_dobj.contains(p00));
        assertEquals( "designator0", src_dobj.getString(p01));
        assertEquals( "version0", src_dobj.getString(p02));
        assertEquals( "meaning0", src_dobj.getString(p03));
    }


    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    private DicomObjectI createTestObject() {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        src_dobj.putString(0x00080016, "1.2.840.10008.5.1.4.1.1.4");
        src_dobj.putString(0x00080018, "1.2.3.4.5.6.7.8.9");
        src_dobj.putString(0x00080100, "toplevel");
        src_dobj.putString(0x00080060, "MR");
        src_dobj.putString(0x0008103E, "testdata");
        src_dobj.putString(0x00100010, "Moore^Charlie");
        src_dobj.putString(0x00100020, "CJM");
        src_dobj.putString( s00, "value0");
        src_dobj.putString( s01, "designator0");
        src_dobj.putString( s02, "version0");
        src_dobj.putString( s03, "meaning0");
        src_dobj.putString( s10, "value1");
        src_dobj.putString( s11, "designator1");
        src_dobj.putString( s12, "version1");
        src_dobj.putString( s13, "meaning1");

        src_dobj.putString( p0, "PVT ID");
        src_dobj.putString( p00, "value0");
        src_dobj.putString( p01, "designator0");
        src_dobj.putString( p02, "version0");
        src_dobj.putString( p03, "meaning0");

        return src_dobj;
    }

    private int[] s00 = {0x00120064,0,0x00080100};
    private int[] s01 = {0x00120064,0,0x00080102};
    private int[] s02 = {0x00120064,0,0x00080103};
    private int[] s03 = {0x00120064,0,0x00080104};
    private int[] s10 = {0x00120064,1,0x00080100};
    private int[] s11 = {0x00120064,1,0x00080102};
    private int[] s12 = {0x00120064,1,0x00080103};
    private int[] s13 = {0x00120064,1,0x00080104};

    private int[] p0 = {0x20050014};
    private int[] p00 = {0x20051402,0,0x00080100};
    private int[] p01 = {0x20051402,0,0x00080102};
    private int[] p02 = {0x20051402,0,0x00080103};
    private int[] p03 = {0x20051402,0,0x00080104};
}
