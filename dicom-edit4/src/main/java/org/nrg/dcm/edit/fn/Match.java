/*
 * DicomEdit: org.nrg.dcm.edit.fn.Match
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
import org.nrg.dicom.mizer.values.MatchValue;
import org.nrg.dicom.mizer.values.Value;

import java.util.List;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public final class Match implements ScriptFunction {
    public static final String name = "match";

    @Override
    public Value apply(List<? extends Value> args) throws ScriptEvaluationException {
        final Value value   = args.get(0);
        final Value pattern = args.get(1);
        final int   group   = ((IntegerValue) args.get(2)).getValue();

        return new MatchValue(value, pattern, group);
    }
}
