/*
 * DicomEdit: org.nrg.dcm.edit.Operation
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.edit;

import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.variables.Variable;
import org.nrg.dicomtools.exceptions.AttributeException;

import java.util.Map;
import java.util.Set;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public interface Operation {
    /**
     * Creates an Action that is a concrete implementation of applying this
     * Operation to the given DicomObject.
     *
     * @param dicomObject DicomObject to act on
     *
     * @return The action object.
     *
     * @throws AttributeException When an error occurs with the attributes.
     * @throws ScriptEvaluationException When an error occurs with the script.
     */
    Action makeAction(final DicomObjectI dicomObject) throws AttributeException, ScriptEvaluationException;

    /**
     * Compute the value that results from applying this Operation to
     * the given object context.
     *
     * @param values Map:Tag(Integer) -&gt; Value(String) representing the object of the operation
     *
     * @return String value
     *
     * @throws ScriptEvaluationException When an error occurs with the script.
     */
    String apply(final Map<Integer, String> values) throws ScriptEvaluationException;

    boolean affects(final int tag);

    /**
     * Maximum tag value potentially required for or modified by this operation.
     *
     * @return The maximum tag value.
     */
    long getTopTag();

    /**
     * Gets a list of the tags referenced by this operation.
     *
     * @return The maximum tag value.
     */
    Set<Long> getScriptTags();

    /**
     * Returns the name of this operation
     *
     * @return String name of this operation
     */
    String getName();

    /**
     * Uses the provided Variable v to replace any similarly-named Variables
     * inside this Operation; this is for value unification across scripts.
     *
     * @param variable The variable to use.
     */
    void replace(final Variable variable);
}
