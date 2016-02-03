/*
 * org.nrg.xdat.om.base.BaseXnatXa3dscandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatXa3dscandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatXa3dscandata extends AutoXnatXa3dscandata {

	public BaseXnatXa3dscandata(ItemI item)
	{
		super(item);
	}

	public BaseXnatXa3dscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatXa3dscandata(UserI user)
	 **/
	public BaseXnatXa3dscandata()
	{}

	public BaseXnatXa3dscandata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
