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
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultError;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.dicomedit.TestUtils.*;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;
import static org.nrg.dicom.dicomedit.TestUtils.put;

/**
 * Run tests of shiftDateTimeByIncrement function.
 *
 */
public class TestShiftDateTimeByIncrementFunction {

    /**
     * Test date shift in simple tags.
     *
     * @throws MizerException
     */
    @Test
    public void testSimpleTags() throws MizerException {

        String script =
                "(0008,002A) := shiftDateTimeByIncrement[ (0008,002A), \"600\", \"seconds\"]\n" +
                        "(0018,9074) := shiftDateTimeByIncrement[ (0018,9074), \"-600\", \"seconds\"]";

        SimpleDateFormat df = new SimpleDateFormat( "yyyyMMddHHmmss.S");

        Long increment = 600000l;
        Date dateTime = new Date();
        Date dateTimeShiftedUp = new Date( dateTime.getTime() + increment);
        Date dateTimeShiftedDown = new Date( dateTime.getTime() - increment);

        final DicomObjectI src_dobj = createSimpleTestObject( df.format( dateTime));

        assertEquals( "PatientName", src_dobj.getString(patientName));

        assertEquals( df.format( dateTime), src_dobj.getString(acquisitionDateTime));
        assertEquals( df.format( dateTime), src_dobj.getString(frameAcquisitionDateTime));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( "PatientName", src_dobj.getString(patientName));

        assertEquals( df.format( dateTimeShiftedUp), result_dobj.getString(acquisitionDateTime));
        assertEquals( df.format( dateTimeShiftedDown), result_dobj.getString(frameAcquisitionDateTime));
    }

    @Test
    public void testIntegerShift() throws MizerException {

        String script =
                "(0008,002A) := shiftDateTimeByIncrement[ (0008,002A), 600, \"seconds\"]\n" +
                        "(0018,9074) := shiftDateTimeByIncrement[ (0018,9074), -600, \"seconds\"]";

        SimpleDateFormat df = new SimpleDateFormat( "yyyyMMddHHmmss.S");

        Long increment = 600000l;
        Date dateTime = new Date();
        Date dateTimeShiftedUp = new Date( dateTime.getTime() + increment);
        Date dateTimeShiftedDown = new Date( dateTime.getTime() - increment);

        DicomObjectI dobj = createSimpleTestObject( df.format( dateTime));

        assertEquals( "PatientName", dobj.getString(patientName));

        assertEquals( df.format( dateTime), dobj.getString(acquisitionDateTime));
        assertEquals( df.format( dateTime), dobj.getString(frameAcquisitionDateTime));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals( "PatientName", dobj.getString(patientName));

        assertEquals( df.format( dateTimeShiftedUp), dobj.getString(acquisitionDateTime));
        assertEquals( df.format( dateTimeShiftedDown), dobj.getString(frameAcquisitionDateTime));
    }

    @Test
    public void testShiftUnits() throws MizerException {
        TestTag pn = new TestTag(0x00100010, "pn");
        TestTag t1 = new TestTag( 0x0008002A, "20240115131415", "20240121131415");
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put(dobj,pn);
        put(dobj,t1);

        String script = "(0008,002A) := shiftDateTimeByIncrement[ (0008,002A), 6, \"days\"]\n";

        SimpleDateFormat df = new SimpleDateFormat( "yyyyMMddHHmmss.S");

        assertEquals( pn.initialValue, dobj.getString(pn.tag));
        assertEquals( t1.initialValue, dobj.getString(t1.tag));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals( pn.postValue, dobj.getString(pn.tag));
        assertEquals( t1.postValue, dobj.getString(t1.tag));
    }

    @Test
    public void testSequenceTags() throws MizerException {

        String script =
                "shiftDateTimeSequenceByIncrement[ \"600\", \"(5200,9230)/(0020,9111)/(0018,9074)\"]\n" +
                        "shiftDateTimeSequenceByIncrement[ \"-600\", \"(5200,9230)/(0020,9111)/(0018,9151)\"]";

        SimpleDateFormat df = new SimpleDateFormat( "yyyyMMddHHmmss.S");

        Long increment = 600000l;
        Date dateTime = new Date();
        Date dateTimeShiftedUp = new Date( dateTime.getTime() + increment);
        Date dateTimeShiftedDown = new Date( dateTime.getTime() - increment);

        DicomObjectI dobj = createSequenceTestObject( df.format( dateTime));

        assertEquals( "PatientName", dobj.getString(patientName));

        assertEquals( df.format( dateTime), dobj.getString(p00));
        assertEquals( df.format( dateTime), dobj.getString(p01));
        assertEquals( df.format( dateTime), dobj.getString(p10));
        assertEquals( df.format( dateTime), dobj.getString(p11));

        assertEquals( df.format( dateTime), dobj.getString(q00));
        assertEquals( df.format( dateTime), dobj.getString(q01));
        assertEquals( df.format( dateTime), dobj.getString(q10));
        assertEquals( df.format( dateTime), dobj.getString(q11));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals( "PatientName", dobj.getString(patientName));

        assertEquals( df.format( dateTimeShiftedUp), dobj.getString(p00));
        assertEquals( df.format( dateTimeShiftedUp), dobj.getString(p01));
        assertEquals( df.format( dateTimeShiftedUp), dobj.getString(p10));
        assertEquals( df.format( dateTimeShiftedUp), dobj.getString(p11));

        assertEquals( df.format( dateTimeShiftedDown), dobj.getString(q00));
        assertEquals( df.format( dateTimeShiftedDown), dobj.getString(q01));
        assertEquals( df.format( dateTimeShiftedDown), dobj.getString(q10));
        assertEquals( df.format( dateTimeShiftedDown), dobj.getString(q11));
    }

