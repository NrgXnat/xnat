/*
 * dicom-edit4: org.nrg.dcm.mizer.IndexedSessionLabelFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.mizer;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import org.nrg.dcm.edit.AbstractIndexedLabelFunction;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.scripts.ScriptFunction;
import org.nrg.dcm.edit.annotations.DicomEdit4Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

@DicomEdit4Function(name = "makeSessionLabel")
public final class IndexedSessionLabelFunction extends AbstractIndexedLabelFunction implements ScriptFunction {
    /**
     * This sets up an indexed session label function for the submitted sessions.
     *
     * @param context The anonymization context.
     */
    public IndexedSessionLabelFunction(final Map<String, Object> context) {
        if (context.containsKey("sessions")) {
            //noinspection unchecked
            _sessions = (Map<String, String>) context.get("sessions");
        } else {
            _log.info("Couldn't find either a map of session labels, setting filter to empty.");
            _sessions = Collections.emptyMap();
        }
    }

    @Override
    public String toString() {
        return "makeSessionLabel(" + Joiner.on(", ").join(_sessions.keySet()) + ")";
    }

    /**
     * This returns a function that returns true in the case where the session label retrieval failed; this is sort of
     * broken but prevents an ugly infinite loop situation.
     *
     * @return Returns the function to test for availability.
     */
    @Override
    protected Function<String, Boolean> isAvailable() throws ScriptEvaluationException {
        return new Function<String, Boolean>() {
            @Override
            public Boolean apply(final String label) {
                return !_sessions.containsKey(label) && !_sessions.containsValue(label);
            }
        };
    }

    private static final Logger _log = LoggerFactory.getLogger(IndexedSessionLabelFunction.class);

    private final Map<String, String> _sessions;
}
