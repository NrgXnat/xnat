/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatEsscandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatEsscandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatEsscandata extends AutoXnatEsscandata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatEsscandata(ItemI item)
	{
		super(item);
	}

	public BaseXnatEsscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatEsscandata(UserI user)
	 **/
	public BaseXnatEsscandata()
	{}

	public BaseXnatEsscandata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
