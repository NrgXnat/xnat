/*
 * org.nrg.xdat.om.base.BaseXnatMgsessiondata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatMgsessiondata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatMgsessiondata extends AutoXnatMgsessiondata {

	public BaseXnatMgsessiondata(ItemI item)
	{
		super(item);
	}

	public BaseXnatMgsessiondata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatMgsessiondata(UserI user)
	 **/
	public BaseXnatMgsessiondata()
	{}

	public BaseXnatMgsessiondata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
