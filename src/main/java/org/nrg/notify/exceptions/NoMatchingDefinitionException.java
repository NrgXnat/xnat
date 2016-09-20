/*
 * org.nrg.notify.exceptions.NoMatchingDefinitionException
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.notify.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;

@SuppressWarnings("unused")
public class NoMatchingDefinitionException extends NrgNotificationException {

    /**
     * Default constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#NoMatchingDefinition}.
     */
    public NoMatchingDefinitionException() {
        super(NrgServiceError.NoMatchingDefinition);
    }

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#NoMatchingDefinition}.
     *
     * @param message The message to set for this exception.
     */
    public NoMatchingDefinitionException(final String message) {
        super(NrgServiceError.NoMatchingDefinition, message);
    }

    /**
     * Wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#NoMatchingDefinition}.
     *
     * @param cause The cause to set for this exception.
     */
    public NoMatchingDefinitionException(final Throwable cause) {
        super(NrgServiceError.NoMatchingDefinition, cause);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#NoMatchingDefinition}.
     *
     * @param message The message to set for this exception.
     * @param cause   The cause to set for this exception.
     */
    public NoMatchingDefinitionException(final String message, final Throwable cause) {
        super(NrgServiceError.NoMatchingDefinition, message, cause);
    }

    private static final long serialVersionUID = -7767438484747548702L;
}
