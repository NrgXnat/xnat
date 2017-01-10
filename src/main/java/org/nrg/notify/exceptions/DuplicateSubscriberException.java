/*
 * notify: org.nrg.notify.exceptions.DuplicateSubscriberException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.notify.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;

@SuppressWarnings("unused")
public class DuplicateSubscriberException extends NrgNotificationException {

    /**
     * Default constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#NoMatchingCategory}.
     */
    public DuplicateSubscriberException() {
        super(NrgServiceError.NoMatchingCategory);
    }

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#NoMatchingCategory}.
     *
     * @param message The message to set for this exception.
     */
    public DuplicateSubscriberException(String message) {
        super(NrgServiceError.NoMatchingCategory);
    }

    /**
     * Wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#NoMatchingCategory}.
     *
     * @param cause The cause to set for this exception.
     */
    public DuplicateSubscriberException(Throwable cause) {
        super(NrgServiceError.NoMatchingCategory, cause);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#NoMatchingCategory}.
     *
     * @param message The message to set for this exception.
     * @param cause   The cause to set for this exception.
     */
    public DuplicateSubscriberException(String message, Throwable cause) {
        super(NrgServiceError.NoMatchingCategory, message, cause);
    }

    private static final long serialVersionUID = 7919811547807306543L;
}