    /**
     * Tagpaths can have wildcards.
     * Inspired by DE-97
     */
    @Test
    public void testSequenceWildcardTags() throws MizerException {

        String script = "shiftDateTimeSequenceByIncrement[ \"600\", \"+/(0018,9151)\"]\n";

        SimpleDateFormat df = new SimpleDateFormat( "yyyyMMddHHmmss.S");

        Long offset = 323232l;     // some random value to offset starting DTs.
        Long increment = 600000l;  // the time increment to apply to DTs.
        Date dateTime1 = new Date();
        Date dateTime2 = new Date( dateTime1.getTime() + offset);
        Date dateTime1_ShiftedUp = new Date( dateTime1.getTime() + increment);
        Date dateTime2_ShiftedUp = new Date( dateTime2.getTime() + increment);

        // tag at top level not shifted.
        TestTag    t1      = new TestTag(0x00189151, df.format( dateTime1));
        TestSeqTag t1_0_t1 = new TestSeqTag(new int[]{0x00420010,0,0x00189151}, df.format( dateTime1), df.format( dateTime1_ShiftedUp));
        TestSeqTag t1_1_t1 = new TestSeqTag(new int[]{0x00420010,1,0x00189151}, df.format( dateTime2), df.format( dateTime2_ShiftedUp));
        TestSeqTag t1_0_t2 = new TestSeqTag(new int[]{0x00420010,0,0x00100010}, "t1_0_t2");
        TestSeqTag t1_1_t2 = new TestSeqTag(new int[]{0x00420010,1,0x00100010}, "t1_1_t2");

        DicomObjectI dobj = DicomObjectFactory.newInstance();

        put( dobj, t1);
        put( dobj, t1_0_t1);
        put( dobj, t1_1_t1);
        put( dobj, t1_0_t2);
        put( dobj, t1_1_t2);

        assertEquals( t1.initialValue, dobj.getString( t1.tag));
        assertEquals( t1_0_t1.initialValue, dobj.getString( t1_0_t1.tag));
        assertEquals( t1_1_t1.initialValue, dobj.getString( t1_1_t1.tag));
        assertEquals( t1_0_t2.initialValue, dobj.getString( t1_0_t2.tag));
        assertEquals( t1_1_t2.initialValue, dobj.getString( t1_1_t2.tag));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals( t1.postValue, dobj.getString( t1.tag));
        assertEquals( t1_0_t1.postValue, dobj.getString( t1_0_t1.tag));
        assertEquals( t1_1_t1.postValue, dobj.getString( t1_1_t1.tag));
        assertEquals( t1_0_t2.postValue, dobj.getString( t1_0_t2.tag));
        assertEquals( t1_1_t2.postValue, dobj.getString( t1_1_t2.tag));
    }

    /**
     * Inspired by DE-40
     * Note precision is rounded to milliseconds.
     */
    @Test
    public void testFractionalTimes() {
        try {
            String script =
                    "(0008,002A) := \"20200101120000\"\n"
            + "(0008,0030) := shiftDateTimeByIncrement[(0008,002A) , \"10\", \"seconds\"]\n"
            + "(0008,0031) := shiftDateTimeByIncrement[\"20200101120000\", \"10\", \"seconds\"]\n"
            + "(0008,0032) := shiftDateTimeByIncrement[\"20200101120000.4\", \"10\", \"seconds\"]\n"
            + "(0008,0033) := shiftDateTimeByIncrement[\"20200101120000.050\", \"10\", \"seconds\"]\n"
            + "(0008,0034) := shiftDateTimeByIncrement[\"20200101120000.800000\", \"10\", \"seconds\"]\n"
            + "(0008,0035) := shiftDateTimeByIncrement[\"20200101120000.800444\", \"10\", \"seconds\"]\n"
            + "(0008,0036) := shiftDateTimeByIncrement[\"20200101120000.800666\", \"10\", \"seconds\"]\n" ;

            final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
            DicomObjectI dobj = DicomObjectFactory.newInstance();
            dobj = sa.apply( dobj).getDicomObject();

            assertEquals( "20200101120010", dobj.getString( 0x00080030));
            assertEquals( "20200101120010", dobj.getString( 0x00080031));
            assertEquals( "20200101120010.4", dobj.getString( 0x00080032));
            assertEquals( "20200101120010.050", dobj.getString( 0x00080033));
            assertEquals( "20200101120010.800000", dobj.getString( 0x00080034));
            assertEquals( "20200101120010.800444", dobj.getString( 0x00080035));
            assertEquals( "20200101120010.800666", dobj.getString( 0x00080036));
        }
        catch( MizerException e) {
            fail("Unexpected exception: " + e);
        }
    }

