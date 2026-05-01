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
import java.util.Properties;

/**
 * Base class for script functions not needing external context.
 */
public abstract class AbstractScriptFunction extends BaseFunction {

    public AbstractScriptFunction(String name, String nameSpace, String usage, String description) {
        super(name, nameSpace, usage, description);
    }

    public AbstractScriptFunction(Properties properties) {
        super(properties);
    }

    abstract Value apply(List<Value> args, DicomObjectI dicomObject) throws ScriptEvaluationException;
    public Value apply(List<Value> args, Map<String, Variable> variables, DicomObjectI dicomObject) throws ScriptEvaluationException {
        return apply(args,dicomObject);
    }

}
