/*
 * DicomEdit: org.nrg.dcm.edit.fn.ReplaceTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.edit.fn;

import org.dcm4che3.data.Tag;
import org.junit.Test;
import org.nrg.dicom.mizer.scripts.ScriptFunction;
import org.nrg.dicom.mizer.values.SingleTagValue;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;
import org.nrg.test.workers.resources.ResourceManager;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class ReplaceTest {
    @Test
    public void testApply() throws Exception {
        final ScriptFunction replace = new Replace();
        final List<Value> values = Arrays.asList(ORIGINAL_VALUE, new ConstantValue("foo"), new ConstantValue("_"));
        assertEquals("_xbarxbazx_xfrob", replace.apply(values).on(CONTEXT));
        assertEquals(ORIGINAL_STRING, replace.apply(Arrays.asList(ORIGINAL_VALUE, new ConstantValue("none"), new ConstantValue("_"))).on(CONTEXT));

        final DicomObjectI dicomObject = DicomObjectFactory.newInstance(DICOM_FILE);
        assertEquals("Sample*ID", replace.apply(Arrays.asList(new SingleTagValue(Tag.PatientID), new ConstantValue(" "), new ConstantValue("*"))).on(dicomObject));
        assertEquals("Sample ID", replace.apply(Arrays.asList(new SingleTagValue(Tag.PatientID), new ConstantValue("*"), new ConstantValue("$"))).on(dicomObject));
        assertEquals("MEDPC ID", replace.apply(Arrays.asList(new SingleTagValue(Tag.PatientID), new ConstantValue("Sample"), new SingleTagValue(Tag.StationName))).on(dicomObject));

        try {
            replace.apply(Arrays.asList(ORIGINAL_VALUE, new ConstantValue("x")));
            fail("Expected ScriptEvaluationException for replace[original, pre]");
        } catch (ScriptEvaluationException ignored) {
        }

        try {
            replace.apply(Collections.singletonList(ORIGINAL_VALUE));
            fail("Expected ScriptEvaluationException for replace[original]");
        } catch (ScriptEvaluationException ignored) {
        }

        try {
            replace.apply(Arrays.asList(new Value[]{}));
            fail("Expected ScriptEvaluationException for replace[]");
        } catch (ScriptEvaluationException ignored) {
        }
    }

    private static final File                 DICOM_FILE      = ResourceManager.getInstance().getTestResourceFile("dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");
    private static final Map<Integer, String> CONTEXT         = Collections.emptyMap();
    private static final String               ORIGINAL_STRING = "fooxbarxbazxfooxfrob";
    private static final Value                ORIGINAL_VALUE  = new ConstantValue(ORIGINAL_STRING);
}
