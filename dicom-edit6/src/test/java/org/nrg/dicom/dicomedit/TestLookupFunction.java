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
import org.nrg.dicom.dicomedit.functions.LookupManager;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Run tests of hashReferencedUIDs function.
 *
 */
public class TestLookupFunction {

//    private static final ResourceManager _resourceManager = ResourceManager.getInstance();
//    private static final File FILE4 = _resourceManager.getTestResourceFile("dicom/1.2.840.113717.2.21733635.1-3-2-7b04bw.dcm");
//    private static final File FILE4 = _resourceManager.getTestResourceFile("short.dcm");

    /**
     * Test script with lookup.
     *
     * @throws MizerException
     */
    @Test
    public void testScriptWithLookup() throws MizerException {

        String script =
             "(0010,0010) := lookup[ \"pn\", (0010,0010) ] \n"
                     + "(0010,0020) := lookup[ \"pid\", (0010,0020)]";

        String[] lookupTable = {
                " // Test lookup table.",
                "",
                "pn/PatientName1 = MappedPatientName1",
                "pid/PatientID1 = MappedPatientID1",
                "pn/PatientName2 = MappedPatientName2",
                "pid/PatientID2 = MappedPatientID2",
        };

        final DicomObjectI src_dobj = createSeqTestObject();

        assertEquals( "PatientName1", src_dobj.getString(pn));
        assertEquals( "PatientID2", src_dobj.getString(pid));

        try {
            LookupManager.getInstance().load(lookupTable);
        }
        catch (IOException e) {
            fail("Unexpected exception.");
        }

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( "MappedPatientName1", src_dobj.getString(pn));
        assertEquals( "MappedPatientID2", src_dobj.getString(pid));

    }


    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    private DicomObjectI createSeqTestObject() {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        src_dobj.putString(pn, "PatientName1");
        src_dobj.putString(pid, "PatientID2");

        return src_dobj;

    }

    private static int[] pn = {0x00100010};
    private static int[] pid = {0x00100020};

}
