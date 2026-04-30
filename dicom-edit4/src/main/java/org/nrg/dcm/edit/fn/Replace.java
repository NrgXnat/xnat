/*
 * DicomEdit: org.nrg.dcm.edit.fn.Replace
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit.fn;

import com.google.common.collect.Sets;
import org.nrg.dicom.mizer.scripts.ScriptFunction;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.AbstractMizerValue;
import org.nrg.dicom.mizer.values.ReplaceValue;
import org.nrg.dicom.mizer.values.Value;
import org.nrg.dicom.mizer.variables.Variable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
@SuppressWarnings("Duplicates")
public final class Replace implements ScriptFunction {
    public static final String name = "replace";

    @Override
    public Value apply(final List<? extends Value> args) throws ScriptEvaluationException {
        if (args == null || args.size() < 3) {
            throw new ScriptEvaluationException(name + " takes three arguments [original, pre, post]");
        }

        return new ReplaceValue(args.getFirst(), args.get(1), args.get(2));
    }
}
