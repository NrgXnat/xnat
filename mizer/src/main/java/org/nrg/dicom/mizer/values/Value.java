/*
 * mizer: org.nrg.dicom.mizer.values.Value
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.values;

import com.google.common.collect.ImmutableSortedSet;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.variables.Variable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * Represents a value extracted from a script processed by a Mizer implementation.
 */
public interface Value {
    Set<Variable> getVariables();

    void replace(Variable v);

    SortedSet<Long> getTags();

    String on(final DicomObjectI dicomObject) throws ScriptEvaluationException;

    String on(final Map<Integer, String> map) throws ScriptEvaluationException;

    Object asObject();

    Boolean asBoolean();

    Integer asInteger();

    Double asDouble();

    String asString();

    @SuppressWarnings("unchecked")
    List<Value> asValueList();

    boolean isBoolean();

    boolean isInteger();

    boolean isDouble();

    boolean isScalar();

    SortedSet<Long>     EMPTY_TAGS      = ImmutableSortedSet.of();
    SortedSet<Variable> EMPTY_VARIABLES = ImmutableSortedSet.of();
}
