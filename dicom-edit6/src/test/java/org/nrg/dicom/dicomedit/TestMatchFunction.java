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

import static org.junit.Assert.*;

/**
 * Run tests of Match function.
 *
 */
public class TestMatchFunction {

    @Test
    public void testSimpleMatch() throws MizerException {
        String script = "(0008,103E) := match[(0010,0010), \"Name\", 0] \n";

        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        src_dobj.putString(patientName, "PatientName");

        assertEquals( "PatientName", src_dobj.getString( patientName));
        assertNull(  src_dobj.getString( seriesDescription));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( "Name", result_dobj.getString(seriesDescription));
    }

    @Test
    public void testSimpleMisMatch() throws MizerException {
        String script = "(0008,103E) := match[(0010,0010), \"name\", 0] \n";

        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        src_dobj.putString(patientName, "PatientName");

        assertEquals( "PatientName", src_dobj.getString( patientName));
        assertNull(  src_dobj.getString( seriesDescription));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertNotEquals( "name", result_dobj.getString(seriesDescription));
        assertNull(  src_dobj.getString( seriesDescription));
    }

    @Test
    public void testMatchAndConcatenate() {
        try {
//            String script = "(0010,0030) := match[(0010,0030), \"\\\\d{4}\", 0] \n";
//            String script = "(0010,0030) := concatenate[ \"1960\", \"0101\"] \n";
            String script = "(0010,0030) := concatenate[ match[(0010,0030), \"\\\\d{4}\", 0], \"0101\"] \n";

            final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

            src_dobj.putString(dateOfBirth, "19600519");

            assertEquals("19600519", src_dobj.getString(dateOfBirth));

            final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
            final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

//            assertEquals("1960", src_dobj.getString(dateOfBirth));
//            assertEquals("19600101", src_dobj.getString(dateOfBirth));
            assertEquals("19600101", result_dobj.getString(dateOfBirth));
        }
        catch ( MizerException e) {
            fail("Unexpected exception: " + e);
        }
    }

    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    private static int[] patientName = {0x00100010};
    private static int[] dateOfBirth = {0x00100030};

    private static int[] seriesDescription = {0x0008103E};
}
