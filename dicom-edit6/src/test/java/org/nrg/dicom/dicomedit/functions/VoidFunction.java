/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.FormatFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;

import org.nrg.dicom.dicomedit.annotation.DicomEditFunction;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.AbstractMizerValue;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

/**
 * Always returns VOID.
 * Useful in tests.
 */
@DicomEditFunction(name = "void", namespace = "")
public class VoidFunction extends AbstractScriptFunction {
    private Logger logger = LoggerFactory.getLogger( VoidFunction.class);

    public VoidFunction(Properties properties) {
        super(properties);
    }

    @Override
    public Value apply(List<Value> values, DicomObjectI dicomObject) {
        logger.trace("Apply void function.");
        return AbstractMizerValue.VOID;
    }
}
