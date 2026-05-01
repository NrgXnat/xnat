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

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertTrue;
import static org.nrg.dicom.dicomedit.TestUtils.TestTag;
import static org.nrg.dicom.dicomedit.TestUtils.put;

/**
 * Run tests of shiftDateByIncrement function.
 *
 */
public class TestShiftDateByIncrementFunction {

    /**
     * Test date shift in simple tags.
     *
     * @throws MizerException
     */
    @Test
    public void testSimpleTags() throws MizerException {

        String script =
                "(0008,0020) := shiftDateByIncrement[ (0008,0020), \"14\"]\n" +
                        "(0008,0021) := shiftDateByIncrement[ (0008,0021), \"14\"]";

        final DicomObjectI src_dobj = createTestObject();

        assertEquals( "PatientName", src_dobj.getString(patientName));

        assertEquals( "20120919", src_dobj.getString(studyDate));
        assertEquals( "20131231", src_dobj.getString(seriesDate));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( "PatientName", src_dobj.getString(patientName));

        assertEquals( "20121003", result_dobj.getString(studyDate));
        assertEquals( "20140114", result_dobj.getString(seriesDate));
    }

    @Test
    public void testIntegerShift() throws MizerException {

        String script = "(0008,0020) := shiftDateByIncrement[ (0008,0020), 14]\n";

        final DicomObjectI src_dobj = createTestObject();

        assertEquals( "PatientName", src_dobj.getString(patientName));

        assertEquals( "20120919", src_dobj.getString(studyDate));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( "PatientName", src_dobj.getString(patientName));

        assertEquals( "20121003", result_dobj.getString(studyDate));
    }

    @Test
    public void testShiftUnits() throws MizerException {
        int seconds = 14 * 24 * 60 * 60;

        String script = "(0008,0020) := shiftDateByIncrement[ (0008,0020), "+seconds+", \"seconds\"]\n";

        final DicomObjectI src_dobj = createTestObject();

        assertEquals( "PatientName", src_dobj.getString(patientName));

        assertEquals( "20120919", src_dobj.getString(studyDate));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( "PatientName", src_dobj.getString(patientName));

        assertEquals( "20121003", result_dobj.getString(studyDate));
    }

    @Test
    public void testPrecision() throws MizerException {
        final DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestTag pn = new TestTag(patientName, "PatientName");
        TestTag std = new TestTag(studyDate, "201210", "201210");
        TestTag sed = new TestTag(seriesDate, "201210", "201211");

        put(dobj,pn);
        put(dobj,std);
        put(dobj,sed);

        String script =
                "(0008,0020) := shiftDateByIncrement[ (0008,0020), \"15\"]\n" +
                        "(0008,0021) := shiftDateByIncrement[ (0008,0021), \"16\"]";

        assertEquals( pn.initialValue, dobj.getString(pn.tag));
        assertEquals( std.initialValue, dobj.getString(std.tag));
        assertEquals( sed.initialValue, dobj.getString(sed.tag));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        sa.apply(dobj).getDicomObject();

        assertEquals( pn.postValue, dobj.getString(pn.tag));
        assertEquals( std.postValue, dobj.getString(std.tag));
        assertEquals( sed.postValue, dobj.getString(sed.tag));
    }

    @Test
    public void test5YearShiftTags() throws MizerException {
        // Note date changes because of leap year.
        // 5 * 365 = 1825
        String script =
                "(0008,0020) := shiftDateByIncrement[ (0008,0020), \"1825\"]\n" ;

        final DicomObjectI src_dobj = createTestObject();

        assertEquals( "PatientName", src_dobj.getString(patientName));

        assertEquals( "20120919", src_dobj.getString(studyDate));
        assertEquals( "20131231", src_dobj.getString(seriesDate));

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        final DicomObjectI result_dobj = sa.apply(src_dobj).getDicomObject();

        assertEquals( "PatientName", src_dobj.getString(patientName));

        assertEquals( "20170918", result_dobj.getString(studyDate));
    }

    @Test
    public void testShiftString() throws MizerException {
        DicomObjectI dobj = createTestObject();

        TestTag t1 = new TestTag( 0x00080012, "19600519", "19610514");
        TestTag t2 = new TestTag( 0x00189516, "20200101", "20200111");
        TestTag t3 = new TestTag( 0x00189517, "20200101", "20191222");
        TestTag t4 = new TestTag( 0x00189804, "20200228", "20200826");
        TestTag t5 = new TestTag( 0x00189919, "19960808", "19940809");

        put(dobj, t1);
        put(dobj, t2);
        put(dobj, t3);
        put(dobj, t4);
        put(dobj, t5);

        assertEquals( t1.initialValue, dobj.getString(t1.tag));
        assertEquals( t2.initialValue, dobj.getString(t2.tag));
        assertEquals( t3.initialValue, dobj.getString(t3.tag));
        assertEquals( t4.initialValue, dobj.getString(t4.tag));
        assertEquals( t5.initialValue, dobj.getString(t5.tag));

        String script =
                "(0008,0012) := shiftDateByIncrement[ (0008,0012), \"360\"]\n" +
                        "(0018,9516) := shiftDateByIncrement[\"20200101\", \"10\"]\n" +
                        "(0018,9517) := shiftDateByIncrement[\"20200101\", \"-10\"]\n" +
                        "(0018,9804) := shiftDateByIncrement[\"20200228\", \"180\"]\n" +
                        "(0018,9919) := shiftDateByIncrement[\"19960808\", \"-730\"]\n" ;

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals( t1.postValue, dobj.getString(t1.tag));
        assertEquals( t2.postValue, dobj.getString(t2.tag));
        assertEquals( t3.postValue, dobj.getString(t3.tag));
        assertEquals( t4.postValue, dobj.getString(t4.tag));
        assertEquals( t5.postValue, dobj.getString(t5.tag));
    }

    @Test
    public void testNotADateString() throws MizerException {
        DicomObjectI dobj = createTestObject();

        TestTag t1 = new TestTag( 0x0008002A, "date");

        put(dobj, t1);

        assertEquals( t1.initialValue, dobj.getString(t1.tag));

        String script =
                "(0008,002A) := shiftDateByIncrement[ (0008,002A), \"360\"]\n" ;

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        AnonymizationResult result = sa.apply(dobj);
        assertTrue(result instanceof AnonymizationResultError);
        assertTrue(String.join("\n", result.getMessages()).contains("Error evaluating shiftDateByIncrement"));
    }

    @Test
    public void testMissingDateString() throws MizerException {
        DicomObjectI dobj = createTestObject();

        TestTag t1 = new TestTag( 0x0008002A, "date");

        String script =
                "(0008,002A) := shiftDateByIncrement[ (0008,002A), \"360\"]\n" ;

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        AnonymizationResult result = sa.apply(dobj);
        assertTrue(result instanceof AnonymizationResultError);
        assertTrue(String.join("\n", result.getMessages()).contains("Error evaluating shiftDateByIncrement"));
    }

    private ByteArrayInputStream bytes(final String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    private DicomObjectI createTestObject() {
        final DicomObjectI src_dobj = DicomObjectFactory.newInstance();

        src_dobj.putString(patientName, "PatientName");

        src_dobj.putString(studyDate, "20120919");
        src_dobj.putString(seriesDate, "20131231");

        return src_dobj;
    }

    private static int patientName = 0x00100010;
    private static int studyDate = 0x00080020;
    private static int seriesDate = 0x00080021;

}
