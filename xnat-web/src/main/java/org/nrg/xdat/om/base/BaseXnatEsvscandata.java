/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatEsvscandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatEsvscandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatEsvscandata extends AutoXnatEsvscandata {

	public BaseXnatEsvscandata(ItemI item)
	{
		super(item);
	}

	public BaseXnatEsvscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatEsvscandata(UserI user)
	 **/
	public BaseXnatEsvscandata()
	{}

	public BaseXnatEsvscandata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
