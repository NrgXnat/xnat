/*
 * notify: org.nrg.notify.exceptions.ChannelRendererProcessingException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.notify.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;

@SuppressWarnings("unused")
public class ChannelRendererProcessingException extends NrgNotificationException {

    /**
     * Default constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#ChannelRendererProcessingError}.
     */
    public ChannelRendererProcessingException() {
        super(NrgServiceError.ChannelRendererProcessingError);
    }

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#ChannelRendererProcessingError}.
     *
     * @param message The message to set for this exception.
     */
    public ChannelRendererProcessingException(final String message) {
        super(NrgServiceError.ChannelRendererProcessingError, message);
    }

    /**
     * Wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#ChannelRendererProcessingError}.
     *
     * @param cause The cause to set for this exception.
     */
    public ChannelRendererProcessingException(final Throwable cause) {
        super(NrgServiceError.ChannelRendererProcessingError, cause);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#ChannelRendererProcessingError}.
     *
     * @param message The message to set for this exception.
     * @param cause   The cause to set for this exception.
     */
    public ChannelRendererProcessingException(final String message, final Throwable cause) {
        super(NrgServiceError.ChannelRendererProcessingError, message, cause);
    }

    private static final long serialVersionUID = -7767438484747548702L;
}
