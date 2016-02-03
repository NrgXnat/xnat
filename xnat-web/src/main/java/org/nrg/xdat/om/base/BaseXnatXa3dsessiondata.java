/*
 * org.nrg.xdat.om.base.BaseXnatXa3dsessiondata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatXa3dsessiondata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatXa3dsessiondata extends AutoXnatXa3dsessiondata {

	public BaseXnatXa3dsessiondata(ItemI item)
	{
		super(item);
	}

	public BaseXnatXa3dsessiondata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatXa3dsessiondata(UserI user)
	 **/
	public BaseXnatXa3dsessiondata()
	{}

	public BaseXnatXa3dsessiondata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
