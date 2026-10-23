/*
 * mizer: org.nrg.dicom.mizer.values.SubstringValue
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.values;

import org.apache.commons.lang3.StringUtils;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.variables.Variable;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public class SubstringValue extends AbstractMizerValue {
    public SubstringValue(final Value value, final int start, final int end) {
        _value = value;
        _start = start;
        _end = end;
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
        return substring(_value.on(dicomObject));
    }

    @Override
    public String on(final Map<Integer, String> map) throws ScriptEvaluationException {
        return substring(_value.on(map));
    }

    private String substring(final String evaluated) throws ScriptEvaluationException {
        if (StringUtils.isBlank(evaluated)) {
            return evaluated;
        }
        try {
            return evaluated.substring(_start, _end);
        } catch (StringIndexOutOfBoundsException e) {
            throw new ScriptEvaluationException(String.format("Index out of bounds, requested substring from %d to %d, string length was %d.", _start, _end, evaluated.length()));
        }
    }

    private final Value _value;
    private final int   _start;
    private final int   _end;
}
