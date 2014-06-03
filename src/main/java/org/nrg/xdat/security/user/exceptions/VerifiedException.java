package org.nrg.xdat.security.user.exceptions;

public class VerifiedException extends FailedLoginException {
    public VerifiedException(String login) {
        super("User (" + login + ") Account is unverified.", login);
    }
}