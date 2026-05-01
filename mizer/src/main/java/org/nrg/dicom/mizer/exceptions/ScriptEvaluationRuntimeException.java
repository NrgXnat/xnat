/*
 * mizer: org.nrg.dicom.mizer.exceptions.ScriptEvaluationRuntimeException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.exceptions;

import org.nrg.dicom.mizer.scripts.MizerScript;

/**
 * Indicates that an unrecoverable run-time error occurred while attempting to evaluate a {@link MizerScript} object.
 */
public class ScriptEvaluationRuntimeException extends RuntimeException {
    /**
     * Creates a new exception with the specified message.
     *
     * @param message The detailed exception message.
     */
    public ScriptEvaluationRuntimeException(final String message) {
        super(message);
    }

    /**
     * Creates a new exception with the specified message and root cause.
     *
     * @param cause The root cause of the exception.
     */
    public ScriptEvaluationRuntimeException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new exception with the specified root cause.
     *
     * @param message The detailed exception message.
     * @param cause   The root cause of the exception.
     */
    public ScriptEvaluationRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
