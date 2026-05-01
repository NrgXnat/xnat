/*
 * transaction: org.nrg.transaction.RollbackException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.transaction;

/**
 * Indicates that an error occurred during a transaction rollback operation.
 */
@SuppressWarnings("unused")
public class RollbackException extends Exception {
    /**
     * Default constructor.
     */
    public RollbackException() {
    }

    /**
     * Constructor that takes an exception message.
     *
     * @param message The message.
     */
    public RollbackException(String message) {
        super(message);
    }

    /**
     * Constructor that takes a root cause exception.
     *
     * @param cause The root cause exception.
     */
    public RollbackException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor that takes an exception message and a root cause exception.
     *
     * @param message The message.
     * @param cause   The root cause exception.
     */
    public RollbackException(String message, Throwable cause) {
        super(message, cause);
    }
}
