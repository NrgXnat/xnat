package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.io.ByteArrayInputStream;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

/**
 * Tests of the MapUIDFunction.
 *
 * MapUIDFunction takes two arguments:
 * 1. the tag from which it gets a value
 * 2. a string prefix that will be prepended to a hash generated from the tagvalue.
 *
 * This function keeps state of previous values it has encountered and is guaranteed to return the same mapped value
 * for previously encountered values.
 *
 */
public class TestMapUIDFunction {

    /**
     * Test mapUID function on simple tags.
     *
     * @throws MizerException
     */
    @Test
    public void testSimpleTags() throws MizerException {

        String script =
                "(0020,000D) := mapUID[ (0020,000D), \"1.666\"]\n" +
                        "(0020,000E) := mapUID[ (0020,000E), \"1.666\"]";

        final DicomObjectI src_dobj1 = createTestObject();

        assertEquals( "PatientName", src_dobj1.getString(patientName));
        assertEquals( "20120919", src_dobj1.getString(studyDate));
        assertEquals( "20131231", src_dobj1.getString(seriesDate));
        assertEquals( "1.2.3.4", src_dobj1.getString(studyInstanceUID));
        assertEquals( "1.2.3.4.1", src_dobj1.getString(seriesInstanceUID));

        final DicomObjectI src_dobj2 = createTestObject();

        assertEquals( "PatientName", src_dobj2.getString(patientName));
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
        assertTrue( newStudyInstanceUID.startsWith("1.666."));

        final DicomObjectI result_dobj2 = sa.apply(src_dobj2).getDicomObject();

        assertEquals( newStudyInstanceUID, result_dobj2.getString(studyInstanceUID));
        assertEquals( newSeriesInstanceUID, result_dobj2.getString(seriesInstanceUID));

    }


    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    private DicomObjectI createTestObject() {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        src_dobj.putString(patientName, "PatientName");

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

    /**
     * Happily generate UID for value == null.
     */
    @Test
    public void testNullValue() {
        try {
            final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

            String script = "(0004,1511) := mapUID[ (0004,1511), \"1.666\"]\n" ;

            assertNull(  src_dobj.getString(0x00041511));

            final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
            final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();
            assertTrue( result_dobj.getString( 0x00041511).startsWith("1.666."));
        }
        catch( Exception e) {
            fail( "Unexpected Exception: " + e.getMessage());
        }
    }

    /**
     * Happily generate UID for value == "".
     */
    @Test
    public void testEmptyValue() {
        try {
            final DicomObjectI src_dobj = DicomObjectFactory.newInstance();
            src_dobj.putString( 0x00041511, "");

            String script = "(0004,1511) := mapUID[ (0004,1511), \"1.666\"]\n" ;

            assertEquals(  "", src_dobj.getString(0x00041511));

            final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
            final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();
            assertTrue( result_dobj.getString( 0x00041511).startsWith("1.666."));
        }
        catch( Exception e) {
            fail( "Unexpected Exception: " + e.getMessage());
        }
    }

}
