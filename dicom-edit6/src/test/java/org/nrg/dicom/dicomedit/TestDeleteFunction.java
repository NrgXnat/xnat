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

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;

/**
 * Run tests of Delete function.
 *
 */
public class TestDeleteFunction {

    /**
     * Test script with attribute in sequence.
     *
     * @throws MizerException
     */
    @Test
    public void testSequenceTag() throws MizerException {

        String script =
                "delete[\"(0028,0010)[0]/(0020,0020)\"] \n";

        final DicomObjectI src_dobj = createSeqTestObject();
        src_dobj.putString(pn, "pn" );
        src_dobj.putString(pid, "pid" );
        src_dobj.putString(s0_1, "s0_1" );
        src_dobj.putString(s0_2, "s0_2" );

        assertEquals( "pn", src_dobj.getString(pn));
        assertEquals( "pid", src_dobj.getString(pid));
        assertEquals( "s0_1", src_dobj.getString(s0_1));
        assertEquals( "s0_2", src_dobj.getString(s0_2));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( "pn", src_dobj.getString(pn));
        assertEquals( "pid", src_dobj.getString(pid));
        assertEquals( "s0_1", src_dobj.getString(s0_1));
        assertNull( "s0_2", src_dobj.getString(s0_2));
    }

    /**
     * Test script with one public tag.
     *
     * @throws MizerException
     */
    @Test
    public void testSimpleTag() throws MizerException {

        String script =
                "delete[\"(0010,0010)\"] \n";

        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();
        src_dobj.putString(pn, "PatientName1");
        src_dobj.putString(pid, "PatientID2");

        assertEquals( "PatientName1", src_dobj.getString(pn));
        assertEquals( "PatientID2", src_dobj.getString(pid));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertNull( "MappedPatientName1", src_dobj.getString(pn));
        assertEquals( "PatientID2", src_dobj.getString(pid));
    }

    @Test
    public void testPrivateTag() throws MizerException {

        String script =
                "delete[\"(0009,1010)\"] \n";

        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();
        src_dobj.putString(pn, "pn");
        src_dobj.putString(pid, "pid");
        src_dobj.putString(pvtc, "pvtc");
        src_dobj.putString(pvt1, "pvt1");

        assertEquals( "pn", src_dobj.getString(pn));
        assertEquals( "pid", src_dobj.getString(pid));
        assertEquals( "pvtc", src_dobj.getString(pvtc));
        assertEquals( "pvt1", src_dobj.getString(pvt1));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( "pn", src_dobj.getString(pn));
        assertEquals( "pid", src_dobj.getString(pid));
        assertEquals( "pvtc", src_dobj.getString(pvtc));
        assertNull( "pvt1", src_dobj.getString(pvt1));
    }

    @Test
    public void testOrphanPrivateTag() throws MizerException {

        String script =
                "delete[\"(0009,1010)\"] \n";

        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();
        src_dobj.putString(pn, "pn");
        src_dobj.putString(pid, "pid");
        src_dobj.putString(pvt1, "pvt1");

        assertEquals( "pn", src_dobj.getString(pn));
        assertEquals( "pid", src_dobj.getString(pid));
        assertEquals( "pvt1", src_dobj.getString(pvt1));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( "pn", src_dobj.getString(pn));
        assertEquals( "pid", src_dobj.getString(pid));
        assertNull( "pvt1", src_dobj.getString(pvt1));
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

    private static int[] s0_1 = {0x00280010,0,0x00100010};
    private static int[] s0_2 = {0x00280010,0,0x00200020};

    private static int[] pvtc = {0x00090010};
    private static int[] pvt1 = {0x00091010};

}
