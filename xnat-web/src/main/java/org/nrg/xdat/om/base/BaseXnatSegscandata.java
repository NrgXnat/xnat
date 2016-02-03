/*
 * org.nrg.xdat.om.base.BaseXnatSegscandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatSegscandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatSegscandata extends AutoXnatSegscandata {

	public BaseXnatSegscandata(ItemI item)
	{
		super(item);
	}

	public BaseXnatSegscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatSegscandata(UserI user)
	 **/
	public BaseXnatSegscandata()
	{}

	public BaseXnatSegscandata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
