/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.DefaultFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;

import com.google.common.base.Joiner;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;

import java.util.List;

/**
 * Created by drm on 4/20/16.
 */
public class DefaultScriptFunction extends AbstractScriptFunction {

    static String FUNCTION_NAME = "default";

    public DefaultScriptFunction() {
        super(FUNCTION_NAME, AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
    }

    public Value apply(final List<Value> args, DicomObjectI dicomObject) {
        return new ConstantValue("[" + FUNCTION_NAME + "(" + Joiner.on(", ").join(args) + "]");
    }

    @Override
    public String getUsage() {
        return "Usage: default function just echos arguments.";
    }
}
