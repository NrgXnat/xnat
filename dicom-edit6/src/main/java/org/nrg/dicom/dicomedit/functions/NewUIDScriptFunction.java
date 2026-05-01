/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.NewUIDFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;

import org.dcm4che2.util.UIDUtils;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.UIDValue;
import org.nrg.dicom.mizer.values.Value;

import java.util.List;

/**
 * Created by drm on 4/20/16.
 */
public class NewUIDScriptFunction extends AbstractScriptFunction {

    public NewUIDScriptFunction() {
        super("newUID", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
    }

    @Override
    public Value apply(final List<Value> args, DicomObjectI dicomObject) {
        return new UIDValue(UIDUtils.createUID());
    }
}