    private final static String DATE = "2022";
    private final static String DATE_WITH_OFFSET = "2022-0500";
    private final static String MONTH = "202205";
    private final static String MONTH_WITH_OFFSET = "202205-0500";
    private final static String DAY = "20220519";
    private final static String DAY_WITH_OFFSET = "20220519-0500";
    private final static String HOUR = "2022051913";
    private final static String HOUR_WITH_OFFSET = "2022051913-0500";
    private final static String MINUTE = "202205191347";
    private final static String MINUTE_WITH_OFFSET = "202205191347-0500";
    private final static String SECOND = "20220519134723";
    private final static String SECOND_WITH_OFFSET = "20220519134723-0500";
    private final static String FRACTIONAL_SECOND = "20220519134723.123456";
    private final static String FRACTIONAL_SECOND_WITH_OFFSET = "20220519134723.123456-0500";
    @Test
    public void testDT() {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestTag acqDT = new TestTag( 0x0008002A, DATE);

        put( dobj, acqDT);
        String date = dobj.getString( acqDT.tag);
        assertEquals( acqDT.initialValue, dobj.getString( acqDT.tag));

        acqDT.initialValue = DATE_WITH_OFFSET;
        put( dobj, acqDT);
        date = dobj.getString( acqDT.tag);
        assertEquals( acqDT.initialValue, dobj.getString( acqDT.tag));

        acqDT.initialValue = MONTH;
        put( dobj, acqDT);
        assertEquals( acqDT.initialValue, dobj.getString( acqDT.tag));
        acqDT.initialValue = MONTH_WITH_OFFSET;
        put( dobj, acqDT);
        assertEquals( acqDT.initialValue, dobj.getString( acqDT.tag));

        acqDT.initialValue = DAY;
        put( dobj, acqDT);
        assertEquals( acqDT.initialValue, dobj.getString( acqDT.tag));
        acqDT.initialValue = DAY_WITH_OFFSET;
        put( dobj, acqDT);
        assertEquals( acqDT.initialValue, dobj.getString( acqDT.tag));

        acqDT.initialValue = HOUR;
        put( dobj, acqDT);
        assertEquals( acqDT.initialValue, dobj.getString( acqDT.tag));
        acqDT.initialValue = HOUR_WITH_OFFSET;
        put( dobj, acqDT);
        assertEquals( acqDT.initialValue, dobj.getString( acqDT.tag));

        acqDT.initialValue = MINUTE;
        put( dobj, acqDT);
        assertEquals( acqDT.initialValue, dobj.getString( acqDT.tag));
        acqDT.initialValue = MINUTE_WITH_OFFSET;
        put( dobj, acqDT);
        assertEquals( acqDT.initialValue, dobj.getString( acqDT.tag));

        acqDT.initialValue = SECOND;
        put( dobj, acqDT);
        assertEquals( acqDT.initialValue, dobj.getString( acqDT.tag));
        acqDT.initialValue = SECOND_WITH_OFFSET;
        put( dobj, acqDT);
        assertEquals( acqDT.initialValue, dobj.getString( acqDT.tag));

        acqDT.initialValue = FRACTIONAL_SECOND;
        put( dobj, acqDT);
        assertEquals( acqDT.initialValue, dobj.getString( acqDT.tag));
        acqDT.initialValue = FRACTIONAL_SECOND_WITH_OFFSET;
        put( dobj, acqDT);
        assertEquals( acqDT.initialValue, dobj.getString( acqDT.tag));

    }

    private final static String DATE_SHIFTED_UP = "2022";
    private final static String DATE_SHIFTED_DOWN = "2022";
    private final static String DATE_WITH_OFFSET_SHIFTED_UP = "2022-0500";
    private final static String DATE_WITH_OFFSET_SHIFTED_DOWN = "2022-0500";

