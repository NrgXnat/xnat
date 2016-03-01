/*
 * DuplicateDefinitionException
 * (C) 2016 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 */
package org.nrg.notify.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;

@SuppressWarnings("unused")
public class DuplicateDefinitionException extends NrgNotificationException {

    /**
     * Default constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#DuplicateDefinition}.
     */
    public DuplicateDefinitionException() {
        super(NrgServiceError.DuplicateDefinition);
    }

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#DuplicateDefinition}.
     *
     * @param message    The message to set for this exception.
     */
    public DuplicateDefinitionException(final String message) {
        super(NrgServiceError.DuplicateDefinition, message);
    }

    /**
     * Wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#DuplicateDefinition}.
     *
     * @param cause    The cause to set for this exception.
     */
    public DuplicateDefinitionException(final Throwable cause) {
        super(NrgServiceError.DuplicateDefinition, cause);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#DuplicateDefinition}.
     *
     * @param message    The message to set for this exception.
     * @param cause    The cause to set for this exception.
     */
    public DuplicateDefinitionException(final String message, final Throwable cause) {
        super(NrgServiceError.DuplicateDefinition, message, cause);
    }

    private static final long serialVersionUID = -7767438484747548702L;
}
