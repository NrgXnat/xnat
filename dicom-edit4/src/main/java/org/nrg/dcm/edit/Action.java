/*
 * DicomEdit: org.nrg.dcm.edit.Action
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit;


import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;

/**
 * Represents an action to be performed on specific DICOM attributes
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public interface Action {
    /**
     * Performs the associated action.
     *
     * @throws ScriptEvaluationException When an error occurs evaluating the script.
     */
    void apply() throws ScriptEvaluationException;
}
