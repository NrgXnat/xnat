/*
 * core: org.nrg.xdat.security.user.XnatUserProvider
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2018, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.user;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.security.UserI;
import org.springframework.stereotype.Component;

import javax.inject.Provider;

/**
 * Defines the default user for XNAT services.
 */
@Component
@Getter
@Setter
@Accessors(prefix = "_")
@Slf4j
public class XnatUserProvider implements Provider<UserI> {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public XnatUserProvider(final SiteConfigPreferences preferences, final String userKey) {
        log.debug("Initializing user provider with preference key {}", userKey);
        _userKey = userKey;
        _login = preferences.getValue(userKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserI get() {
        if (_user == null) {
            updateUser(getLogin());
        }
        return getUser();
    }

    private void updateUser(final String login) {
        setLogin(login);
        try {
            setUser(Users.getUser(login));
        } catch (UserInitException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.UserServiceError, "User object for name " + login + " could not be initialized", e);
        } catch (UserNotFoundException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.UserNotFoundError, "User with name " + login + " could not be found.");
        }
    }

    private final String _userKey;

    private String _login;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private UserI _user;
}
