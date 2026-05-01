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
import org.nrg.dicom.dicomedit.TestUtils.TestTag;
import org.nrg.dicom.dicomedit.TestUtils.TestSeqTag;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultError;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.nrg.dicom.dicomedit.TestUtils.bytes;
import static org.nrg.dicom.dicomedit.TestUtils.put;

/**
 * Run tests of anonymizeAge function.
 *
 */
public class TestScalePatientAgeAndDobFromStudyDateFunction {
    private static final int PATIENT_AGE = 0x00101010;
    private static final int PATIENT_DOB = 0x00100030;
    private static final int STUDY_DATE = 0x00080020;

    @Test
    public void testYoungster() throws MizerException {

        TestTag youngster = new TestTag( 0x00101010, "063Y");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put( dobj, youngster);

        assertEquals( youngster.initialValue, dobj.getString(youngster.tag));

        String script = "scalePatientAgeAndDobFromStudyDate[]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals( youngster.postValue, dobj.getString(youngster.tag));
    }

    @Test
    public void testOldster() throws MizerException {

        TestTag oldster = new TestTag( 0x00101010, "090Y", "089Y");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put( dobj, oldster);

        assertEquals( oldster.initialValue, dobj.getString(oldster.tag));

        String script = "scalePatientAgeAndDobFromStudyDate[]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals( oldster.postValue, dobj.getString(oldster.tag));
    }

    @Test
    public void testUnexpectedAgeFormat() throws MizerException {

        TestTag weirdAge = new TestTag( 0x00101010, "45");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put( dobj, weirdAge);

        assertEquals( weirdAge.initialValue, dobj.getString(weirdAge.tag));

        String script = "scalePatientAgeAndDobFromStudyDate[]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        AnonymizationResult result = sa.apply(dobj);
        assertTrue(result instanceof AnonymizationResultError);
        assertTrue(String.join("\n", result.getMessages()).contains("Error parsing age"));
    }

    @Test
    public void testInSequences() throws MizerException {

        TestTag t1 = new TestTag( 0x00101010, "090Y", "089Y");
        TestTag p1c = new TestTag( 0x00130010, "pc1");
        TestSeqTag p1_0_t1 = new TestSeqTag( new int[]{0x00131000,0,0x00101010}, "099Y", "089Y");
        TestSeqTag p1_1_t1 = new TestSeqTag( new int[]{0x00131000,1,0x00101010}, "099Y", "089Y");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put( dobj, t1);
        put( dobj, p1c);
        put( dobj, p1_0_t1);
        put( dobj, p1_1_t1);

        assertEquals( t1.initialValue, dobj.getString(t1.tag));
        assertEquals( p1c.initialValue, dobj.getString(p1c.tag));
        assertEquals( p1_0_t1.initialValue, dobj.getString(p1_0_t1.tag));
        assertEquals( p1_1_t1.initialValue, dobj.getString(p1_1_t1.tag));

        String script = "scalePatientAgeAndDobFromStudyDate[]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals( t1.postValue, dobj.getString(t1.tag));
        assertEquals( p1c.postValue, dobj.getString(p1c.tag));
        assertEquals( p1_0_t1.postValue, dobj.getString(p1_0_t1.tag));
        assertEquals( p1_1_t1.postValue, dobj.getString(p1_1_t1.tag));
    }

    @Test
    public void testDOB() throws MizerException {

        TestTag age = new TestTag( PATIENT_AGE, "100Y", "089Y");
        TestTag dob = new TestTag( PATIENT_DOB, "19230315", "19340316");
        TestTag studydate = new TestTag( STUDY_DATE, "20230315");

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        put( dobj, age);
        put( dobj, dob);
        put( dobj, studydate);

        assertEquals( age.initialValue, dobj.getString(age.tag));
        assertEquals( dob.initialValue, dobj.getString(dob.tag));
        assertEquals( studydate.initialValue, dobj.getString(studydate.tag));

        String script = "scalePatientAgeAndDobFromStudyDate[]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance( bytes(script));
        dobj = sa.apply(dobj).getDicomObject();

        assertEquals( age.postValue, dobj.getString(age.tag));
        assertEquals( dob.postValue, dobj.getString(dob.tag));
        assertEquals( studydate.postValue, dobj.getString(studydate.tag));
    }

}
