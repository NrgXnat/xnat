/*
 * DicomEdit: TestConditionalStatements
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

import static org.junit.Assert.*;

/**
 * Run tests to delete Curve tags.
 *
 */
public class TestKillTheCurves {

    @Test
    public void testKillTheCurves() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        String statement = "-(50X@,XXXX) \n";

        int[] s1 = {0x50001000};
        src_dobj.putString(s1, "data");
        int[] s2 = {0x50002000};
        src_dobj.putString(s2, "1");
        int[] s3 = {0x50122000};
        src_dobj.putString(s3, "2");
        int[] s4 = {0x50126767 };
        src_dobj.putString(s4, "data");
        int[] s5 = {0x50030010};
        src_dobj.putString(s5, "test creator id");
        int[] s6 = {0x50031003};
        src_dobj.putString(s6, "private data");

        assertTrue(src_dobj.contains(0x50001000));
        assertTrue(src_dobj.contains(0x50002000));
        assertTrue(src_dobj.contains(0x50122000));
        assertTrue(src_dobj.contains(0x50126767));
        assertTrue(src_dobj.contains(0x50030010));
        assertTrue(src_dobj.contains(0x50031003));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(statement));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertFalse(src_dobj.contains(0x50001000));
        assertFalse(src_dobj.contains(0x50002000));
        assertFalse(src_dobj.contains(0x50122000));
        assertFalse(src_dobj.contains(0x50126767));
        assertTrue(src_dobj.contains(0x50030010));
        assertTrue(src_dobj.contains(0x50031003));
    }


    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }
}
