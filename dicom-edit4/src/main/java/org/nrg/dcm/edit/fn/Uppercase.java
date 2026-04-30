/*
 * DicomEdit: org.nrg.dcm.edit.fn.Uppercase
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit.fn;

import org.nrg.dicom.mizer.scripts.ScriptFunction;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.MizerValueShim;
import org.nrg.dicom.mizer.values.Value;

import java.util.List;
import java.util.Map;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public final class Uppercase implements ScriptFunction {
    public static final String name = "uppercase";

    /* (non-Javadoc)
     * @see org.nrg.dicom.mizer.scripts.ScriptFunction#apply(java.util.List)
     */
    public Value apply(final List<? extends Value> args) throws ScriptEvaluationException {
        if (args == null || args.size() == 0) {
            throw new ScriptEvaluationException(name + " requires one argument");
        }
        return new MizerValueShim(args.getFirst()) {
            public String on(final Map<Integer, String> map) throws ScriptEvaluationException {
                return toUpper(getValue().on(map));
            }

            public String on(final DicomObjectI dicomObject) throws ScriptEvaluationException {
                return toUpper(getValue().on(dicomObject));
            }
        };
    }

    private static String toUpper(final String s) {
        return null == s ? null : s.toUpperCase();
    }
}
