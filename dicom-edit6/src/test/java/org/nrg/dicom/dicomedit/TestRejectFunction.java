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
import org.nrg.dicom.mizer.objects.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.nrg.dicom.dicomedit.TestUtils.*;

/**
 * Reject function tests.
 *
 */
public class TestRejectFunction {

    @Test
    public void testRejectFunctionDefaultArg() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestTag tag = new TestTag(0x00080080, "fubar");
        put( dobj, tag);

        final String script = "reject[]\n";
        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script), false);
        AnonymizationResult result = sa.apply(dobj);
        assertTrue( result instanceof AnonymizationResultReject);
    }

    @Test
    public void testRejectFunctionArgFalse() throws MizerException {
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestTag tag = new TestTag(0x00080080, "fubar");
        put( dobj, tag);

        final String script = "reject[]\n";

        final BaseScriptApplicator sa = BaseScriptApplicator.getInstance(bytes(script), true);
        AnonymizationResult result = sa.apply(dobj);
        assertFalse( result instanceof AnonymizationResultReject);
    }

}
