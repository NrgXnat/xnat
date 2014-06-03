/*
 * org.nrg.xdat.om.base.BaseXdatUserLogin
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

import org.nrg.xdat.om.base.auto.AutoXdatUserLogin;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatUserLogin extends AutoXdatUserLogin {

	public BaseXdatUserLogin(ItemI item)
	{
		super(item);
	}

	public BaseXdatUserLogin(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatUserLogin(UserI user)
	 **/
	public BaseXdatUserLogin()
	{}

	public BaseXdatUserLogin(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
