/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.ReplaceFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;


import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;

import java.util.List;

/**
 * Created by drm on 4/24/16.
 */
public class ReplaceScriptFunction extends AbstractScriptFunction {

    public ReplaceScriptFunction() {
        super("replace", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
    }

    @Override
    public Value apply(List<Value> values, DicomObjectI dicomObject) {
        final String string = (values.get(0).asObject() instanceof TagPath)? dicomObject.getString((TagPath)values.get(0).asObject()): values.get(0).asString();
        final String find    = values.get(1).asString();
        final String replace = values.get(2).asString();

        return new ConstantValue(string.replace(find, replace));
    }
}
