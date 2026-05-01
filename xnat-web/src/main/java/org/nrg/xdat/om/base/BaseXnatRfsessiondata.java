/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatRfsessiondata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatRfsessiondata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatRfsessiondata extends AutoXnatRfsessiondata {

	public BaseXnatRfsessiondata(ItemI item)
	{
		super(item);
	}

	public BaseXnatRfsessiondata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatRfsessiondata(UserI user)
	 **/
	public BaseXnatRfsessiondata()
	{}

	public BaseXnatRfsessiondata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
