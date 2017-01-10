/*
 * web: org.nrg.xnat.utils.XnatUserProvider
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.utils;

import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.preferences.SiteConfigPreferences;
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
    public XnatUserProvider(final SiteConfigPreferences preferences, final String userKey) {
        _logger.debug("Initializing user provider with preference key {}", userKey);
        _preferences = preferences;
        _userKey = userKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserI get() {
        if (null == _user) {
            final String login = getLogin();
            try {
                _user = Users.getUser(login);
                _logger.debug("Retrieved user with login {}", login);
            } catch (UserInitException e) {
                throw new NrgServiceRuntimeException(NrgServiceError.UserServiceError, "User object for name " + login + " could not be initialized.");
            } catch (UserNotFoundException e) {
                throw new NrgServiceRuntimeException(NrgServiceError.UserNotFoundError, "User with name " + login + " could not be found.");
            }
        }
        return _user;
    }

    public String getUserKey() {
        return _userKey;
    }

    /**
     * Returns the configured login name for the default user. This can be used when only the username is required,
     * since this is a more lightweight operation.
     *
     * @return The configured user login name.
     */
    public String getLogin() {
        if (StringUtils.isBlank(_login)) {
            _login = _preferences.getValue(_userKey);
        }
        return _login;
    }

    public void setLogin(final String login) {
        _login = login;
    }

    public void clearUserObject(){
        _user = null;
    }

    private final Logger _logger = LoggerFactory.getLogger(XnatUserProvider.class);

    private final SiteConfigPreferences _preferences;
    private final String                _userKey;

    private String _login;
    private UserI _user = null;
}
