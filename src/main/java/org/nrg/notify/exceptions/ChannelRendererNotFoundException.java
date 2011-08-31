/**
 * ChannelRendererNotFoundException
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

/**
 * Thrown when there's an attempt to create a duplicate {@link Definition definition} object.
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
public class ChannelRendererNotFoundException extends NrgNotificationException {

    /**
     * Default constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#ChannelRendererNotFound}.
     */
    public ChannelRendererNotFoundException() {
        super();
        setServiceError(NrgServiceError.ChannelRendererNotFound);
    }

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#ChannelRendererNotFound}.
     */
    public ChannelRendererNotFoundException(String message) {
        super(message);
        setServiceError(NrgServiceError.ChannelRendererNotFound);
    }

    /**
     * Wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#ChannelRendererNotFound}.
     */
    public ChannelRendererNotFoundException(Throwable cause) {
        super(cause);
        setServiceError(NrgServiceError.ChannelRendererNotFound);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#ChannelRendererNotFound}.
     */
    public ChannelRendererNotFoundException(String message, Throwable cause) {
        super(message, cause);
        setServiceError(NrgServiceError.ChannelRendererNotFound);
    }

    private static final long serialVersionUID = -7767438484747548702L;
}
