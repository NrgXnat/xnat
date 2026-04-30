/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatXcsessiondata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatXcsessiondata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatXcsessiondata extends AutoXnatXcsessiondata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatXcsessiondata(ItemI item)
	{
		super(item);
	}

	public BaseXnatXcsessiondata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatXcsessiondata(UserI user)
	 **/
	public BaseXnatXcsessiondata()
	{}

	public BaseXnatXcsessiondata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
