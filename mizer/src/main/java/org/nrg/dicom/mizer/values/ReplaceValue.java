/*
 * mizer: org.nrg.dicom.mizer.values.ReplaceValue
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.values;

import com.google.common.collect.Sets;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.variables.Variable;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public class ReplaceValue extends AbstractMizerValue {
    public ReplaceValue(final Value original, final Value pre, final Value post) {
        _original = original;
        _pre = pre;
        _post = post;
    }

    @Override
    public String on(final DicomObjectI dicomObject) throws ScriptEvaluationException {
        return _original.on(dicomObject).replace(_pre.on(dicomObject), _post.on(dicomObject));
    }

    @Override
    public String on(final Map<Integer, String> map) throws ScriptEvaluationException {
        return _original.on(map).replace(_pre.on(map), _post.on(map));
    }

    @Override
    public Set<Variable> getVariables() {
        final Set<Variable> variables = Sets.newLinkedHashSet();
        variables.addAll(_original.getVariables());
        variables.addAll(_pre.getVariables());
        variables.addAll(_post.getVariables());
        return variables;
    }

    @Override
    public SortedSet<Long> getTags() {
        final SortedSet<Long> tags = Sets.newTreeSet();
        tags.addAll(_original.getTags());
        tags.addAll(_pre.getTags());
        tags.addAll(_post.getTags());
        return tags;
    }

    @Override
    public void replace(final Variable variable) {
        _original.replace(variable);
        _pre.replace(variable);
        _post.replace(variable);
    }

    private final Value _original;
    private final Value _pre;
    private final Value _post;
}
