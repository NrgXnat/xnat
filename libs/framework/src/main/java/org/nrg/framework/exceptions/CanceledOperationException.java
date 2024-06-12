/*
 * DicomEdit: org.nrg.framework.exceptions.CanceledOperationException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.framework.exceptions;

import com.google.common.base.Objects;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
@SuppressWarnings("unused")
public final class CanceledOperationException extends Exception {
    /**
     * Creates an exception object with a default message.
     */
    public CanceledOperationException() {
        this("operation canceled by user");
    }

    /**
     * Creates a new exception with the specified message.
     *
     * @param message The detailed exception message.
     */
    public CanceledOperationException(final String message) {
        super(message);
    }

    /**
     * Creates a new exception with the specified root cause.
     *
     * @param cause The root cause of the exception.
     */
    public CanceledOperationException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new exception with the specified message and root cause.
     *
     * @param message The detailed exception message.
     * @param cause   The root cause of the exception.
     */
    public CanceledOperationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return Objects.hashCode(getMessage(), getCause());
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(final Object o) {
        if (o instanceof CanceledOperationException) {
            final Exception e = (Exception)o;
            return Objects.equal(getMessage(), e.getMessage()) &&
            Objects.equal(getCause(), e.getCause());
        } else {
            return false;
        }
    }
}
