/*
 * mizer: org.nrg.dicom.mizer.values.VariableValue
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class VariableValue extends AbstractMizerValue {
    public VariableValue(final Variable variable) {
        _variable = variable;
    }

    /**
     * {@inheritDoc}
     */
    public SortedSet<Long> getTags() { return EMPTY_TAGS; }

    /**
     * {@inheritDoc}
     */
    public Set<Variable> getVariables() {
        return Collections.singleton(_variable);
    }

    /**
     * {@inheritDoc}
     */
    public String on(final DicomObjectI dicomObject) throws ScriptEvaluationException {
        return evaluate(dicomObject);
    }

    /**
     * {@inheritDoc}
     */
    public String on(final Map<Integer,String> map) throws ScriptEvaluationException {
        return evaluate(map);
    }

    /**
     * {@inheritDoc}
     */
    public void replace(final Variable variable) {
        if (variable.equals(_variable)) {
            _variable = variable;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return super.toString() + "(" + _variable + ")";
    }

    private String evaluate(final Object input) throws ScriptEvaluationException {
        final boolean isDicomObject = input instanceof DicomObjectI;

        logger.trace("evaluating {}", isDicomObject ? "DICOM object" : "map", toString());

        final Value value = _variable.getValue();
        logger.trace("primary value = {}", value);
        if (value != null) {
            //noinspection unchecked
            final String evaluated = isDicomObject ? value.on((DicomObjectI) input) : value.on((Map<Integer, String>) input);
            logger.trace("evaluated = {}", evaluated);
            return evaluated;
        }
        final Value initial = _variable.getInitialValue();
        logger.trace("initial value = {}", initial);
        if (initial == null) {
            return null;
        }

        //noinspection unchecked
        final String evaluated = isDicomObject ? initial.on((DicomObjectI) input) : initial.on((Map<Integer, String>) input);
        logger.trace("evaluated = {}", evaluated);
        return evaluated;
    }

    private static final Logger logger = LoggerFactory.getLogger(VariableValue.class);

    private Variable _variable;
}
