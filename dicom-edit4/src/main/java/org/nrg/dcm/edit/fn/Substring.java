/*
 * DicomEdit: org.nrg.dcm.edit.fn.Substring
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit.fn;

import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.scripts.ScriptFunction;
import org.nrg.dicom.mizer.values.IntegerValue;
import org.nrg.dicom.mizer.values.SubstringValue;
import org.nrg.dicom.mizer.values.Value;

import java.util.List;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public final class Substring implements ScriptFunction {
    public static final String name = "substring";

    @Override
    public Value apply(final List<? extends Value> args) throws ScriptEvaluationException {
        try {
            final Value value = args.get(0);
            final int   start = ((IntegerValue) args.get(1)).getValue();
            final int   end   = ((IntegerValue) args.get(2)).getValue();
            return new SubstringValue(value, start, end);
        } catch (ClassCastException e) {
            throw new ScriptEvaluationException("substring[ string, start{integer}, end{integer}]");
        }
    }
}
