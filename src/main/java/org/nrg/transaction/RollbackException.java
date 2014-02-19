/*
 * org.nrg.transaction.RollbackException
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 5:31 PM
 */
package org.nrg.transaction;

public class RollbackException extends Exception {
    /**
     * 
     */
    public RollbackException() {
    }

    /**
     * @param message
     */
    public RollbackException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public RollbackException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public RollbackException(String message, Throwable cause) {
        super(message, cause);
    }
}
