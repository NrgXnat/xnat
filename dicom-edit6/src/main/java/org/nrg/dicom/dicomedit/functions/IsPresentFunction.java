/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.UppercaseFunction
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

public class IsPresentFunction extends AbstractScriptFunction {

    public IsPresentFunction() {
        super("isPresent", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
    }

    @Override
    public Value apply(List<Value> values, DicomObjectI dicomObject) {
        for (Value value : flattenValueList(values)) {
            if (value.asObject() instanceof TagPath) {
                TagPath tp = (TagPath) value.asObject();
                if (!tp.isSingular()) {
                    throw new IllegalArgumentException("Argument is not a singular tagPath: " + value.asString());
                }
                if (!dicomObject.resolveTagPath(tp).isPresent()) {
                    return new ConstantValue(false);
                }
            } else {
                throw new IllegalArgumentException("Argument is not a tagPath: " + value.asString());
            }
        }
        return new ConstantValue(true);
    }
}
