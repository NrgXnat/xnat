/*
 * core: org.nrg.xdat.services.XdatUserAuthService
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.services;

import java.util.List;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xdat.entities.UserAuthI;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xft.security.UserI;

public interface XdatUserAuthService extends BaseHibernateService<XdatUserAuth>{
	String LOCALDB = "localdb";
	String LDAP = "ldap";
	String OPENID = "openid";
	String TOKEN = "token";

	XdatUserAuth getUserByNameAndAuth(String user, String auth, String id);
	XdatUserAuth getUserByXdatUsernameAndAuth(String user, String auth, String id);
    UserI getUserDetailsByNameAndAuth(String user, String auth);
	UserI getUserDetailsByNameAndAuth(String user, String auth, String id);
	UserI getUserDetailsByNameAndAuth(String user, String auth, String id, String email);
	UserI getUserDetailsByNameAndAuth(String user, String auth, String id, String email, String lastname, String firstname);
	UserI getUserDetailsByUsernameAndMostRecentSuccessfulLogin(String username);
	List<XdatUserAuth> getUsersByName(String user);
	List<XdatUserAuth> getUsersByXdatUsername(String xdatUser);
}
