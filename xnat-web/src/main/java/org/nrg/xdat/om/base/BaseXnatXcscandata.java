/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatXcscandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatXcscandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatXcscandata extends AutoXnatXcscandata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatXcscandata(ItemI item)
	{
		super(item);
	}

	public BaseXnatXcscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatXcscandata(UserI user)
	 **/
	public BaseXnatXcscandata()
	{}

	public BaseXnatXcscandata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
