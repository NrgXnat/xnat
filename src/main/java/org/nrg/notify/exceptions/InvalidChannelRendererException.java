/**
 * InvalidChannelRendererException
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
public class InvalidChannelRendererException extends NrgNotificationException {

    /**
     * Default constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#InvalidChannelRenderer}.
     */
    public InvalidChannelRendererException() {
        super();
        setServiceError(NrgServiceError.InvalidChannelRenderer);
    }

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#InvalidChannelRenderer}.
     */
    public InvalidChannelRendererException(String message) {
        super(message);
        setServiceError(NrgServiceError.InvalidChannelRenderer);
    }

    /**
     * Wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#InvalidChannelRenderer}.
     */
    public InvalidChannelRendererException(Throwable cause) {
        super(cause);
        setServiceError(NrgServiceError.InvalidChannelRenderer);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#InvalidChannelRenderer}.
     */
    public InvalidChannelRendererException(String message, Throwable cause) {
        super(message, cause);
        setServiceError(NrgServiceError.InvalidChannelRenderer);
    }

    private static final long serialVersionUID = -7767438484747548702L;
}
