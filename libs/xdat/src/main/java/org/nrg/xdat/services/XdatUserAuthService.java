/*
 * core: org.nrg.xdat.services.XdatUserAuthService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xft.security.UserI;

import java.util.List;

public interface XdatUserAuthService extends BaseHibernateService<XdatUserAuth> {
    String LOCALDB = "localdb";
    String LDAP    = "ldap";
    String OPENID  = "openid";
    String TOKEN   = "token";

    boolean hasUserByNameAndAuth(final String user, final String auth);

    boolean hasUserByNameAndAuth(final String user, final String auth, final String id);

    String getXdatUsernameByAuthNameAndProvider(final String user, final String auth, final String id);

    XdatUserAuth getUserByNameAndAuth(final String user, final String auth, final String id);

    XdatUserAuth getUserByXdatUsernameAndAuth(final String user, final String auth, final String id);

    UserI getUserDetailsByNameAndAuth(final String user, final String auth);

    UserI getUserDetailsByNameAndAuth(final String user, final String auth, final String id);

    UserI getUserDetailsByNameAndAuth(final String user, final String auth, final String id, final String email);

    UserI getUserDetailsByNameAndAuth(final String user, final String auth, final String id, final String email, final String lastname, final String firstname);

    UserI getUserDetailsByUsernameAndMostRecentSuccessfulLogin(final String username);

    List<XdatUserAuth> getUsersByName(final String user);

    List<XdatUserAuth> getUsersByXdatUsername(final String xdatUser);

    /**
     * Increments the indicated account's failed login count. If the user's number of failed logins exceeds the maximum allowed,
     * this method sets the lockout time and returns true. Otherwise, this returns false. Note that the user parameter should be
     * the username for the indicated provider and method.
     *
     * @param user The user auth record for which the failed login count should be incremented.
     *
     * @return Returns true if the user has exceeded the maximum allowable failures, false otherwise.
     */
    boolean addFailedLoginAttempt(final XdatUserAuth user);

    /**
     * Increments the indicated account's failed login count. If the user's number of failed logins exceeds the maximum allowed,
     * this method sets the lockout time and returns true. Otherwise, this returns false. Note that the user parameter should be
     * the username for the indicated provider and method.
     *
     * @param user       The user for whom the failed login count should be incremented.
     * @param provider   The provider implementation.
     * @param providerId The definition for the provider implementation.
     *
     * @return Returns true if the user has exceeded the maximum allowable failures, false otherwise.
     */
    boolean addFailedLoginAttempt(final String user, final String provider, final String providerId);

    /**
     * Resets ths user's failed login count. If the lockout time is set, that is cleared as well.
     *
     * @param user The user auth record for which the failed login count and lockout time should be cleared.
     */
    void resetFailedLogins(final XdatUserAuth user);

    /**
     * Resets ths user's failed login count. If the lockout time is set, that is cleared as well.
     *
     * @param user       The user for whom the failed login count should be incremented.
     * @param provider   The provider implementation.
     * @param providerId The definition for the provider implementation.
     */
    void resetFailedLogins(final String user, final String provider, final String providerId);
}
