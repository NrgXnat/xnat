/*
 * DicomEdit: org.nrg.dcm.edit.fn.LowercaseTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.edit.fn;

import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class LowercaseTest {
    private static final DicomObjectI NULL_OBJECT = null;
    private static final Value        vFOO        = new ConstantValue("FOO");
    private static final Value        vBar        = new ConstantValue("Bar");
    private static final Value        v1bAzbang   = new ConstantValue("1bAz!");

    /**
     * Test method for {@link org.nrg.dcm.edit.fn.Lowercase#apply(java.util.List)}.
     */
    @Test
    public void testApplyNoArgs() {
        final Lowercase lowercase = new Lowercase();
        try {
            lowercase.apply(new ArrayList<Value>());
            fail("expected ScriptEvaluationException for empty arguments list");
        } catch (ScriptEvaluationException ignored) {
        }
    }

    /**
     * Test method for {@link org.nrg.dcm.edit.fn.Lowercase#apply(java.util.List)}.
     */
    @Test
    public void testApply() throws ScriptEvaluationException {
        final Lowercase l = new Lowercase();
        assertEquals("foo", l.apply(Collections.singletonList(vFOO)).on(NULL_OBJECT));
        assertEquals("bar", l.apply(Collections.singletonList(vBar)).on(NULL_OBJECT));
        assertEquals("1baz!", l.apply(Collections.singletonList(v1bAzbang)).on(NULL_OBJECT));
    }
}