    /**
     * shifting '2022' up 3 days = '2022'
     * shifting '2022' down 3 days = '2021'.
     * @throws MizerException
     */
    @Test
    public void testShiftDays() throws MizerException {

        // 3 days in seconds = 3 * 24 * 60 * 60 = 259200
        String script = "(0008,002A) := shiftDateTimeByIncrement[ (0008,002A), \"-259200\"]\n" +
                "(0018,9074) := shiftDateTimeByIncrement[ (0018,9074), \"259200\"]";
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestTag acqDT = new TestTag( 0x0008002A, DATE);
        TestTag frameAcqDT = new TestTag( 0x00189074, DATE);

        acqDT.initialValue = DATE;
        frameAcqDT.initialValue = DATE;
        put( dobj, acqDT);
        put( dobj, frameAcqDT);
        assertEquals( acqDT.initialValue, dobj.getString( acqDT.tag));
        assertEquals( frameAcqDT.initialValue, dobj.getString( frameAcqDT.tag));
        dobj = sa.apply( dobj).getDicomObject();
        assertEquals( DATE_SHIFTED_DOWN, dobj.getString( acqDT.tag));
        assertEquals( DATE_SHIFTED_UP, dobj.getString( frameAcqDT.tag));

        acqDT.initialValue = DATE_WITH_OFFSET;
        frameAcqDT.initialValue = DATE_WITH_OFFSET;
        put( dobj, acqDT);
        put( dobj, frameAcqDT);
        assertEquals( acqDT.initialValue, dobj.getString( acqDT.tag));
        assertEquals( frameAcqDT.initialValue, dobj.getString( frameAcqDT.tag));
        dobj = sa.apply( dobj).getDicomObject();
        assertEquals( DATE_WITH_OFFSET_SHIFTED_DOWN, dobj.getString( acqDT.tag));
        assertEquals( DATE_WITH_OFFSET_SHIFTED_UP, dobj.getString( frameAcqDT.tag));

    }

    @Test
    public void testNotADateTimeString() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestTag t1 = new TestTag( 0x00189516, "dateTime");

        put(dobj, t1);

        assertEquals( t1.initialValue, dobj.getString(t1.tag));

        String script = "(0018,9516) := shiftDateTimeByIncrement[(0018,9516), \"10\"]\n" ;

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        AnonymizationResult result = sa.apply(dobj);
        assertTrue(result instanceof AnonymizationResultError);
        assertTrue(String.join("\n", result.getMessages()).contains("Failed to parse DICOM time string"));
    }

    @Test
    public void testMissingDateTimeString() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();

        TestTag t1 = new TestTag( 0x00189516, "dateTime");

        String script = "(0018,9516) := shiftDateTimeByIncrement[(0018,9516), \"10\"]\n" ;

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        AnonymizationResult result = sa.apply(dobj);
        assertTrue(result instanceof AnonymizationResultError);
        assertTrue(String.join("\n", result.getMessages()).contains("Failed to parse DICOM time string"));
    }

    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    private DicomObjectI createSimpleTestObject(String dateTimeString) {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        src_dobj.putString(patientName, "PatientName");

        src_dobj.putString(acquisitionDateTime, dateTimeString);
        src_dobj.putString(frameAcquisitionDateTime, dateTimeString);

        return src_dobj;
    }

    private DicomObjectI createSequenceTestObject(String dateTimeString) {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        src_dobj.putString(patientName, "PatientName");

        src_dobj.putString(p00, dateTimeString);
        src_dobj.putString(p01, dateTimeString);
        src_dobj.putString(p10, dateTimeString);
        src_dobj.putString(p11, dateTimeString);

        src_dobj.putString(q00, dateTimeString);
        src_dobj.putString(q01, dateTimeString);
        src_dobj.putString(q10, dateTimeString);
        src_dobj.putString(q11, dateTimeString);

        return src_dobj;
    }

    private static int[] patientName = {0x00100010};

    private static int[] acquisitionDateTime = {0x0008002A};
    private static int[] frameAcquisitionDateTime = {0x00189074};

    private static int[] p00 = {0x52009230, 0, 0x00209111, 0, 0x00189074};
    private static int[] p01 = {0x52009230, 0, 0x00209111, 1, 0x00189074};
    private static int[] p10 = {0x52009230, 1, 0x00209111, 0, 0x00189074};
    private static int[] p11 = {0x52009230, 1, 0x00209111, 1, 0x00189074};

    private static int[] q00 = {0x52009230, 0, 0x00209111, 0, 0x00189151};
    private static int[] q01 = {0x52009230, 0, 0x00209111, 1, 0x00189151};
    private static int[] q10 = {0x52009230, 1, 0x00209111, 0, 0x00189151};
    private static int[] q11 = {0x52009230, 1, 0x00209111, 1, 0x00189151};

}
