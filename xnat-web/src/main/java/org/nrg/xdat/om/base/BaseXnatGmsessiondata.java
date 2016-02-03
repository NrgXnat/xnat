/*
 * org.nrg.xdat.om.base.BaseXnatGmsessiondata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatGmsessiondata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatGmsessiondata extends AutoXnatGmsessiondata {

	public BaseXnatGmsessiondata(ItemI item)
	{
		super(item);
	}

	public BaseXnatGmsessiondata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatGmsessiondata(UserI user)
	 **/
	public BaseXnatGmsessiondata()
	{}

	public BaseXnatGmsessiondata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
