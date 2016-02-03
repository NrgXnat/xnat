/*
 * org.nrg.xdat.om.base.BaseXnatComputationdata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatComputationdata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatComputationdata extends AutoXnatComputationdata {

	public BaseXnatComputationdata(ItemI item)
	{
		super(item);
	}

	public BaseXnatComputationdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatComputationdata(UserI user)
	 **/
	public BaseXnatComputationdata()
	{}

	public BaseXnatComputationdata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
