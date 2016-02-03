/*
 * org.nrg.xdat.om.base.BaseXnatAupdrs3data
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatAupdrs3data;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatAupdrs3data extends AutoXnatAupdrs3data {

	public BaseXnatAupdrs3data(ItemI item)
	{
		super(item);
	}

	public BaseXnatAupdrs3data(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatAupdrs3data(UserI user)
	 **/
	public BaseXnatAupdrs3data()
	{}

	public BaseXnatAupdrs3data(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}

