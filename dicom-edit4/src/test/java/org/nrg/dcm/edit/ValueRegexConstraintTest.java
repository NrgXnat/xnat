/*
 * DicomEdit: org.nrg.dcm.edit.ValueRegexConstraintTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.edit;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.SingleTagValue;
import org.nrg.dicom.mizer.values.Value;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class ValueRegexConstraintTest {
    /**
     * Test method for {@link org.nrg.dcm.edit.AbstractValueConstraint#matches(DicomObjectI)}.
     */
    @Test
    public void testMatchesDicomObject() throws ScriptEvaluationException {
        final DicomObjectI o1 = DicomObjectFactory.newInstance(new BasicDicomObject());
        o1.getDcm4che2Object().putString(Tag.StudyDescription, VR.LO, "foo");

        final DicomObjectI o2 = DicomObjectFactory.newInstance(new BasicDicomObject());
        o2.getDcm4che2Object().putString(Tag.StudyDescription, VR.LO, "bar");
        
        final Value           studyDesc = new SingleTagValue(Tag.StudyDescription);
        final ConstraintMatch m1        = new ValueRegexConstraint(studyDesc, new ConstantValue("f\\wo"));
        assertTrue(m1.matches(o1));
        assertFalse(m1.matches(o2));
        
        final ConstraintMatch m3 = new ValueRegexConstraint(studyDesc, new ConstantValue("\\w+"));
        assertTrue(m3.matches(o1));
        assertTrue(m3.matches(o2));
        
        final Value nullVal = new ConstantValue(null);       
        final ConstraintMatch m4 = new ValueRegexConstraint(nullVal, new ConstantValue(""));
        assertFalse(m4.matches(o1));
        
        final Value patientName = new SingleTagValue(Tag.PatientName);
        final ConstraintMatch m5 = new ValueRegexConstraint(patientName, new ConstantValue("\\w+"));
        assertFalse(m5.matches(o1));
    }
}
