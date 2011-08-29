/**
 * DuplicateDefinitionException
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.services;

import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.notify.entities.Definition;

/**
 * Thrown when there's an attempt to create a duplicate {@link Definition definition} object.
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
public class DuplicateDefinitionException extends NrgServiceException {

    /**
     * Default constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#DuplicateDefinition}.
     */
    public DuplicateDefinitionException() {
        super();
        setServiceError(NrgServiceError.DuplicateDefinition);
    }

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#DuplicateDefinition}.
     */
    public DuplicateDefinitionException(String message) {
        super(message);
        setServiceError(NrgServiceError.DuplicateDefinition);
    }

    /**
     * Wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#DuplicateDefinition}.
     */
    public DuplicateDefinitionException(Throwable cause) {
        super(cause);
        setServiceError(NrgServiceError.DuplicateDefinition);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#DuplicateDefinition}.
     */
    public DuplicateDefinitionException(String message, Throwable cause) {
        super(message, cause);
        setServiceError(NrgServiceError.DuplicateDefinition);
    }

    private static final long serialVersionUID = -7767438484747548702L;
}
