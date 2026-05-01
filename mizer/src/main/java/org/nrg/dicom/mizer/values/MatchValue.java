/*
 * mizer: org.nrg.dicom.mizer.values.MatchValue
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.values;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.variables.Variable;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatchValue extends AbstractMizerValue {
    public MatchValue(final Value value, final Value pattern, final int group) {
        _value = value;
        _pattern = pattern;
        _group = group;
    }

    @Override
    public Set<Variable> getVariables() {
        final Set<Variable> variables = Sets.newLinkedHashSet(_value.getVariables());
        variables.addAll(_pattern.getVariables());
        return variables;
    }

    @Override
    public SortedSet<Long> getTags() {
        final SortedSet<Long> tags = Sets.newTreeSet(_value.getTags());
        tags.addAll(_pattern.getTags());
        return tags;
    }

    @Override
    public String on(final DicomObjectI dicomObject) throws ScriptEvaluationException {
        return evaluate(_value.on(dicomObject), _pattern.on(dicomObject), _group);
    }

    @Override
    public String on(final Map<Integer, String> map) throws ScriptEvaluationException {
        return evaluate(_value.on(map), _pattern.on(map), _group);
    }

    @Override
    public void replace(final Variable variable) {
        _value.replace(variable);
        _pattern.replace(variable);
    }

    @Override
    public String toString() {
        return "match\"" + _pattern + "\" on variables: " + Joiner.on(", ").join(getVariables());
    }

    private static String evaluate(final String value, final String pattern, final int group) {
        if (null == value) {
            return null;
        }
        final Matcher matcher = Pattern.compile(pattern).matcher(value);
        return matcher.matches() ? matcher.group(group) : null;
    }

    private final Value _value;
    private final Value _pattern;
    private final int   _group;
}
