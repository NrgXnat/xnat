/*
 * core: org.nrg.xdat.om.base.BaseXdatUserLogin
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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
