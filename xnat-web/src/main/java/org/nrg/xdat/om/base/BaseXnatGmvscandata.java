/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatGmvscandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatGmvscandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatGmvscandata extends AutoXnatGmvscandata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatGmvscandata(ItemI item)
	{
		super(item);
	}

	public BaseXnatGmvscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatGmvscandata(UserI user)
	 **/
	public BaseXnatGmvscandata()
	{}

	public BaseXnatGmvscandata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
