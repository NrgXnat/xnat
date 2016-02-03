/*
 * org.nrg.xdat.om.base.BaseXnatEsvsessiondata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatEsvsessiondata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatEsvsessiondata extends AutoXnatEsvsessiondata {

	public BaseXnatEsvsessiondata(ItemI item)
	{
		super(item);
	}

	public BaseXnatEsvsessiondata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatEsvsessiondata(UserI user)
	 **/
	public BaseXnatEsvsessiondata()
	{}

	public BaseXnatEsvsessiondata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
