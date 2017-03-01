/*
 * web: org.nrg.xapi.model.users.UserFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.model.users;

import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Handles all data operations with {@link User the user data model}.
 */
@Service
public class UserFactory {
    @Autowired
    public UserFactory(final XdatUserAuthService service) {
        _service = service;
    }

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
        final User user = new User();
        populateUser(user, xdatUser);
        return user;
    }
    
    private User populateUser(final User user, final UserI xdatUser) {
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

    private final XdatUserAuthService _service;
}
