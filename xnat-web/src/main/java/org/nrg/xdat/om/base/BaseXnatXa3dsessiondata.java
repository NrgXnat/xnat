/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatXa3dsessiondata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatXa3dsessiondata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatXa3dsessiondata extends AutoXnatXa3dsessiondata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatXa3dsessiondata(ItemI item)
	{
		super(item);
	}

	public BaseXnatXa3dsessiondata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatXa3dsessiondata(UserI user)
	 **/
	public BaseXnatXa3dsessiondata()
	{}

	public BaseXnatXa3dsessiondata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
