/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.HashUIDFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;

import org.nrg.dicom.dicomedit.pixels.PixelEditorManager;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.AbstractMizerValue;
import org.nrg.dicom.mizer.values.Value;

import java.util.List;

public class AlterPixelsFunction extends AbstractScriptFunction {

    public AlterPixelsFunction() {
        super("alterPixels", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
    }

    @Override
    public Value apply(final List<Value> values, DicomObjectI dicomObject) throws ScriptEvaluationException {
        if (values.size() != 4) {
            throw new ScriptEvaluationException("usage: alterPixels[<shape>, <shape-params>, <fill-algorithm>, <fill-params> ]");
        }

        String shape = values.get(0).asString().trim();
        String shapeProperties = values.get(1).asString().trim();
        String algorithm = values.get(2).asString().trim();
        String algorithmProperties = values.get(3).asString().trim();
        try {
            PixelEditorManager.getInstance().apply(shape, shapeProperties, algorithm, algorithmProperties, dicomObject);
        }
        catch( Exception e) {
            String msg = String.format("Error in alterPixels function. shape = %s, shapeProperties = %s, algorithm = %s, algorithmProperties = %s",
                    shape, shapeProperties, algorithm, algorithmProperties);
            throw new ScriptEvaluationException( msg, e);
        }

        return AbstractMizerValue.VOID;
    }
}
