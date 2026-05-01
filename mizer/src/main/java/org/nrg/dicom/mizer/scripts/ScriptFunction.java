/*
 * mizer: org.nrg.dicom.mizer.scripts.ScriptFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dicom.mizer.scripts;

import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.values.Value;

import java.util.List;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public interface ScriptFunction {
    Value apply(List<? extends Value> args) throws ScriptEvaluationException;
}
