/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.LowercaseFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;

import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;

import java.text.MessageFormat;
import java.util.List;

/**
 * Created by drm on 4/24/16.
 */
public class LowercaseScriptFunction extends AbstractScriptFunction {

    public LowercaseScriptFunction() {
        super("lowercase", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
    }

    @Override
    public Value apply(List<Value> values, DicomObjectI dicomObject) throws ScriptEvaluationException {
        if( values.size() == 1 ) {
            final String value = (values.get(0).asObject() instanceof TagPath)? dicomObject.getString((TagPath)values.get(0).asObject()): values.get(0).asString();
            if (value == null) {
                throw new ScriptEvaluationException("Unable to convert value for " + values.get(0) + " to lowercase. Value is null.");
            }
            return new ConstantValue(value.toLowerCase());
        }
        else {
            throw new ScriptEvaluationException(MessageFormat.format("Illegal number of arguments in script function 'lowwercase'. Expect 1, has {0}", values.size()));
        }
    }
}
