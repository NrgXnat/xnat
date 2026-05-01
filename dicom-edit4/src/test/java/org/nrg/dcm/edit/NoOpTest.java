/*
 * DicomEdit: org.nrg.dcm.edit.NoOpTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.edit;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.Tag;
import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicomtools.exceptions.AttributeException;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class NoOpTest {
    /**
     * Test method for {@link org.nrg.dcm.edit.NoOp#makeAction(DicomObjectI)}.
     */
    @Test
    public void testMakeAction() throws AttributeException, ScriptEvaluationException {
        final Operation noop = new NoOp();
        final Action noa = noop.makeAction(DicomObjectFactory.newInstance(new BasicDicomObject()));
        noa.apply();    // TODO: make this a mock and verify zero interactions
    }

    /**
     * Test method for {@link org.nrg.dcm.edit.NoOp#apply(java.util.Map)}.
     */
    @Test
    public void testApply() throws ScriptEvaluationException {
        final Operation noop = new NoOp();
        assertEquals(null, noop.apply(Collections.singletonMap(Tag.StudyInstanceUID, "1.2.3.4")));
    }
}
