/*
 * org.nrg.xdat.om.base.BaseXnatXascandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatXascandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatXascandata extends AutoXnatXascandata {

	public BaseXnatXascandata(ItemI item)
	{
		super(item);
	}

	public BaseXnatXascandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatXascandata(UserI user)
	 **/
	public BaseXnatXascandata()
	{}

	public BaseXnatXascandata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
