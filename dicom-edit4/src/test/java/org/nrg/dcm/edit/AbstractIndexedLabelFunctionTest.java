/*
 * DicomEdit: org.nrg.dcm.edit.AbstractIndexedLabelFunctionTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.edit;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class AbstractIndexedLabelFunctionTest {
    final class TestLabelFunction extends AbstractIndexedLabelFunction {
        private final Set<String> _defined;

        public TestLabelFunction(final String... defined) {
            this._defined = new HashSet<>(Arrays.asList(defined));
        }

        /**
         * This returns a function that returns true in the case where the session label retrieval failed; this is sort
         * of broken but prevents an ugly infinite loop situation.
         *
         * @return Returns the function to test for availability.
         */
        @Override
        protected Function<String, Boolean> isAvailable() throws ScriptEvaluationException {
            return new Function<String, Boolean>() {
                @Override
                public Boolean apply(final String label) {
                    return !_defined.contains(label);
                }
            };
        }
    }

    @Test
    public void testApply() throws ScriptEvaluationException {
        assertEquals("", new TestLabelFunction().apply(format("")).on(_dicomObject));
        assertEquals("0", new TestLabelFunction().apply(format("#")).on(_dicomObject));
        assertEquals("1", new TestLabelFunction("0").apply(format("#")).on(_dicomObject));
        assertEquals("00", new TestLabelFunction().apply(format("##")).on(_dicomObject));
        assertEquals("01", new TestLabelFunction("00").apply(format("##")).on(_dicomObject));
        assertEquals("foo002bar", new TestLabelFunction("foo000bar", "foo001bar").apply(format("foo###bar")).on(_dicomObject));
    }

    private final DicomObjectI _dicomObject = null;

    private List<Value> format(final String format) {
        final List<Value> l = Lists.newArrayList();
        l.add(new ConstantValue(format));
        return l;
    }
}
