/*
 * framework: org.nrg.framework.exceptions.NrgRuntimeException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.exceptions;

/**
 * An NRG-specific run-time exception class to allow filtering of exceptions in handlers.
 */
public class NrgRuntimeException extends RuntimeException {

    /**
     * Default constructor.
     */
    public NrgRuntimeException() {
        super();
    }

    /**
     * Default message constructor.
     *
     * @param message The message to set for the exception.
     */
    public NrgRuntimeException(String message) {
        super(message);
    }

    /**
     * Default wrapper constructor.
     *
     * @param cause The cause of the exception.
     */
    public NrgRuntimeException(Throwable cause) {
        super(cause);
    }

    /**
     * Default message and wrapper constructor.
     *
     * @param message The message to set for the exception.
     * @param cause   The cause of the exception.
     */
    public NrgRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    private static final long serialVersionUID = -836449451507257629L;
}
