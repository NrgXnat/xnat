/*
 * mizer: org.nrg.dicom.mizer.exceptions.ScriptEvaluationException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dicom.mizer.exceptions;

import org.nrg.dicom.mizer.scripts.MizerScript;

/**
 * Indicates that an error occurred while attempting to evaluate a {@link MizerScript} object.
 */
public class RejectedInstanceException extends ScriptEvaluationRuntimeException {
    /**
     * Creates a new exception with the specified message.
     *
     * @param message The detailed exception message.
     */
    public RejectedInstanceException(final String message) {
        super(message);
    }

    /**
     * Creates a new exception with the specified message and root cause.
     *
     * @param cause The root cause of the exception.
     */
    public RejectedInstanceException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new exception with the specified root cause.
     *
     * @param message The detailed exception message.
     * @param cause   The root cause of the exception.
     */
    public RejectedInstanceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
