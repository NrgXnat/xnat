/*
 * org.nrg.transaction.TransactionException
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 5:31 PM
 */
package org.nrg.transaction;

public class TransactionException extends Throwable {
    /**
     * 
     */
    public TransactionException() {
    }

    /**
     * @param message
     */
    public TransactionException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public TransactionException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
