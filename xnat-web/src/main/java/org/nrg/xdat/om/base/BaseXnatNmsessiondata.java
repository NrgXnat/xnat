/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatNmsessiondata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatNmsessiondata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatNmsessiondata extends AutoXnatNmsessiondata {

	public BaseXnatNmsessiondata(ItemI item)
	{
		super(item);
	}

	public BaseXnatNmsessiondata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatNmsessiondata(UserI user)
	 **/
	public BaseXnatNmsessiondata()
	{}

	public BaseXnatNmsessiondata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
