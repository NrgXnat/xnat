/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.HashFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;

import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.Value;

import java.util.List;

/**
 * Created by drm on 4/20/16.
 */
public class HashScriptFunction extends AbstractScriptFunction {

    public HashScriptFunction() {
        super("hash", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
    }

    @Override
    public Value apply(List<Value> args, DicomObjectI dicomObject) {
        return null;
    }
}
