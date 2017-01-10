/*
 * mail: org.nrg.mail.exceptions.NrgMailException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.mail.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;

@SuppressWarnings("unused")
public class NrgMailException extends NrgServiceException {
    /**
     * Default constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#GenericMailError}.
     */
    @SuppressWarnings("unused")
    public NrgMailException() {
        super(NrgServiceError.GenericMailError);
    }

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#GenericMailError}.
     * @param message    The message to set for this exception.
     */
    public NrgMailException(final String message) {
        super(NrgServiceError.GenericMailError, message);
    }

    /**
     * Wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#GenericMailError}.
     * @param cause    The cause to set for this exception.
     */
    public NrgMailException(final Throwable cause) {
        super(NrgServiceError.GenericMailError, cause);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#GenericMailError}.
     * @param message    The message to set for this exception.
     * @param cause    The cause to set for this exception.
     */
    public NrgMailException(final String message, final Throwable cause) {
        super(NrgServiceError.GenericMailError, message, cause);
    }

    /**
     * Mail exception with a specific error.
     * @param error    The error to set for this exception.
     */
    protected NrgMailException(final NrgServiceError error) {
        super(error);
    }

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#GenericMailError}.
     * @param error    The error to set for this exception.
     * @param message    The message to set for this exception.
     */
    protected NrgMailException(final NrgServiceError error, final String message) {
        super(error, message);
    }

    /**
     * Wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#GenericMailError}.
     * @param error    The error to set for this exception.
     * @param cause    The cause to set for this exception.
     */
    protected NrgMailException(final NrgServiceError error, final Throwable cause) {
        super(error, cause);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#GenericMailError}.
     * @param error    The error to set for this exception.
     * @param message    The message to set for this exception.
     * @param cause    The cause to set for this exception.
     */
    protected NrgMailException(final NrgServiceError error, final String message, final Throwable cause) {
        super(error, message, cause);
    }

    private static final long serialVersionUID = 485915249241211527L;
}
