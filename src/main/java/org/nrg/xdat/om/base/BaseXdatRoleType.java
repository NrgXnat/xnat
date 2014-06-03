/*
 * org.nrg.xdat.om.base.BaseXdatRoleType
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

import org.nrg.xdat.om.base.auto.AutoXdatRoleType;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatRoleType extends AutoXdatRoleType {

	public BaseXdatRoleType(ItemI item)
	{
		super(item);
	}

	public BaseXdatRoleType(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatRoleType(UserI user)
	 **/
	public BaseXdatRoleType()
	{}

	public BaseXdatRoleType(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
