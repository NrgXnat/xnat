/*
 * DicomEdit: org.nrg.dcm.edit.fn.Format
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit.fn;

import java.util.List;

import org.nrg.dicom.mizer.values.MessageFormatValue;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.scripts.ScriptFunction;
import org.nrg.dicom.mizer.values.Value;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class Format implements ScriptFunction {
    public static final String name = "format";

    /* (non-Javadoc)
     * @see org.nrg.dicom.mizer.scripts.ScriptFunction#apply(java.util.List)
     */
    public Value apply(final List<? extends Value> args) throws ScriptEvaluationException {
        return new MessageFormatValue(args.get(0), args.subList(1, args.size()));
    }
}
