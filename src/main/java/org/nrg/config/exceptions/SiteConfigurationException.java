package org.nrg.config.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;

/**
 * Contains exceptions thrown by the site configuration service.
 */
public class SiteConfigurationException extends NrgServiceException {
    /**
     * Default constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#Default}.
     */
    public SiteConfigurationException() {
        super();
        setServiceError(NrgServiceError.Default);
    }

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#Default}.
     */
    public SiteConfigurationException(String message) {
        super(message);
        setServiceError(NrgServiceError.Default);
    }

    /**
     * Wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#Default}.
     */
    public SiteConfigurationException(Throwable cause) {
        super(cause);
        setServiceError(NrgServiceError.Default);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#Default}.
     */
    public SiteConfigurationException(String message, Throwable cause) {
        super(message, cause);
        setServiceError(NrgServiceError.Default);
    }

    /**
     * Default error code constructor. This sets the {@link #getServiceError() service error}
     * property to the submitted {@link NrgServiceError} value.
     */
    public SiteConfigurationException(NrgServiceError error) {
        super();
        setServiceError(error);
    }

    /**
     * Error code message constructor. This sets the {@link #getServiceError() service error}
     * property to the submitted {@link NrgServiceError} value.
     */
    public SiteConfigurationException(NrgServiceError error, String message) {
        super(message);
        setServiceError(error);
    }

    /**
     * Error code wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to the submitted {@link NrgServiceError} value.
     */
    public SiteConfigurationException(NrgServiceError error, Throwable cause) {
        super(cause);
        setServiceError(error);
    }

    /**
     * Error code and message wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to the submitted {@link NrgServiceError} value.
     */
    public SiteConfigurationException(NrgServiceError error, String message, Throwable cause) {
        super(message, cause);
        setServiceError(error);
    }
}
