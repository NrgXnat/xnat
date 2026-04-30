/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatOtherdicomscandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatOtherdicomscandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatOtherdicomscandata extends AutoXnatOtherdicomscandata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatOtherdicomscandata(ItemI item)
	{
		super(item);
	}

	public BaseXnatOtherdicomscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatOtherdicomscandata(UserI user)
	 **/
	public BaseXnatOtherdicomscandata()
	{}

	public BaseXnatOtherdicomscandata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
