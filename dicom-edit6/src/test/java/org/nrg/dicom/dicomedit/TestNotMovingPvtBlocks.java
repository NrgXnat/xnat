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

/**
 * Run tests for issue DE-19 (moving private elements).
 *
 */
public class TestNotMovingPvtBlocks {

    /**
     * Test for DE19. Do-nothing script should not result in private elements moving.
     *
     * @throws MizerException
     */
    @Test
    public void testDE19() throws MizerException {

        String script = 
                "version \"6.1\"\n" +
                "// test";

        final DicomObjectI src_dobj = createTestObject();

        assertEquals( "MR", src_dobj.getString(0x00080060));
        assertEquals( "Test ID 1", src_dobj.getString(0x00210010));
        assertEquals( "Test ID 2", src_dobj.getString(0x00210020));
        assertEquals( "Test_Val", src_dobj.getString(0x00211005));
        assertEquals( "Test_Val2", src_dobj.getString(0x00212050));
        assertFalse( src_dobj.contains(0x00210011));
        assertFalse( src_dobj.contains(0x00211150));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( "MR", result_dobj.getString(0x00080060));
        assertEquals( "Test ID 1", result_dobj.getString(0x00210010));
        assertEquals( "Test ID 2", result_dobj.getString(0x00210020));
        assertEquals( "Test_Val", result_dobj.getString(0x00211005));
        assertEquals( "Test_Val2", result_dobj.getString(0x00212050));
        assertFalse( result_dobj.contains(0x00210011));
        assertFalse( result_dobj.contains(0x00211150));
    }


    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    private DicomObjectI createTestObject() {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        src_dobj.putString(0x00080016, "1.2.840.10008.5.1.4.1.1.4");
        src_dobj.putString(0x00080018, "1.2.3.4.5.6.7.8.9");
        src_dobj.putString(0x00080060, "MR");
        src_dobj.putString(0x0008103E, "testdata");
        src_dobj.putString(0x00100010, "Moore^Charlie");
        src_dobj.putString(0x00100020, "CJM");
        src_dobj.putString(0x0020000D, "1.2.3.4.5.6.7");
        src_dobj.putString(0x0020000E, "1.2.3.4.5.6.7.8");
        src_dobj.putString(0x00210010, "Test ID 1");
        src_dobj.putString(0x00210020, "Test ID 2");
        src_dobj.putString(0x00211005, "Test_Val");
        src_dobj.putString(0x00212050, "Test_Val2");

        return src_dobj;
    }

}
