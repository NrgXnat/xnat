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
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Run tests of SerialScriptApplicator.
 *
 */
public class TestSerialScriptApplicator {

//    private static final ResourceManager _resourceManager = ResourceManager.getInstance();
//    private static final File FILE4 = _resourceManager.getTestResourceFile("dicom/1.2.840.113717.2.21733635.1-3-2-7b04bw.dcm");
//    private static final File FILE4 = _resourceManager.getTestResourceFile("short.dcm");

    @Test
    public void testOneScriptWithNoLookup() {
        String scriptString =
                "(0010,0010) := \"NewPatientName\" \n"
                        + "(0010,0020) := \"NewPatientID\"";
        DE6Script script = null;
        try {
            script = new DE6Script.Builder().statement(scriptString).build();

            List<DE6Script> scripts = new ArrayList<>();
            scripts.add( script);

            final DicomObjectI src_dobj = createSeqTestObject();

            assertEquals( "PatientName1", src_dobj.getString(pn));
            assertEquals( "PatientID2", src_dobj.getString(pid));

            final SerialScriptApplicator sa = new SerialScriptApplicator( scripts);
            final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

            assertEquals( "NewPatientName", src_dobj.getString(pn));
            assertEquals( "NewPatientID", src_dobj.getString(pid));

        } catch (MizerException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testTwoScriptWithNoLookup() {
        String scriptString1 =
                "(0010,0010) := \"NewPatientName1\" \n"
                        + "(0010,0020) := \"NewPatientID1\"";

        String scriptString2 =
                "(0010,0010) := \"NewPatientName2\" \n"
                        + "(0010,0020) := \"NewPatientID2\"";

        try {
            List<DE6Script> scripts = new ArrayList<>();

            DE6Script script = new DE6Script.Builder().statement(scriptString1).build();
            scripts.add( script);
            script = new DE6Script.Builder().statement(scriptString2).build();
            scripts.add( script);

            final DicomObjectI src_dobj = createSeqTestObject();

            assertEquals( "PatientName1", src_dobj.getString(pn));
            assertEquals( "PatientID2", src_dobj.getString(pid));

            final SerialScriptApplicator sa = new SerialScriptApplicator( scripts);
            final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

            assertEquals( "NewPatientName2", src_dobj.getString(pn));
            assertEquals( "NewPatientID2", src_dobj.getString(pid));

        } catch (MizerException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    /**
     * Test script with lookup.
     *
     * @throws MizerException
     */
    @Test
    public void testOneScriptWithLookup() throws MizerException {

        String scriptString =
             "(0010,0010) := lookup[ \"pn\", (0010,0010) ] \n"
                     + "(0010,0020) := lookup[ \"pid\", (0010,0020)]";

        String lookupTable =
                " // Test lookup table.\n" +
                "\n" +
                "pn/PatientName1 = MappedPatientName1\n" +
                "pid/PatientID1 = MappedPatientID1\n" +
                "pn/PatientName2 = MappedPatientName2\n" +
                "pid/PatientID2 = MappedPatientID2\n";

        try {
            DE6Script script = new DE6Script.Builder().statements( bytes(scriptString)).lookupTable( bytes(lookupTable)).build();

            List<DE6Script> scripts = new ArrayList<>();
            scripts.add( script);

            final DicomObjectI src_dobj = createSeqTestObject();

            assertEquals( "PatientName1", src_dobj.getString(pn));
            assertEquals( "PatientID2", src_dobj.getString(pid));

            final SerialScriptApplicator sa = new SerialScriptApplicator( scripts);
            final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

            assertEquals( "MappedPatientName1", src_dobj.getString(pn));
            assertEquals( "MappedPatientID2", src_dobj.getString(pid));

        } catch (MizerException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testTwoScriptWithLookup() throws MizerException {

        String scriptString1 =
                "(0010,0010) := lookup[ \"pn\", (0010,0010) ] \n"
                        + "(0010,0020) := lookup[ \"pid\", (0010,0020)]";

        String lookupTable1 =
                " // Test lookup table.\n" +
                        "\n" +
                        "pn/PatientName1 = MappedPatientName1\n" +
                        "pid/PatientID1 = MappedPatientID1\n" +
                        "pn/PatientName2 = MappedPatientName2\n" +
                        "pid/PatientID2 = MappedPatientID2\n";

        String scriptString2 =
                "(0010,0010) := lookup[ \"pn\", (0010,0010) ] \n"
                        + "(0010,0020) := lookup[ \"pid\", (0010,0020)]";

        String lookupTable2 =
                " // Test lookup table.\n" +
                        "\n" +
                        "pn/MappedPatientName1 = Mapped2PatientName1\n" +
                        "pid/MappedPatientID1 = Mapped2PatientID1\n" +
                        "pn/MappedPatientName2 = Mapped2PatientName2\n" +
                        "pid/MappedPatientID2 = Mapped2PatientID2\n";

        try {
            List<DE6Script> scripts = new ArrayList<>();

            DE6Script script = new DE6Script.Builder()
                    .statements( bytes(scriptString1))
                    .lookupTable(bytes(lookupTable1))
                    .build();
            scripts.add( script);

            script = new DE6Script.Builder()
                    .statements( bytes(scriptString2))
                    .lookupTable( bytes(lookupTable2))
                    .build();
            scripts.add( script);

            final DicomObjectI src_dobj = createSeqTestObject();

            assertEquals( "PatientName1", src_dobj.getString(pn));
            assertEquals( "PatientID2", src_dobj.getString(pid));

            final SerialScriptApplicator sa = new SerialScriptApplicator( scripts);
            final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

            assertEquals( "Mapped2PatientName1", src_dobj.getString(pn));
            assertEquals( "Mapped2PatientID2", src_dobj.getString(pid));

        } catch (MizerException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
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
