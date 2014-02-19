/**
 * ChannelRendererProcessingException
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.notify.entities.Definition;

public class ChannelRendererProcessingException extends NrgNotificationException {

    /**
     * Default constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#ChannelRendererProcessingError}.
     */
    public ChannelRendererProcessingException() {
        super();
        setServiceError(NrgServiceError.ChannelRendererProcessingError);
    }

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#ChannelRendererProcessingError}.
     */
    public ChannelRendererProcessingException(String message) {
        super(message);
        setServiceError(NrgServiceError.ChannelRendererProcessingError);
    }

    /**
     * Wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#ChannelRendererProcessingError}.
     */
    public ChannelRendererProcessingException(Throwable cause) {
        super(cause);
        setServiceError(NrgServiceError.ChannelRendererProcessingError);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#ChannelRendererProcessingError}.
     */
    public ChannelRendererProcessingException(String message, Throwable cause) {
        super(message, cause);
        setServiceError(NrgServiceError.ChannelRendererProcessingError);
    }

    private static final long serialVersionUID = -7767438484747548702L;
}
