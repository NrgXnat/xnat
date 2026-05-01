/*
 * mizer: org.nrg.dicom.mizer.exceptions.MizerException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.exceptions;

import org.nrg.dicom.mizer.service.MizerService;

/**
 * The primary class used to distinguish exceptions from operations in the {@link MizerService} and its components.
 */
public class MizerException extends Exception {
    /**
     * Creates a new exception with the specified message.
     *
     * @param message The detailed exception message.
     */
    public MizerException(final String message) {
        super(message);
    }

    /**
     * Creates a new exception with the specified message and root cause.
     *
     * @param cause The root cause of the exception.
     */
    public MizerException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new exception with the specified root cause.
     *
     * @param message The detailed exception message.
     * @param cause   The root cause of the exception.
     */
    public MizerException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
