/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatSmsessiondata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatSmsessiondata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatSmsessiondata extends AutoXnatSmsessiondata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatSmsessiondata(ItemI item)
	{
		super(item);
	}

	public BaseXnatSmsessiondata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatSmsessiondata(UserI user)
	 **/
	public BaseXnatSmsessiondata()
	{}

	public BaseXnatSmsessiondata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
