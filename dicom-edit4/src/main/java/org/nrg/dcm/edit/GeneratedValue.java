/*
 * DicomEdit: org.nrg.dcm.edit.GeneratedValue
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.AbstractMizerValue;
import org.nrg.dicom.mizer.values.Value;
import org.nrg.dicom.mizer.variables.Variable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class GeneratedValue extends AbstractMizerValue {
    private final GeneratorScope _scope;
    private final int            _tag;
    private final String         _label;
    private final List<Value>    _parameters;

    public GeneratedValue(final GeneratorScope scope, final int tag, final String label, final List<Value> parameters) {
        _scope = scope;
        _tag = tag;
        _label = label;
        _parameters = ImmutableList.copyOf(parameters);
    }


    /* (non-Javadoc)
     * @see org.nrg.dcm.edit.Value#getTags()
     */
    public SortedSet<Long> getTags() {
        final SortedSet<Long> tags = Sets.newTreeSet();
        for (final Value param : _parameters) {
            tags.addAll(param.getTags());
        }
        return tags;
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.edit.Value#getVariables()
     */
    public Set<Variable> getVariables() {
        final Set<Variable> vars = Sets.newLinkedHashSet();
        for (final Value v : _parameters) {
            vars.addAll(v.getVariables());
        }
        return vars;
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.edit.Value#on(org.nrg.dicom.mizer.objects.DicomObjectI)
     */
    public String on(final DicomObjectI dicomObject) throws ScriptEvaluationException {
        final List<String> values = Lists.newArrayListWithExpectedSize(_parameters.size());
        for (final Value v : _parameters) {
            values.add(v.on(dicomObject));
        }
        return _scope.getValue(_label, _tag, dicomObject.getString(_tag), values);
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.edit.Value#on(java.util.Map)
     */
    public String on(final Map<Integer,String> m) throws ScriptEvaluationException {
        final List<String> values = Lists.newArrayListWithExpectedSize(_parameters.size());
        for (final Value v : _parameters) {
            values.add(v.on(m));
        }
        return _scope.getValue(_label, _tag, m.get(_tag), values);
    }
    
    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.Value#replaceVariable(org.nrg.dcm.edit.Variable)
     */
    public void replace(final Variable var) {
        for (final Value val : _parameters) {
            val.replace(var);
        }
    }
}
