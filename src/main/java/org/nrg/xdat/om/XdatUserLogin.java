/*
 * org.nrg.xdat.om.XdatUserLogin
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

import org.nrg.xdat.om.base.BaseXdatUserLogin;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatUserLogin extends BaseXdatUserLogin {

	public XdatUserLogin(ItemI item)
	{
		super(item);
	}

	public XdatUserLogin(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatUserLogin(UserI user)
	 **/
	public XdatUserLogin()
	{}

	public XdatUserLogin(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
