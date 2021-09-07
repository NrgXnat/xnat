/*
 * web: org.nrg.xapi.model.users.UserFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.model.users;

import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.security.UserI;
import org.springframework.stereotype.Service;

/**
 * Handles all data operations with {@link User the user data model}.
 */
@Service
public class UserFactory {
    public User getUser() {
        return new User();
    }

    public User getUser(final String username) throws UserNotFoundException {
        final XdatUser user = XDATUser.getXdatUsersByLogin(username, null, false);
        if (user == null) {
            throw new UserNotFoundException(username);
        }
        return getUser((UserI) user);
    }

    public User getUser(final UserI xdatUser) {
        return populateUser(xdatUser);
    }

    private User populateUser(final UserI xdatUser) {
        final User user = new User();
        user.setId(xdatUser.getID());
        user.setUsername(xdatUser.getUsername());
        user.setFirstName(xdatUser.getFirstname());
        user.setLastName(xdatUser.getLastname());
        user.setEmail(xdatUser.getEmail());
        user.setPassword(xdatUser.getPassword());
        user.setSalt(xdatUser.getSalt());
        user.setLastModified(xdatUser.getLastModified());
        user.setAuthorization(xdatUser.getAuthorization());
        user.setEnabled(xdatUser.isEnabled());
        user.setVerified(xdatUser.isVerified());
        user.setSecured(true);
        return user;
    }
}
