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
public class TestIsMatchFunction {

    @Test
    public void testSimpleMatch() throws MizerException {
        String script = "(0008,103E) := ismatch[(0010,0010), \"[a-zA-Z]+\"] \n";

        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        src_dobj.putString(patientName, "PatientName");

        assertEquals( "PatientName", src_dobj.getString( patientName));
        assertNull(  src_dobj.getString( seriesDescription));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( "true", result_dobj.getString(seriesDescription));
    }

    @Test
    public void testSimpleMisMatch() throws MizerException {
        String script = "(0008,103E) := ismatch[(0010,0010), \"[0-9]+\"] \n";

        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        src_dobj.putString(patientName, "PatientName");

        assertEquals( "PatientName", src_dobj.getString( patientName));
        assertNull(  src_dobj.getString( seriesDescription));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( "false", result_dobj.getString(seriesDescription));
    }

    @Test
    public void testConditionalMatch() throws MizerException {
//        String script = "ismatch[(0010,0010), \"[a-zA-Z]+\"] ? (0010,0010) := \"MATCHED\"\n";
        String script = "(0010,0010) ~ \"[a-zA-Z]+\" ? (0010,0010) := \"MATCHED\"\n";

        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        src_dobj.putString(patientName, "PatientName");

        assertEquals( "PatientName", src_dobj.getString( patientName));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes( script));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( "MATCHED", result_dobj.getString(0x00100010));
    }

    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    private static int[] patientName = {0x00100010};

    private static int[] seriesDescription = {0x0008103E};
}
