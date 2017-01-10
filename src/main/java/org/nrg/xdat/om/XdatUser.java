/*
 * core: org.nrg.xdat.om.XdatUser
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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
