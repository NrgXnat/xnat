/*
 * org.nrg.xnat.utils.XnatUserProvider
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.utils;

import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Provider;

@Component
public class XnatUserProvider implements Provider<UserI> {
    public XnatUserProvider(final String login) {
        _login = login;
    }

    /*
     * (non-Javadoc)
     * @see javax.inject.Provider#get()
     */
    public UserI get() {
        if (null == user) {
            try {
                user = Users.getUser(_login);
            } catch (Throwable t) {
                _logger.error("Unable to retrieve user " + _login, t);
                return null;
            }
        }
        return user;
    }

    private final Logger _logger = LoggerFactory.getLogger(XnatUserProvider.class);
    private final String _login;
    private UserI user = null;
}
