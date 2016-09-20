/*
 * org.nrg.config.exceptions.SiteConfigurationException
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.config.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;

/**
 * Contains exceptions thrown by the site configuration service.
 */
public class SiteConfigurationException extends ConfigServiceException {
    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#Default}.
     *
     * @param message Error message.
     */
    public SiteConfigurationException(String message) {
        super(message);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#Default}.
     *
     * @param message Error message.
     * @param cause   Root cause.
     */
    public SiteConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Error code and message wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to the submitted {@link NrgServiceError} value.
     *
     * @param error   Error code.
     * @param message Error message.
     * @param cause   Root cause.
     */
    @SuppressWarnings("unused")
    public SiteConfigurationException(NrgServiceError error, String message, Throwable cause) {
        super(error, message, cause);
    }
}
