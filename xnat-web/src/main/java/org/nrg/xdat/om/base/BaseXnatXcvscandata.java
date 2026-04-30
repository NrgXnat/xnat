/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatXcvscandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatXcvscandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatXcvscandata extends AutoXnatXcvscandata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatXcvscandata(ItemI item)
	{
		super(item);
	}

	public BaseXnatXcvscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatXcvscandata(UserI user)
	 **/
	public BaseXnatXcvscandata()
	{}

	public BaseXnatXcvscandata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
