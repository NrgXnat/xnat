/*
 * org.nrg.xdat.om.XdatRoleType
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

import org.nrg.xdat.om.base.BaseXdatRoleType;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatRoleType extends BaseXdatRoleType {

	public XdatRoleType(ItemI item)
	{
		super(item);
	}

	public XdatRoleType(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatRoleType(UserI user)
	 **/
	public XdatRoleType()
	{}

	public XdatRoleType(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
