package org.nrg.xdat.security.user.exceptions;

public class EnabledException extends FailedLoginException {
    public EnabledException(String login) {
        super("User (" + login + ") Account is disabled.", login);
    }
}