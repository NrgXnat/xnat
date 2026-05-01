/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.ReplaceFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;

import org.nrg.dicom.dicomedit.ReplaceKnownValueVisitor;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.AbstractMizerValue;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;

import java.util.List;
import java.util.stream.Collectors;

public class BlankValues extends AbstractScriptFunction {

    public BlankValues() {
        super("blankValues", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
    }

    @Override
    public Value apply(List<Value> values, DicomObjectI dicomObject) {
        final List<String> strings = asStrings(values, dicomObject);
        ReplaceKnownValueVisitor visitor = new ReplaceKnownValueVisitor(strings);
        visitor.visit(dicomObject);

        return new ConstantValue(AbstractMizerValue.VOID);
    }
}
