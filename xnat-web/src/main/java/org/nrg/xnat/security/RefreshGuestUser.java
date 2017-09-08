/*
 * web: org.nrg.xnat.security.DisableInactiveUsers
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.utils.XnatUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;

public class RefreshGuestUser implements Runnable {

    public RefreshGuestUser() {
    }

    /**
     * Refreshes guest user object so information about what permissions it has are up to date.
     */
    @Override
    public void run() {
        try {
            Users.getGuest(true);
        } catch (final Exception e) {
            logger.error("An error occurred trying to refresh guest user.", e);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(RefreshGuestUser.class);

}

