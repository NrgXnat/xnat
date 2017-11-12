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

    XdatUserAuth getUserByNameAndAuth(final String user, final String auth, final String id);

    XdatUserAuth getUserByXdatUsernameAndAuth(final String user, final String auth, final String id);

    UserI getUserDetailsByNameAndAuth(final String user, final String auth);

    UserI getUserDetailsByNameAndAuth(final String user, final String auth, final String id);

    UserI getUserDetailsByNameAndAuth(final String user, final String auth, final String id, final String email);

    UserI getUserDetailsByNameAndAuth(final String user, final String auth, final String id, final String email, final String lastname, final String firstname);

    UserI getUserDetailsByUsernameAndMostRecentSuccessfulLogin(final String username);

    List<XdatUserAuth> getUsersByName(final String user);

    List<XdatUserAuth> getUsersByXdatUsername(final String xdatUser);
}
