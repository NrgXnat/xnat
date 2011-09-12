/**
 * NrgMailException
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Sep 12, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.mail.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;

/**
 * Thrown when there's an attempt to create a duplicate {@link Definition definition} object.
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
public class NrgMailException extends NrgServiceException {
    /**
     * Default constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#GenericMailError}.
     */
    public NrgMailException() {
        super();
        setServiceError(NrgServiceError.GenericMailError);
    }

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#GenericMailError}.
     */
    public NrgMailException(String message) {
        super(message);
        setServiceError(NrgServiceError.GenericMailError);
    }

    /**
     * Wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#GenericMailError}.
     */
    public NrgMailException(Throwable cause) {
        super(cause);
        setServiceError(NrgServiceError.GenericMailError);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#GenericMailError}.
     */
    public NrgMailException(String message, Throwable cause) {
        super(message, cause);
        setServiceError(NrgServiceError.GenericMailError);
    }

    private static final long serialVersionUID = 485915249241211527L;
}
