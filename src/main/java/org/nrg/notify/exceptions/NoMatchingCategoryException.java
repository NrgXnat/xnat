/**
 * NoMatchingCategoryException
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.exceptions;

import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.notify.entities.Category;

/**
 * Thrown when there's an attempt to retrieve a non-existent {@link Category category} object.
 * This is only thrown in particular circumstances when it's necessary to distinguish the cause
 * of a failure to create, e.g., a definition or notification.
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
public class NoMatchingCategoryException extends NrgNotificationException {

    /**
     * Default constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#NoMatchingCategory}.
     */
    public NoMatchingCategoryException() {
        super();
        setServiceError(NrgServiceError.NoMatchingCategory);
    }

    /**
     * Message constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#NoMatchingCategory}.
     */
    public NoMatchingCategoryException(String message) {
        super(message);
        setServiceError(NrgServiceError.NoMatchingCategory);
    }

    /**
     * Wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#NoMatchingCategory}.
     */
    public NoMatchingCategoryException(Throwable cause) {
        super(cause);
        setServiceError(NrgServiceError.NoMatchingCategory);
    }

    /**
     * Message and wrapper constructor. This sets the {@link #getServiceError() service error}
     * property to {@link NrgServiceError#NoMatchingCategory}.
     */
    public NoMatchingCategoryException(String message, Throwable cause) {
        super(message, cause);
        setServiceError(NrgServiceError.NoMatchingCategory);
    }

    private static final long serialVersionUID = -7767438484747548702L;
}
