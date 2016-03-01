/*
 * ChannelRendererNotFoundException
 * (C) 2016 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 */
package org.nrg.notify.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;

@SuppressWarnings("unused")
public class ChannelRendererNotFoundException extends NrgNotificationException {

    /**
     * Default constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#ChannelRendererNotFound}.
     */
    public ChannelRendererNotFoundException() {
        super(NrgServiceError.ChannelRendererNotFound);
    }

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#ChannelRendererNotFound}.
     *
     * @param message    The message to set for this exception.
     */
    public ChannelRendererNotFoundException(final String message) {
        super(NrgServiceError.ChannelRendererNotFound, message);
    }

    /**
     * Wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#ChannelRendererNotFound}.
     *
     * @param cause    The cause to set for this exception.
     */
    public ChannelRendererNotFoundException(final Throwable cause) {
        super(NrgServiceError.ChannelRendererNotFound, cause);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#ChannelRendererNotFound}.
     *
     * @param message    The message to set for this exception.
     * @param cause    The cause to set for this exception.
     */
    public ChannelRendererNotFoundException(final String message, final Throwable cause) {
        super(NrgServiceError.ChannelRendererNotFound, message, cause);
    }

    private static final long serialVersionUID = -7767438484747548702L;
}
