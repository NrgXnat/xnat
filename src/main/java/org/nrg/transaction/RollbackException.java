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
