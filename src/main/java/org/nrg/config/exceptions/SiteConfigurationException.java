package org.nrg.config.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;

/**
 * Contains exceptions thrown by the site configuration service.
 */
public class SiteConfigurationException extends ConfigServiceException {

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#Default}.
     */
    public SiteConfigurationException(String message) {
        super(message);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#Default}.
     */
    public SiteConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Error code and message wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to the submitted {@link NrgServiceError} value.
     */
    public SiteConfigurationException(NrgServiceError error, String message, Throwable cause) {
        super(error, message, cause);
    }
}
