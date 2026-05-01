/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.ReplaceFunction
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NormalizeStringFunction extends AbstractScriptFunction {

    private Logger logger = LoggerFactory.getLogger(NormalizeStringFunction.class);
    private final String DEFAULT_REPLACEMENT_STRING = "_";

    public NormalizeStringFunction() {
        super("normalizeString", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
    }

    @Override
    public Value apply(List<Value> values, DicomObjectI dicomObject) throws ScriptEvaluationException {
        final String string;
        final String replacementString;

        if (values.isEmpty() || values.size() > 2) {
            String msg = String.format("Usage: normalizeString[<tagpath> | <string>, [<replacementString>] ] (default replacement is '%s'.", DEFAULT_REPLACEMENT_STRING);
            throw new ScriptEvaluationException(msg);
        }

        if (values.get(0).asObject() instanceof TagPath) {
            string = dicomObject.getString((TagPath) values.get(0).asObject());
        } else {
            string = values.get(0).asString();
        }
        replacementString = (values.size() == 2) ? values.get(1).asString() : DEFAULT_REPLACEMENT_STRING;

        String normalizedString = null;
        if (string != null) {
//            normalizedString = Normalizer.normalize( string, Normalizer.Form.NFD)
//                    .replaceAll( "[^\\p{ASCII}]", replacementString);
            normalizedString = string.replaceAll("[^\\p{ASCII}]", replacementString);
        }

        return new ConstantValue(normalizedString);
    }
}
