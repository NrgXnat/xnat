/*
 * org.nrg.xdat.services.XdatUserAuthService
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */
package org.nrg.xdat.services;

import java.util.List;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xdat.entities.UserAuthI;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xft.security.UserI;

public interface XdatUserAuthService extends BaseHibernateService<XdatUserAuth>{

	public static final String LOCALDB = "localdb";
	public static final String LDAP = "ldap";
	public static final String OPENID = "openid";
	public static final String TOKEN = "token";
	public XdatUserAuth getUserByNameAndAuth(String user, String auth, String id);
    public XdatUserAuth getUserByXdatUsernameAndAuth(String user, String auth, String id);
    public UserI getUserDetailsByNameAndAuth(String user, String auth);
	public UserI getUserDetailsByNameAndAuth(String user, String auth, String id);
	public UserI getUserDetailsByNameAndAuth(String user, String auth, String id, String email);
	public UserI getUserDetailsByNameAndAuth(String user, String auth, String id, String email, String lastname, String firstname);
	public UserI getUserDetailsByUsernameAndMostRecentSuccessfulLogin(String username);
	public List<XdatUserAuth> getUsersByName(String user);
	public List<XdatUserAuth> getUsersByXdatUsername(String xdatUser);
}
