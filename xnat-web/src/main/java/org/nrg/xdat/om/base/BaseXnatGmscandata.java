/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatGmscandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatGmscandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatGmscandata extends AutoXnatGmscandata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatGmscandata(ItemI item)
	{
		super(item);
	}

	public BaseXnatGmscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatGmscandata(UserI user)
	 **/
	public BaseXnatGmscandata()
	{}

	public BaseXnatGmscandata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
