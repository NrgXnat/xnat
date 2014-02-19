/*
 * org.nrg.xdat.om.base.BaseXdatStoredSearchAllowedUser
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.om.base;
import java.util.Hashtable;

import org.nrg.xdat.om.base.auto.AutoXdatStoredSearchAllowedUser;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatStoredSearchAllowedUser extends AutoXdatStoredSearchAllowedUser {

	public BaseXdatStoredSearchAllowedUser(ItemI item)
	{
		super(item);
	}

	public BaseXdatStoredSearchAllowedUser(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatStoredSearchAllowedUser(UserI user)
	 **/
	public BaseXdatStoredSearchAllowedUser()
	{}

	public BaseXdatStoredSearchAllowedUser(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
