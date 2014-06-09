package org.nrg.xdat.security.user.exceptions;

public class UserNotFoundException extends FailedLoginException {
    public UserNotFoundException(String login) {
        super("Invalid Login and/or Password", login);
    }
    public UserNotFoundException(Integer id) {
        super("Invalid User id", id.toString());
    }
}