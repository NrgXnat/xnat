/**
 * DuplicateSubscriberException
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;

/**
 * 
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
public class DuplicateSubscriberException extends NrgNotificationException {

    /**
     * Default constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#DuplicateSubscriber}.
     */
    public DuplicateSubscriberException() {
        super();
        setServiceError(NrgServiceError.DuplicateSubscriber);
    }

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#DuplicateSubscriber}.
     */
    public DuplicateSubscriberException(String message) {
        super(message);
        setServiceError(NrgServiceError.DuplicateSubscriber);
    }

    /**
     * Wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#DuplicateSubscriber}.
     */
    public DuplicateSubscriberException(Throwable cause) {
        super(cause);
        setServiceError(NrgServiceError.DuplicateSubscriber);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#DuplicateSubscriber}.
     */
    public DuplicateSubscriberException(String message, Throwable cause) {
        super(message, cause);
        setServiceError(NrgServiceError.DuplicateSubscriber);
    }

    private static final long serialVersionUID = 7919811547807306543L;
}
