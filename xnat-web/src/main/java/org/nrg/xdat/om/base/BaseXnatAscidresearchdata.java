/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatAscidresearchdata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatAscidresearchdata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatAscidresearchdata extends AutoXnatAscidresearchdata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatAscidresearchdata(ItemI item)
	{
		super(item);
	}

	public BaseXnatAscidresearchdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatAscidresearchdata(UserI user)
	 **/
	public BaseXnatAscidresearchdata()
	{}

	public BaseXnatAscidresearchdata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}

