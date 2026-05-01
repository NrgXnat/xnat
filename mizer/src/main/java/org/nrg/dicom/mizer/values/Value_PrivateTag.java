/*
 * mizer: org.nrg.dicom.mizer.values.Value_PrivateTag
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.values;

import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.variables.Variable;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * Created by drm on 5/25/16.
 */
public class Value_PrivateTag extends AbstractMizerValue {
    public Value_PrivateTag(String group, String _privateCreatorId, String element) {
        super();
        this._group = group;
        this._privateCreatorId = _privateCreatorId;
        this._element = element;
    }

    public String getGroup() {
        return _group;
    }

    public void setGroup(String group) {
        this._group = group;
    }

    public String getPrivateCreatorId() {
        return _privateCreatorId;
    }

    public void setPrivateCreatorId(String privateCreatorId) {
        this._privateCreatorId = privateCreatorId;
    }

    public String getElement() {
        return _element;
    }

    public void setElement(String element) {
        this._element = element;
    }

    @Override
    public String toString() {
        return _group + "[" + _privateCreatorId + "]" + _element;
    }

    @Override
    public String asString() {
        return toString();
    }

    @Override
    public SortedSet<Long> getTags() {
        return EMPTY_TAGS;
    }

    @Override
    public Set<Variable> getVariables() {
        return EMPTY_VARIABLES;
    }

    @Override
    public void replace(final Variable variable) {
        //
    }

    @Override
    public String on(final DicomObjectI dicomObject) throws ScriptEvaluationException {
        return toString();
    }

    @Override
    public String on(final Map<Integer, String> map) throws ScriptEvaluationException {
        return toString();
    }

    private String _group;
    private String _privateCreatorId;
    private String _element;
}
