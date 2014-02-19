/**
 * UnknownChannelRendererErrorException
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

public class UnknownChannelRendererException extends NrgNotificationException {

    /**
     * Default constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#UnknownChannelRendererError}.
     */
    public UnknownChannelRendererException() {
        super();
        setServiceError(NrgServiceError.UnknownChannelRendererError);
    }

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#UnknownChannelRendererError}.
     */
    public UnknownChannelRendererException(String message) {
        super(message);
        setServiceError(NrgServiceError.UnknownChannelRendererError);
    }

    /**
     * Wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#UnknownChannelRendererError}.
     */
    public UnknownChannelRendererException(Throwable cause) {
        super(cause);
        setServiceError(NrgServiceError.UnknownChannelRendererError);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#UnknownChannelRendererError}.
     */
    public UnknownChannelRendererException(String message, Throwable cause) {
        super(message, cause);
        setServiceError(NrgServiceError.UnknownChannelRendererError);
    }

    private static final long serialVersionUID = -7767438484747548702L;
}
