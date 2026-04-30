/*
 * DicomEdit: org.nrg.dcm.edit.fn.UrlEncode
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public final class UrlEncode implements ScriptFunction {
    public static final String name = "urlEncode";

    /* (non-Javadoc)
     * @see org.nrg.dicom.mizer.scripts.ScriptFunction#apply(java.util.List)
     */
    public Value apply(final List<? extends Value> args) throws ScriptEvaluationException {
        if (args == null || args.size() == 0) {
            throw new ScriptEvaluationException(name + " requires one argument");
        }
        return new MizerValueShim(args.getFirst()) {
            public String on(final DicomObjectI dicomObject) throws ScriptEvaluationException {
                return encode(getValue().on(dicomObject));
            }

            public String on(Map<Integer, String> m) throws ScriptEvaluationException {
                return encode(getValue().on(m));
            }
        };
    }

    private static String encode(final String s) {
        try {
            return null == s ? null : URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
