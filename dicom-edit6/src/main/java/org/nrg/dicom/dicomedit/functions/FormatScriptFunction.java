/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.FormatFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;


import org.nrg.dicom.mizer.exceptions.ScriptEvaluationRuntimeException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class FormatScriptFunction extends AbstractScriptFunction {

    public FormatScriptFunction() {
        super("format", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
    }

    @Override
    public Value apply(List<Value> values, DicomObjectI dicomObject) {
        try {
            List<Value> subList = flattenValueList(values.subList(1, values.size()));
            List<String> strings = new ArrayList<>( subList.size());
            for( Value v: subList) {
                if( v.asObject() instanceof TagPath) {
                    strings.add( dicomObject.getString( (TagPath) v.asObject()));
                } else {
                    strings.add( v.asString());
                }
            }
            return values.size() > 0 ? new ConstantValue(MessageFormat.format(values.get(0).asString(), strings.toArray())) : null;
        } catch( IllegalArgumentException e) {
            throw new ScriptEvaluationRuntimeException(getFormattedErrorMessage(values));
        }
    }
}
