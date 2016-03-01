/*
 * UnknownChannelRendererErrorException
 * (C) 2016 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 */
package org.nrg.notify.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;

@SuppressWarnings("unused")
public class UnknownChannelRendererException extends NrgNotificationException {

    /**
     * Default constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#UnknownChannelRendererError}.
     */
    public UnknownChannelRendererException() {
        super(NrgServiceError.UnknownChannelRendererError);
    }

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#UnknownChannelRendererError}.
     *
     * @param message    The message to set for this exception.
     */
    public UnknownChannelRendererException(final String message) {
        super(NrgServiceError.UnknownChannelRendererError, message);
    }

    /**
     * Wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#UnknownChannelRendererError}.
     *
     * @param cause    The cause to set for this exception.
     */
    public UnknownChannelRendererException(final Throwable cause) {
        super(NrgServiceError.UnknownChannelRendererError, cause);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#UnknownChannelRendererError}.
     *
     * @param message    The message to set for this exception.
     * @param cause    The cause to set for this exception.
     */
    public UnknownChannelRendererException(final String message, final Throwable cause) {
        super(NrgServiceError.UnknownChannelRendererError, message, cause);
    }

    private static final long serialVersionUID = -7767438484747548702L;
}
