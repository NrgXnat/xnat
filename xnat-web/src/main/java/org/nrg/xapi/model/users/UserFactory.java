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
    
    private void populateUser(final User user, final UserI xdatUser) {
        user.setId(xdatUser.getID());
        user.setUsername(xdatUser.getUsername());
        user.setFirstName(xdatUser.getFirstname());
        user.setLastName(xdatUser.getLastname());
        user.setEmail(xdatUser.getEmail());
        checkPendingEmail(user, xdatUser);
        user.setPassword(xdatUser.getPassword());
        user.setLastModified(xdatUser.getLastModified());
        user.setAuthorization(xdatUser.getAuthorization());
        user.setEnabled(xdatUser.isEnabled());
        user.setVerified(xdatUser.isVerified());
        user.setSecured(true);
    }

    private void checkPendingEmail(final User user, final UserI xdatUser) {
        UserChangeRequest ucr = _userChangeRequestService.findChangeRequestForUserAndField(xdatUser.getUsername(), "email");
        if (ucr != null) {
            user.setPendingEmail(ucr.getNewValue());
        }
    }

    private final UserChangeRequestService _userChangeRequestService;
}
