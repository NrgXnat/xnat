/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatCrsessiondata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatCrsessiondata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatCrsessiondata extends AutoXnatCrsessiondata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatCrsessiondata(ItemI item)
	{
		super(item);
	}

	public BaseXnatCrsessiondata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatCrsessiondata(UserI user)
	 **/
	public BaseXnatCrsessiondata()
	{}

	public BaseXnatCrsessiondata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
