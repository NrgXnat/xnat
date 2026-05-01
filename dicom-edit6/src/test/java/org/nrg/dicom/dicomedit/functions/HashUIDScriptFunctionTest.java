package org.nrg.dicom.dicomedit.functions;

import org.junit.Test;
import org.nrg.dicom.dicomedit.BaseScriptApplicator;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class HashUIDScriptFunctionTest {

    /**
     * Test mapUID function on simple tags.
     *
     * @throws MizerException
     */
    @Test
    public void testSimpleTags() throws MizerException {

        String script =
                "(0020,000D) := hashUID[ (0020,000D)]\n" +
                        "(0020,000E) := hashUID[ (0020,000E)]";

        final DicomObjectI src_dobj1 = createTestObject();

        assertEquals( "Patient^Name", src_dobj1.getString(patientName));
        assertEquals( "20120919", src_dobj1.getString(studyDate));
        assertEquals( "20131231", src_dobj1.getString(seriesDate));
        assertEquals( "1.2.3.4", src_dobj1.getString(studyInstanceUID));
        assertEquals( "1.2.3.4.1", src_dobj1.getString(seriesInstanceUID));

        final DicomObjectI src_dobj2 = createTestObject();

        assertEquals( "Patient^Name", src_dobj2.getString(patientName));
        assertEquals( "20120919", src_dobj2.getString(studyDate));
        assertEquals( "20131231", src_dobj2.getString(seriesDate));
        assertEquals( "1.2.3.4", src_dobj2.getString(studyInstanceUID));
        assertEquals( "1.2.3.4.1", src_dobj2.getString(seriesInstanceUID));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        final DicomObjectI result_dobj1 = sa.apply(src_dobj1).getDicomObject();

        String newStudyInstanceUID = result_dobj1.getString(studyInstanceUID);
        String newSeriesInstanceUID = result_dobj1.getString(seriesInstanceUID);

        assertNotEquals( "1.2.3.4", newStudyInstanceUID);
        assertNotEquals( "1.2.3.4.1", newSeriesInstanceUID);

        final DicomObjectI result_dobj2 = sa.apply(src_dobj2).getDicomObject();

        assertEquals( newStudyInstanceUID, result_dobj2.getString(studyInstanceUID));
        assertEquals( newSeriesInstanceUID, result_dobj2.getString(seriesInstanceUID));

    }


    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    private DicomObjectI createTestObject() {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        src_dobj.putString(patientName, "Patient^Name");

        src_dobj.putString(studyDate, "20120919");
        src_dobj.putString(seriesDate, "20131231");
        src_dobj.putString(studyInstanceUID, "1.2.3.4");
        src_dobj.putString(seriesInstanceUID, "1.2.3.4.1");

        return src_dobj;
    }

    private static int[] patientName = {0x00100010};
    private static int[] studyDate = {0x00080020};
    private static int[] seriesDate = {0x00080021};
    private static int[] studyInstanceUID = {0x0020000D};
    private static int[] seriesInstanceUID = {0x0020000E};

}