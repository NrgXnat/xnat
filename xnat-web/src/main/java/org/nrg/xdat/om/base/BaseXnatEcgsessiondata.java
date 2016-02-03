/*
 * org.nrg.xdat.om.base.BaseXnatEcgsessiondata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatEcgsessiondata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatEcgsessiondata extends AutoXnatEcgsessiondata {

	public BaseXnatEcgsessiondata(ItemI item)
	{
		super(item);
	}

	public BaseXnatEcgsessiondata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatEcgsessiondata(UserI user)
	 **/
	public BaseXnatEcgsessiondata()
	{}

	public BaseXnatEcgsessiondata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
