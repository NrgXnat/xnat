/*
 * org.nrg.xdat.om.XdatStoredSearchAllowedUser
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.om;
import java.util.Hashtable;

import org.nrg.xdat.om.base.BaseXdatStoredSearchAllowedUser;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatStoredSearchAllowedUser extends BaseXdatStoredSearchAllowedUser {

	public XdatStoredSearchAllowedUser(ItemI item)
	{
		super(item);
	}

	public XdatStoredSearchAllowedUser(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatStoredSearchAllowedUser(UserI user)
	 **/
	public XdatStoredSearchAllowedUser()
	{}

	public XdatStoredSearchAllowedUser(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
