/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatEegscandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatEegscandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatEegscandata extends AutoXnatEegscandata {

	public BaseXnatEegscandata(ItemI item)
	{
		super(item);
	}

	public BaseXnatEegscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatEcgscandata(UserI user)
	 **/
	public BaseXnatEegscandata()
	{}

	public BaseXnatEegscandata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
