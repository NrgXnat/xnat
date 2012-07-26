package org.nrg.xdat.services;

import java.util.List;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xdat.entities.XDATUserDetails;
import org.nrg.xdat.entities.XdatUserAuth;

public interface XdatUserAuthService extends BaseHibernateService<XdatUserAuth>{

	public static final String LOCALDB = "localdb";
	public static final String LDAP = "ldap";
	public static final String OPENID = "openid";
	public XdatUserAuth getUserByNameAndAuth(String user, String auth, String id);
	public XDATUserDetails getUserDetailsByNameAndAuth(String user, String auth);
	public XDATUserDetails getUserDetailsByNameAndAuth(String user, String auth, String id);
	public XDATUserDetails getUserDetailsByNameAndAuth(String user, String auth, String id, String email);
	public XDATUserDetails getUserDetailsByNameAndAuth(String user, String auth, String id, String email, String lastname, String firstname);
	public List<XdatUserAuth> getUsersByName(String user);
}
