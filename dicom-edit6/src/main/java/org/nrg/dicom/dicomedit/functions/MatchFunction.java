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
import org.nrg.dicom.mizer.values.MatchValue;
import org.nrg.dicom.mizer.values.Value;

import java.text.MessageFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Matches value against the regular expression regexp and returns the content of the capturing group with the given index.
 This is a more powerful and flexible method for extracting substrings than the index-based substring function.
 */
public class MatchFunction extends AbstractScriptFunction {

    public MatchFunction() {
        super("match", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
    }

    @Override
    public Value apply(List<Value> values, DicomObjectI dicomObject) throws ScriptEvaluationException {
        try {
            if (values.size() != 3) {
                throw new ScriptEvaluationException("usage: match[ value, regex, group-index ]");
            }
            String value = (values.get(0).asObject() instanceof TagPath)? dicomObject.getString((TagPath)values.get(0).asObject()): values.get(0).asString();
            String regex = values.get(1).asString();
            int index = values.get(2).asInteger();

            Pattern pattern = Pattern.compile( regex);
            Matcher matcher = pattern.matcher( value);
            return matcher.find()? new ConstantValue( matcher.group( index)): new ConstantValue( null);
        } catch( IllegalArgumentException e) {
            throw new ScriptEvaluationRuntimeException(getFormattedErrorMessage(values));
        }
    }
}
