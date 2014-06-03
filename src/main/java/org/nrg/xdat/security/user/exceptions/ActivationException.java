package org.nrg.xdat.security.user.exceptions;

public class ActivationException extends FailedLoginException {
    public ActivationException(String login) {
        super("User (" + login + ") Account is in quarantine.", login);
    }
}