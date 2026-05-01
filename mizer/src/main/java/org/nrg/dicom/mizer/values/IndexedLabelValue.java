/*
 * mizer: org.nrg.dicom.mizer.values.IndexedLabelValue
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.values;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationRuntimeException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.variables.Variable;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * Provides a container for indexed label values.
 */
public class IndexedLabelValue extends AbstractMizerValue {
    public IndexedLabelValue(final Value format, final Function<String, Boolean> isAvailable) {
        _format = format;
        _isAvailable = isAvailable;
    }

    @Override
    public Set<Variable> getVariables() {
        return _format.getVariables();
    }

    @Override
    public void replace(final Variable variable) {
        _format.replace(variable);
    }

    @Override
    public SortedSet<Long> getTags() {
        return _format.getTags();
    }

    @Override
    public String on(final DicomObjectI dicomObject) throws ScriptEvaluationException {
        return valueFor(_format.on(dicomObject));
    }

    @Override
    public String on(final Map<Integer, String> m) throws ScriptEvaluationException {
        return valueFor(_format.on(m));
    }

    @Override
    public String asString() {
        try {
            return on(Collections.<Integer, String>emptyMap());
        } catch (ScriptEvaluationException e) {
            throw new ScriptEvaluationRuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "Format \"" + _format + "\" on variables: " + Joiner.on(", ").join(getVariables());
    }

    private NumberFormat buildFormatter(final int len) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append("0");
        }
        return new DecimalFormat(sb.toString());
    }

    private String valueFor(final String format) throws ScriptEvaluationException {
        if (StringUtils.isBlank(format) || !format.contains("#")) {
            return "";
        }
        final int offset = format.indexOf('#');
        if (-1 == offset) {
            return "";
        }
        int end = offset;
        while (end < format.length() && '#' == format.charAt(end)) {
            end++;
        }

        final NumberFormat nf = buildFormatter(end - offset);

        final StringBuilder sb = new StringBuilder(format);

        for (int i = 0; true; i++) {
            final String label = sb.replace(offset, end, nf.format(i)).toString();
            if (BooleanUtils.isTrue(_isAvailable.apply(label))) {
                return label;
            }
        }
    }

    private final Value                     _format;
    private final Function<String, Boolean> _isAvailable;
}
