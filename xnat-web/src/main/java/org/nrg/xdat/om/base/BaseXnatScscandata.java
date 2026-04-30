/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatScscandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatScscandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatScscandata extends AutoXnatScscandata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatScscandata(ItemI item)
	{
		super(item);
	}

	public BaseXnatScscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatScscandata(UserI user)
	 **/
	public BaseXnatScscandata()
	{}

	public BaseXnatScscandata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
