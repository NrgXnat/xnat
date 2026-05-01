/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.ConcatenateFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;


import com.google.common.base.Joiner;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;

import java.util.ArrayList;
import java.util.List;

public class ConcatenateScriptFunction extends AbstractScriptFunction {

    public ConcatenateScriptFunction() {
        super("concatenate", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
    }

    public Value apply(final List<Value> args, DicomObjectI dicomObject) {
        List<String> strings = new ArrayList<>(args.size());
        for( Value v: args) {
            if( v.asObject() instanceof TagPath) {
                strings.add( dicomObject.getString( (TagPath) v.asObject()));
            } else {
                strings.add( v.asString());
            }
        }
        return new ConstantValue(Joiner.on("").join(strings));
    }
}
