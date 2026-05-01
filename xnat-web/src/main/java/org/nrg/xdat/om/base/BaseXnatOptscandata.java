/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatOptscandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatOptscandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatOptscandata extends AutoXnatOptscandata {

	public BaseXnatOptscandata(ItemI item)
	{
		super(item);
	}

	public BaseXnatOptscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatOptscandata(UserI user)
	 **/
	public BaseXnatOptscandata()
	{}

	public BaseXnatOptscandata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
