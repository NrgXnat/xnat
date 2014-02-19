/**
 * NoMatchingDefinitionException
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.notify.entities.Definition;

public class NoMatchingDefinitionException extends NrgNotificationException {

    /**
     * Default constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#NoMatchingDefinition}.
     */
    public NoMatchingDefinitionException() {
        super();
        setServiceError(NrgServiceError.NoMatchingDefinition);
    }

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#NoMatchingDefinition}.
     */
    public NoMatchingDefinitionException(String message) {
        super(message);
        setServiceError(NrgServiceError.NoMatchingDefinition);
    }

    /**
     * Wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#NoMatchingDefinition}.
     */
    public NoMatchingDefinitionException(Throwable cause) {
        super(cause);
        setServiceError(NrgServiceError.NoMatchingDefinition);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#NoMatchingDefinition}.
     */
    public NoMatchingDefinitionException(String message, Throwable cause) {
        super(message, cause);
        setServiceError(NrgServiceError.NoMatchingDefinition);
    }

    private static final long serialVersionUID = -7767438484747548702L;
}
