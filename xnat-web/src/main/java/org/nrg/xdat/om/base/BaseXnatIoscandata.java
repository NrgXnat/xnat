/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatIoscandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatIoscandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatIoscandata extends AutoXnatIoscandata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatIoscandata(ItemI item)
	{
		super(item);
	}

	public BaseXnatIoscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatIoscandata(UserI user)
	 **/
	public BaseXnatIoscandata()
	{}

	public BaseXnatIoscandata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
