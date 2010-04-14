// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Mon Feb 26 14:11:51 CST 2007
 *
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
