package org.nrg.xdat.security.user.exceptions;

public class PasswordAuthenticationException extends FailedLoginException {
    public PasswordAuthenticationException(String login) {
        super("Invalid Login and/or Password", login);
    }
}