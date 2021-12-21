/*
 * web: org.nrg.xapi.model.users.UserFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.model.users;

import org.nrg.xdat.entities.UserChangeRequest;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.UserChangeRequestService;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Handles all data operations with {@link User the user data model}.
 */
@Service
public class UserFactory {
    @Autowired
    public UserFactory(final UserChangeRequestService userChangeRequestService) {
        _userChangeRequestService = userChangeRequestService;
    }

    public User getUser() {
        return new User();
    }

    public User getUser(final String username) throws UserNotFoundException {
        return getUser(username, false);
    }

    public User getUser(final String username, boolean includeSensitiveFields) throws UserNotFoundException {
        final XdatUser user = XDATUser.getXdatUsersByLogin(username, null, false);
        if (user == null) {
            throw new UserNotFoundException(username);
        }
        return getUser((UserI) user, includeSensitiveFields);
    }

    public User getUser(final UserI xdatUser) {
        return getUser(xdatUser, false);
    }

    public User getUser(final UserI xdatUser, boolean includeSensitiveFields) {
        final User user = new User();
        populateUser(user, xdatUser, includeSensitiveFields);
        return user;
    }
    
    private void populateUser(final User user, final UserI xdatUser, boolean includeSensitiveFields) {
        if (includeSensitiveFields) {
            user.setId(xdatUser.getID());
            user.setPassword(xdatUser.getPassword());
            user.setSalt(xdatUser.getSalt());
            user.setLastModified(xdatUser.getLastModified());
            user.setAuthorization(xdatUser.getAuthorization());
            user.setEnabled(xdatUser.isEnabled());
            user.setVerified(xdatUser.isVerified());
        }
        UserChangeRequest ucr = _userChangeRequestService.findChangeRequestForUserAndField(xdatUser.getUsername(), "email");
        if (ucr != null) {
            user.setPendingEmail(ucr.getNewValue());
        }
        user.setUsername(xdatUser.getUsername());
        user.setFirstName(xdatUser.getFirstname());
        user.setLastName(xdatUser.getLastname());
        user.setEmail(xdatUser.getEmail());
        user.setSecured(true);
    }

    private final UserChangeRequestService _userChangeRequestService;
}
