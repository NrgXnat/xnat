/*
 * DicomEdit: org.nrg.dcm.edit.fn.UrlEncodeTest
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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class UrlEncodeTest {
    private static final DicomObjectI NULL_OBJECT = null;
    private static final Value        VALUE0      = new ConstantValue("no-change-needed");
    private static final List<Value>  VALUE0_LIST = Collections.singletonList(VALUE0);
    private static final Value        VALUE1      = new ConstantValue("has space");
    private static final List<Value>  VALUE1_LIST = Collections.singletonList(VALUE1);
    private static final Value        VALUE2      = new ConstantValue("lots of $%^@! characters");
    private static final List<Value>  VALUE2_LIST = Collections.singletonList(VALUE2);

    @Test
    public void testApplyNoArgs() {
        final UrlEncode u = new UrlEncode();
        try {
            u.apply(new ArrayList<Value>());
            fail("expected ScriptEvaluationException for empty arguments list");
        } catch (ScriptEvaluationException ignored) {
        }
    }

    @Test
    public void testApply() throws ScriptEvaluationException, UnsupportedEncodingException {
        final UrlEncode u = new UrlEncode();
        assertEquals("no-change-needed", u.apply(VALUE0_LIST).on(NULL_OBJECT));
        assertEquals("has+space", u.apply(VALUE1_LIST).on(NULL_OBJECT));
        assertEquals("lots+of+%24%25%5E%40%21+characters", u.apply(VALUE2_LIST).on(NULL_OBJECT));
        assertEquals(VALUE2.on(NULL_OBJECT), URLDecoder.decode(u.apply(VALUE2_LIST).on(NULL_OBJECT), "UTF-8"));
    }
}
