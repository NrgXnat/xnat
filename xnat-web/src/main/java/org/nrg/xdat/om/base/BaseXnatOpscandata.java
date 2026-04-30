/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatOpscandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatOpscandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatOpscandata extends AutoXnatOpscandata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatOpscandata(ItemI item)
	{
		super(item);
	}

	public BaseXnatOpscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatOpscandata(UserI user)
	 **/
	public BaseXnatOpscandata()
	{}

	public BaseXnatOpscandata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
