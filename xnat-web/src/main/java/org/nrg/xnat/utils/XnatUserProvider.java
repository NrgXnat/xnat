/*
 * web: org.nrg.xnat.utils.XnatUserProvider
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.utils;

import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Provider;

/**
 * Defines the default user for XNAT services.
 */
@Component
public class XnatUserProvider implements Provider<UserI> {
    public XnatUserProvider(final String login) {
        _login = login;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserI get() {
        if (null == user) {
            try {
                user = Users.getUser(_login);
            } catch (UserInitException e) {
                throw new NrgServiceRuntimeException(NrgServiceError.UserServiceError, "User object for name " + _login + " could not be initialized.");
            } catch (UserNotFoundException e) {
                throw new NrgServiceRuntimeException(NrgServiceError.UserNotFoundError, "User with name " + _login + " could not be found.");
            }
        }
        return user;
    }

    /**
     * Returns the configured login name for the default user. This can be used when only the username is required,
     * since this is a more lightweight operation.
     *
     * @return The configured user login name.
     */
    public String getLogin() {
        return _login;
    }

    private final Logger _logger = LoggerFactory.getLogger(XnatUserProvider.class);
    private final String _login;
    private UserI user = null;
}
