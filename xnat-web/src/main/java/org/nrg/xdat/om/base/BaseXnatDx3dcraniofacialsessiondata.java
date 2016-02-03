/*
 * org.nrg.xdat.om.base.BaseXnatDx3dcraniofacialsessiondata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatDx3dcraniofacialsessiondata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatDx3dcraniofacialsessiondata extends AutoXnatDx3dcraniofacialsessiondata {

	public BaseXnatDx3dcraniofacialsessiondata(ItemI item)
	{
		super(item);
	}

	public BaseXnatDx3dcraniofacialsessiondata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatDx3dcraniofacialsessiondata(UserI user)
	 **/
	public BaseXnatDx3dcraniofacialsessiondata()
	{}

	public BaseXnatDx3dcraniofacialsessiondata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
