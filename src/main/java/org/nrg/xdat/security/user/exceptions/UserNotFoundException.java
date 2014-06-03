package org.nrg.xdat.security.user.exceptions;

public class UserNotFoundException extends FailedLoginException {
    public UserNotFoundException(String login) {
        super("Invalid Login and/or Password", login);
    }
}