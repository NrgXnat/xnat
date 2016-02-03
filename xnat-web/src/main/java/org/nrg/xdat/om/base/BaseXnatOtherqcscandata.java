/*
 * org.nrg.xdat.om.base.BaseXnatOtherqcscandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatOtherqcscandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatOtherqcscandata extends AutoXnatOtherqcscandata {

	public BaseXnatOtherqcscandata(ItemI item)
	{
		super(item);
	}

	public BaseXnatOtherqcscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatOtherqcscandata(UserI user)
	 **/
	public BaseXnatOtherqcscandata()
	{}

	public BaseXnatOtherqcscandata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
