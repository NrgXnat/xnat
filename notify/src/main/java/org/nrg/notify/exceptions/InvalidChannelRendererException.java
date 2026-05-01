/*
 * notify: org.nrg.notify.exceptions.InvalidChannelRendererException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.notify.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;

@SuppressWarnings("unused")
public class InvalidChannelRendererException extends NrgNotificationException {

    /**
     * Default constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#NoMatchingCategory}.
     */
    public InvalidChannelRendererException() {
        super(NrgServiceError.NoMatchingCategory);
    }

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#NoMatchingCategory}.
     *
     * @param message The message to set for this exception.
     */
    public InvalidChannelRendererException(String message) {
        super(NrgServiceError.NoMatchingCategory);
    }

    /**
     * Wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#NoMatchingCategory}.
     *
     * @param cause The cause to set for this exception.
     */
    public InvalidChannelRendererException(Throwable cause) {
        super(NrgServiceError.NoMatchingCategory, cause);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#NoMatchingCategory}.
     *
     * @param message The message to set for this exception.
     * @param cause   The cause to set for this exception.
     */
    public InvalidChannelRendererException(String message, Throwable cause) {
        super(NrgServiceError.NoMatchingCategory, message, cause);
    }

    private static final long serialVersionUID = -7767438484747548702L;
}
