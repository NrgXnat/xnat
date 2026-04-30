/*
 * DicomEdit: org.nrg.dcm.edit.ValueEqualsConstraintTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.edit;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
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
public class ValueEqualsConstraintTest {
    /**
     * Test method for {@link AbstractValueConstraint#matches(DicomObjectI)}.
     */
    @Test
    public void testMatchesDicomObject() throws ScriptEvaluationException {
        final DicomObjectI o1 = DicomObjectFactory.newInstance();
        o1.putString(Tag.StudyDescription, "LO", "foo");

        final DicomObjectI o2 = DicomObjectFactory.newInstance();
        o2.putString(Tag.StudyDescription, VR.LO.toString(), "bar");

        final Value           studyDesc = new SingleTagValue(Tag.StudyDescription);
        final ConstraintMatch m1        = new ValueEqualsConstraint(studyDesc, new ConstantValue("foo"));
        assertTrue(m1.matches(o1));
        assertFalse(m1.matches(o2));

        final ConstraintMatch m2 = new ValueEqualsConstraint(new ConstantValue("bar"), studyDesc);
        assertFalse(m2.matches(o1));
        assertTrue(m2.matches(o2));

        final Value patientName = new SingleTagValue(Tag.PatientName);
        final Value nullVal     = new ConstantValue(null);

        final ConstraintMatch m3 = new ValueEqualsConstraint(studyDesc, nullVal);
        assertFalse(m3.matches(o1));
        final ConstraintMatch m4 = new ValueEqualsConstraint(patientName, nullVal);
        assertTrue(m4.matches(o1));

        final ConstraintMatch m5 = new ValueEqualsConstraint(patientName, new ConstantValue(""));
        assertFalse(m5.matches(o1));
    }
}
