/*
 * org.nrg.xdat.om.base.BaseXnatCrscandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatCrscandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatCrscandata extends AutoXnatCrscandata {

	public BaseXnatCrscandata(ItemI item)
	{
		super(item);
	}

	public BaseXnatCrscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatCrscandata(UserI user)
	 **/
	public BaseXnatCrscandata()
	{}

	public BaseXnatCrscandata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
