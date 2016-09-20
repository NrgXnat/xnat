/*
 * core: org.nrg.xdat.om.base.BaseXdatUser
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om.base;
import java.util.Hashtable;

import org.nrg.xdat.om.base.auto.AutoXdatUser;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatUser extends AutoXdatUser {

	public BaseXdatUser(ItemI item)
	{
		super(item);
	}

	public BaseXdatUser(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatUser(UserI user)
	 **/
	public BaseXdatUser()
	{}

	public BaseXdatUser(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
