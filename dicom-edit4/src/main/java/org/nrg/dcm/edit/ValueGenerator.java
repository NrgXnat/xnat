/*
 * DicomEdit: org.nrg.dcm.edit.ValueGenerator
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit;

import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public interface ValueGenerator {
    /**
     * Returns a new value to replace the provided previous value.
     * @param old previous value
     * @param values concrete (String) values of generator parameters
     * @return generated value
     * @throws ScriptEvaluationException When an error occurs evaluating the script.
     */
    String valueFor(String old, Iterable<String> values) throws ScriptEvaluationException;
}
