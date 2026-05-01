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
 * Delete the attribute
 *
 * The tag-path string is in the direct notation.
 */
public class DeleteFunction extends AbstractScriptFunction {
    private static final Logger logger = LoggerFactory.getLogger(DeleteFunction.class);

    public DeleteFunction() {
        super("delete", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
    }

    @Override
    public Value apply(final List<Value> values, DicomObjectI dicomObject) throws ScriptEvaluationException {
        if (values.isEmpty()) {
            throw new ScriptEvaluationException("usage: delete[tagpath-string (direct notation)]");
        }

        final Value value = values.get(0);
        String tagPathString = value.asString();

        int[] ints = TagPathFactory.createTagPathInstance( tagPathString);
        dicomObject.delete( ints);
        return AbstractMizerValue.VOID;
    }

}
