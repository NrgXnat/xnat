/*
 * DicomEdit: org.nrg.dcm.edit.fn.UppercaseTest
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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class UppercaseTest {
    private static final DicomObjectI NULL_OBJECT    = null;
    private static final Value        VALUE_FOO      = new ConstantValue("foo");
    private static final Value        VALUE_BAR      = new ConstantValue("Bar");
    private static final Value        VALUE_1BAZBANG = new ConstantValue("1bAz!");

    /**
     * Test method for {@link Uppercase#apply(List)}.
     */
    @Test
    public void testApplyNoArgs() {
        final Uppercase u = new Uppercase();
        try {
            u.apply(new ArrayList<Value>());
            fail("expected ScriptEvaluationException for empty arguments list");
        } catch (ScriptEvaluationException ignored) {
        }
    }

    /**
     * Test method for {@link Uppercase#apply(List)}.
     */
    @Test
    public void testApply() throws ScriptEvaluationException {
        final Uppercase u = new Uppercase();
        assertEquals("FOO", u.apply(Collections.singletonList(VALUE_FOO)).on(NULL_OBJECT));
        assertEquals("BAR", u.apply(Collections.singletonList(VALUE_BAR)).on(NULL_OBJECT));
        assertEquals("1BAZ!", u.apply(Collections.singletonList(VALUE_1BAZBANG)).on(NULL_OBJECT));
    }
}
