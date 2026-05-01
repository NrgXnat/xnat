/*
 * mizer: org.nrg.dicom.mizer.values.LowercaseValue
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.values;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.variables.Variable;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public class LowercaseValue extends AbstractMizerValue {
    public LowercaseValue(final Value value) {
        _value = value;
    }

    @Override
    public Set<Variable> getVariables() {
        return _value.getVariables();
    }

    @Override
    public void replace(final Variable variable) {
        _value.replace(variable);
    }

    @Override
    public SortedSet<Long> getTags() {
        return _value.getTags();
    }

    @Override
    public String on(final DicomObjectI dicomObject) throws ScriptEvaluationException {
        return StringUtils.lowerCase(_value.on(dicomObject));
    }

    @Override
    public String on(final Map<Integer, String> map) throws ScriptEvaluationException {
        return StringUtils.lowerCase(_value.on(map));
    }

    @Override
    public String toString() {
        return "lowercase \"" + _value + "\" on variables: " + Joiner.on(", ").join(getVariables());
    }

    private final Value _value;
}
