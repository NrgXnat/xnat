package org.nrg.xdat.security.user.exceptions;

public class FailedLoginException extends Exception {
    public String FAILED_LOGIN = null;

    public FailedLoginException(String message, String login) {
        super(message);
        FAILED_LOGIN = login;
    }
}