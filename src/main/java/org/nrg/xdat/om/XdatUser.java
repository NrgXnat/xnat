/*
 * org.nrg.xdat.om.XdatUser
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

import org.nrg.xdat.om.base.BaseXdatUser;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatUser extends BaseXdatUser {

	public XdatUser(ItemI item)
	{
		super(item);
	}

	public XdatUser(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatUser(UserI user)
	 **/
	public XdatUser()
	{}

	public XdatUser(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
