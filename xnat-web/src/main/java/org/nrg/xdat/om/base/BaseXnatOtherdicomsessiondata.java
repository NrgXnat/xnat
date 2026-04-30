/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatOtherdicomsessiondata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatOtherdicomsessiondata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatOtherdicomsessiondata extends AutoXnatOtherdicomsessiondata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatOtherdicomsessiondata(ItemI item)
	{
		super(item);
	}

	public BaseXnatOtherdicomsessiondata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatOtherdicomsessiondata(UserI user)
	 **/
	public BaseXnatOtherdicomsessiondata()
	{}

	public BaseXnatOtherdicomsessiondata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
