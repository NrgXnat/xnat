/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatUsscandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatUsscandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatUsscandata extends AutoXnatUsscandata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatUsscandata(ItemI item)
	{
		super(item);
	}

	public BaseXnatUsscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatUsscandata(UserI user)
	 **/
	public BaseXnatUsscandata()
	{}

	public BaseXnatUsscandata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
