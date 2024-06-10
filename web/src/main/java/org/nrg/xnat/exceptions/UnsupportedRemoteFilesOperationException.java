package org.nrg.xnat.exceptions;


public class UnsupportedRemoteFilesOperationException extends Exception {
    public UnsupportedRemoteFilesOperationException(final String message) {
        super(message);
    }

    public UnsupportedRemoteFilesOperationException(final String message, final Throwable e) {
        super(message, e);
    }

    public UnsupportedRemoteFilesOperationException(final Throwable e) {
        super(e);
    }
}
