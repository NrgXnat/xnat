/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatXascandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatXascandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatXascandata extends AutoXnatXascandata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatXascandata(ItemI item)
	{
		super(item);
	}

	public BaseXnatXascandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatXascandata(UserI user)
	 **/
	public BaseXnatXascandata()
	{}

	public BaseXnatXascandata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
