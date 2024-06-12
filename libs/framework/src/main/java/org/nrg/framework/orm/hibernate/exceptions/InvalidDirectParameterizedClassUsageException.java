/*
 * framework: org.nrg.framework.orm.hibernate.exceptions.InvalidDirectParameterizedClassUsageException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.hibernate.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;

public class InvalidDirectParameterizedClassUsageException extends NrgServiceRuntimeException {
    /**
     * Default constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#InvalidDirectParameterizedClassUsage}.
     */
    public InvalidDirectParameterizedClassUsageException() {
        super(NrgServiceError.InvalidDirectParameterizedClassUsage);
    }

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#InvalidDirectParameterizedClassUsage}.
     *
     * @param message The message to set for the exception.
     */
    public InvalidDirectParameterizedClassUsageException(final String message) {
        super(NrgServiceError.InvalidDirectParameterizedClassUsage, message);
    }

    /**
     * Wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#InvalidDirectParameterizedClassUsage}.
     *
     * @param cause The cause of the exception.
     */
    public InvalidDirectParameterizedClassUsageException(final Throwable cause) {
        super(NrgServiceError.InvalidDirectParameterizedClassUsage, cause);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#InvalidDirectParameterizedClassUsage}.
     *
     * @param message The message to set for the exception.
     * @param cause   The cause of the exception.
     */
    public InvalidDirectParameterizedClassUsageException(final String message, final Throwable cause) {
        super(NrgServiceError.InvalidDirectParameterizedClassUsage, message, cause);
    }

    private static final long serialVersionUID = -5369229024951738808L;
}
