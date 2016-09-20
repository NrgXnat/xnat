/*
 * org.nrg.notify.exceptions.NrgNotificationException
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.notify.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;

abstract public class NrgNotificationException extends NrgServiceException {

    /**
     * Default constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#ChannelRendererNotFound}.
     *
     * @param error    The NRG service error code to set for this exception.
     */
    public NrgNotificationException(final NrgServiceError error) {
        super(error);
    }

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#ChannelRendererNotFound}.
     *
     * @param error    The NRG service error code to set for this exception.
     * @param message    The message to set for this exception.
     */
    public NrgNotificationException(final NrgServiceError error, final String message) {
        super(error, message);
    }

    /**
     * Wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#ChannelRendererNotFound}.
     *
     * @param error    The NRG service error code to set for this exception.
     * @param cause    The cause to set for this exception.
     */
    public NrgNotificationException(final NrgServiceError error, final Throwable cause) {
        super(error, cause);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#ChannelRendererNotFound}.
     *
     * @param error    The NRG service error code to set for this exception.
     * @param message    The message to set for this exception.
     * @param cause    The cause to set for this exception.
     */
    public NrgNotificationException(final NrgServiceError error, final String message, final Throwable cause) {
        super(error, message, cause);
    }

    private static final long serialVersionUID = -9129204298278181426L;
}
