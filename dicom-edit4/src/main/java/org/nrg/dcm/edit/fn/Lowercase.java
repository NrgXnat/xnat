/*
 * DicomEdit: org.nrg.dcm.edit.fn.Lowercase
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit.fn;

import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.scripts.ScriptFunction;
import org.nrg.dicom.mizer.values.LowercaseValue;
import org.nrg.dicom.mizer.values.Value;

import java.util.List;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class Lowercase implements ScriptFunction {
    public static final String name = "lowercase";

    @Override
    public Value apply(final List<? extends Value> args) throws ScriptEvaluationException {
        if (args == null || args.size() == 0) {
            throw new ScriptEvaluationException(name + " requires one argument");
        }
        return new LowercaseValue(args.get(0));
    }
}
