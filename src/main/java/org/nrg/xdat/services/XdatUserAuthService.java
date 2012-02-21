package org.nrg.xdat.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xdat.entities.XDATUserDetails;
import org.nrg.xdat.entities.XdatUserAuth;

public interface XdatUserAuthService extends BaseHibernateService<XdatUserAuth>{

	public XdatUserAuth getUserByNameAndAuth(String user, String auth);
	public XDATUserDetails getUserDetailsByNameAndAuth(String user, String auth);
	public XDATUserDetails getUserDetailsByNameAndAuth(String user, String auth, String email);
}
