/*
 * DicomEdit: org.nrg.dcm.edit.fn.MatchTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.edit.fn;

import org.dcm4che2.data.Tag;
import org.junit.Test;
import org.nrg.dicom.mizer.scripts.ScriptFunction;
import org.nrg.dicom.mizer.values.SingleTagValue;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.IntegerValue;
import org.nrg.test.workers.resources.ResourceManager;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class MatchTest {
    private static final File                 DICOM_FILE = ResourceManager.getInstance().getTestResourceFile("dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");
    private static final Map<Integer, String> context    = Collections.emptyMap();

    /**
     * Test method for {@link Match#apply(List)}.
     *
     * @throws IOException               When an error occurs reading or writing to a file.
     * @throws ScriptEvaluationException When an error occurs evaluating an anonymization script.
     */
    @Test
    public void testApply() throws MizerException, IOException {
        final ScriptFunction match = new Match();
        assertEquals("v00", match.apply(Arrays.asList(new ConstantValue("s01_v00_mr1"),
                                                      new ConstantValue(".*_(v[0-9]+)_.*"),
                                                      new IntegerValue("1"))).on(context));

        assertEquals("s01_v00_mr1", match.apply(Arrays.asList(new ConstantValue("s01_v00_mr1"),
                                                              new ConstantValue(".*_(v[0-9]+)_.*"),
                                                              new IntegerValue("0"))).on(context));

        assertNull(match.apply(Arrays.asList(new ConstantValue("s01+v00+mr1"),
                                             new ConstantValue(".*_(v[0-9]+)_.*"),
                                             new IntegerValue("1"))).on(context));

        final DicomObjectI o4 = DicomObjectFactory.newInstance(DICOM_FILE);
        assertEquals("ID", match.apply(Arrays.asList(new SingleTagValue(Tag.PatientID),
                                                     new ConstantValue("Sample (\\w+)"),
                                                     new IntegerValue("1"))).on(o4));
    }

    @Test
    public void testApplyNullValue() throws ScriptEvaluationException, IOException {
        final ScriptFunction match = new Match();
        assertNull(match.apply(Arrays.asList(new ConstantValue(null),
                                             new ConstantValue(".*_(v[0-9]+)_.*"),
                                             new IntegerValue("0"))).on(context));
    }
}
