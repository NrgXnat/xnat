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
