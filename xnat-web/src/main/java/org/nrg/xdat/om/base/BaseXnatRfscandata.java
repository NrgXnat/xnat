/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatRfscandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatRfscandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatRfscandata extends AutoXnatRfscandata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatRfscandata(ItemI item)
	{
		super(item);
	}

	public BaseXnatRfscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatRfscandata(UserI user)
	 **/
	public BaseXnatRfscandata()
	{}

	public BaseXnatRfscandata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
