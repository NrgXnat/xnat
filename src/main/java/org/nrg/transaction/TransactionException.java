/*
 * org.nrg.transaction.TransactionException
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.transaction;

/**
 * Indicates that an error occurred during a transaction operation.
 */
@SuppressWarnings("unused")
public class TransactionException extends Throwable {
    /**
     * Default constructor
     */
    public TransactionException() {
    }

    /**
     * Constructor that takes an exception message.
     *
     * @param message The message.
     */
    public TransactionException(String message) {
        super(message);
    }

    /**
     * Constructor that takes a root cause exception.
     *
     * @param cause The root cause exception.
     */
    public TransactionException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor that takes an exception message and a root cause exception.
     *
     * @param message The message.
     * @param cause   The root cause exception.
     */
    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
