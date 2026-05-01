package org.nrg.xft.exception;

public class XftItemException extends Exception {
    public XftItemException() {
        super();
    }

    public XftItemException(final String message) {
        super(message);
    }

    public XftItemException(final Throwable cause) {
        super(cause);
    }

    public XftItemException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
