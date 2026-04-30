/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatSrscandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatSrscandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatSrscandata extends AutoXnatSrscandata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatSrscandata(ItemI item)
	{
		super(item);
	}

	public BaseXnatSrscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatSrscandata(UserI user)
	 **/
	public BaseXnatSrscandata()
	{}

	public BaseXnatSrscandata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
