/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.UppercaseFunction
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
import org.nrg.dicom.mizer.values.AbstractMizerValue;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.List;

/**
 * Hash the referenced uids and prepend the prefix.
 *
 */
public class LookupFunction extends AbstractScriptFunction {

    private static final Logger logger = LoggerFactory.getLogger(LookupFunction.class);

    public LookupFunction() {
        super("lookup", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
    }

    @Override
    public Value apply(final List<Value> values, DicomObjectI dicomObject) throws ScriptEvaluationException {

        if (values.size() == 2) {

            final String keyType = values.get(0).asString();
            final String key = (values.get(1).asObject() instanceof TagPath)? dicomObject.getString((TagPath)values.get(1).asObject()): values.get(1).asString();
            final String newValueString = LookupManager.getInstance().lookup( keyType, key);
            return new ConstantValue( newValueString);

        } else {
            illegalArguments( values);
        }

        return AbstractMizerValue.VOID;
    }

    protected void illegalArguments(List<Value> values) {

        String arguments = "";
        switch (values.size()) {
            case 0:
                arguments = "null";
                break;
            default:
                for( Value v: values) {
                    arguments += v.asString() + ", ";
                }
        }

        MessageFormat message = new MessageFormat("lookup illegal arguments: '{0}'. Expect tagPath-string, keyvalue-string");
        throw new ScriptEvaluationRuntimeException(message.format(arguments));
    }

}
