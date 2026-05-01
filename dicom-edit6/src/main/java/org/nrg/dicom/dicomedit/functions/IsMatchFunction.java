/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.FormatFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;


import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationRuntimeException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
    Returns true if value matches against the regular expression regexp
 */
public class IsMatchFunction extends AbstractScriptFunction {

    public IsMatchFunction() {
        super("ismatch", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
    }

    @Override
    public Value apply(List<Value> values, DicomObjectI dicomObject) throws ScriptEvaluationException {
        try {
            if (values.size() != 2) {
                throw new ScriptEvaluationException("usage: ismatch[ value, regex]");
            }
            String value = (values.get(0).asObject() instanceof TagPath)? dicomObject.getString((TagPath)values.get(0).asObject()): values.get(0).asString();
            String regex = values.get(1).asString();

            Pattern pattern = Pattern.compile( regex);
            Matcher matcher = pattern.matcher( value);
            return new ConstantValue( matcher.matches());
        } catch( IllegalArgumentException e) {
            throw new ScriptEvaluationRuntimeException(getFormattedErrorMessage(values));
        }
    }
}
