/**
 * NrgNotificationException
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 30, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;

abstract public class NrgNotificationException extends NrgServiceException {

    /**
     * Default constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#ChannelRendererNotFound}.
     */
    public NrgNotificationException() {
        super();
    }

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#ChannelRendererNotFound}.
     */
    public NrgNotificationException(String message) {
        super(message);
    }

    /**
     * Wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#ChannelRendererNotFound}.
     */
    public NrgNotificationException(Throwable cause) {
        super(cause);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#ChannelRendererNotFound}.
     */
    public NrgNotificationException(String message, Throwable cause) {
        super(message, cause);
    }

    private static final long serialVersionUID = -9129204298278181426L;
}
