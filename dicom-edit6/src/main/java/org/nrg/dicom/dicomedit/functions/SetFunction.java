/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.HashUIDFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;

import org.nrg.dicom.dicomedit.TagPathFactory;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.AbstractMizerValue;
import org.nrg.dicom.mizer.values.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Set the attribute
 *
 * The tag-path string is in the direct notation.
 */
public class SetFunction extends AbstractScriptFunction {
    private static final Logger logger = LoggerFactory.getLogger(SetFunction.class);

    public SetFunction() {
        super("set", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
    }

    @Override
    public Value apply(final List<Value> values, DicomObjectI dicomObject) throws ScriptEvaluationException {
        if (values.size() != 2) {
            throw new ScriptEvaluationException("usage: set[tagpath-string (direct notation), value]");
        }

        Value value = values.get(0);
        String tagPathString = value.asString();
        int[] ints = TagPathFactory.createTagPathInstance( tagPathString);

        value = values.get(1);
        dicomObject.putString( ints, value.asString());

        return AbstractMizerValue.VOID;
    }

}
