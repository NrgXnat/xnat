/*
 * DicomEdit: org.nrg.dicom.mizer.values.SubstringValueTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.values;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import org.dcm4che3.data.Tag;
import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.variables.BasicVariable;
import org.nrg.dicom.mizer.variables.Variable;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class SubstringValueTest {

    /**
     * Test method for {@link SubstringValue#getTags()}.
     */
    @Test
    public void testGetTags() {
        final Value v1  = new SingleTagValue(Tag.PatientName);
        final Value ss1 = new SubstringValue(v1, 0, 1);
        assertEquals(ImmutableSortedSet.of(0xffffffffL & Tag.PatientName), ss1.getTags());
    }

    /**
     * Test method for {@link SubstringValue#getVariables()}.
     */
    @Test
    public void testGetVariables() {
        final Value v1 = new SingleTagValue(Tag.PatientName);
        final Value ss1 = new SubstringValue(v1, 0, 1);
        assertTrue(ss1.getVariables().isEmpty());

        final Variable var = new BasicVariable("foo");
        final Value    v2  = new VariableValue(var);
        final Value    ss2 = new SubstringValue(v2, 0, 1);
        assertEquals(ImmutableSet.of(var), ss2.getVariables());
    }

    /**
     * Test method for {@link SubstringValue#on(java.util.Map)}.
     */
    @Test
    public void testOnMapOfIntegerString() throws ScriptEvaluationException {
        final Value v1 = new SingleTagValue(Tag.PatientName);
        final Map<Integer,String> m = ImmutableMap.of(Tag.PatientName, "abcdef");
        final Value ss = new SubstringValue(v1, 2, 5);
        assertEquals("cde", ss.on(m));
        
        try {
            ss.on(ImmutableMap.of(Tag.PatientName, "abcd"));
            fail("expected string index bounds exception");
        } catch (ScriptEvaluationException ignored) {}
    }
}
