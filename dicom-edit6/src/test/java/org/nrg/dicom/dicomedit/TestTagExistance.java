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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Run tests of conditional statements using 'match anything' regex.
 *
 * Null values don't match anything, including null.
 * This can be used to test for the existence of a tag
 *
 * (0010,0010) !~ ".*"? (0010,0010) := "value"
 * will set (0010,0010) only if it does not exist.
 *
 */
public class TestTagExistance {


    @Test
    public void testMatchesAnythingRegex() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        String statement = "(0008,0080) ~ \".*\" ? (0008,0080) := \"New Institute\" \n"
                + "(0010,0010) ~ \".*\" ? (0010,0010) := \"New Value\" \n";
        int[] s = {0x00080080};
        src_dobj.putString(s, "Some Institution");
        int[] s2 = {0x00100010};
        src_dobj.putString(s2, "");

        assertTrue(src_dobj.contains(0x00080080));
        assertEquals(src_dobj.getString(s), "Some Institution");
        assertTrue(src_dobj.contains(0x00100010));
        assertEquals(src_dobj.getString(s2), "");

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(statement));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals(result_dobj.getString(0x00080080), "New Institute");
        assertEquals(result_dobj.getString(0x00100010), "New Value");
    }

    @Test
    public void testNotMatchesAnythingRegex() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        String statement = "(0008,0080) !~ \"Some Institution\" ? (0008,0080) := \"New Institute\" \n"
                + "(0010,0010) !~ \".*\" ? (0010,0010) := \"New Value\" \n";
        int[] s = {0x00080080};
        src_dobj.putString(s, "Some Institution");

        assertTrue(src_dobj.contains(0x00080080));
        assertEquals(src_dobj.getString(s), "Some Institution");
        assertFalse(src_dobj.contains(0x00100010));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(statement));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals(result_dobj.getString(0x00080080), "Some Institution");
        assertEquals(result_dobj.getString(0x00100010), "New Value");
    }

    @Test
    public void testNullDoesNotMatchAnything() throws MizerException {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        String statement = "(0020,0020) ~ (0080,0080) ? (0008,0080) := \"New Institute\" \n"
                + "(0020,0020) ~ (0010,0010) ? (0010,0010) := \"New Value\" \n"
                + "(0020,0020) ~ (0012,0012) ? (0012,0012) := \"New Value\" \n";
        int[] s = {0x00080080};
        src_dobj.putString(s, "Some Institution");
        int[] s2 = {0x00100010};
        src_dobj.putString(s2, "");

        assertTrue(src_dobj.contains(0x00080080));
        assertEquals(src_dobj.getString(s), "Some Institution");
        assertTrue(src_dobj.contains(0x00100010));
        assertEquals(src_dobj.getString(s2), "");
        assertFalse(src_dobj.contains(0x00200020));
        assertFalse(src_dobj.contains(0x00120012));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(statement));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals(result_dobj.getString(0x00080080), "Some Institution");
        assertEquals(result_dobj.getString(0x00100010), "");
        assertFalse(src_dobj.contains(0x00120012));
    }


    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }
}
