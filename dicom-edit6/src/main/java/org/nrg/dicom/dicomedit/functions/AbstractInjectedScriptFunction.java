/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.Function
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;

import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.Value;
import org.nrg.dicom.mizer.variables.Variable;

import java.util.List;
import java.util.Map;

/**
 * Base class for script functions needing external context.
 */
public abstract class AbstractInjectedScriptFunction extends BaseFunction {

    public AbstractInjectedScriptFunction(String name, String nameSpace, String usage, String description) {
        super(name, nameSpace, usage, description);
    }

    public abstract Value apply(List<Value> args, Map<String, Variable> variables, DicomObjectI dicomObject) throws ScriptEvaluationException;

}
