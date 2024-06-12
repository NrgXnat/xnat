/*
 * framework: org.nrg.framework.exceptions.NrgException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.exceptions;

/**
 * An NRG-specific exception class to allow filtering of exceptions in handlers.
 */
public class NrgException extends Exception {

    /**
     * Default constructor.
     */
    public NrgException() {
        super();
    }

    /**
     * Default message constructor.
     *
     * @param message The message to set for the exception.
     */
    public NrgException(String message) {
        super(message);
    }

    /**
     * Default wrapper constructor.
     *
     * @param cause The cause of the exception.
     */
    public NrgException(Throwable cause) {
        super(cause);
    }

    /**
     * Default message and wrapper constructor.
     *
     * @param message The message to set for the exception.
     * @param cause   The cause of the exception.
     */
    public NrgException(String message, Throwable cause) {
        super(message, cause);
    }

    private static final long serialVersionUID = -836449451507257629L;
}
