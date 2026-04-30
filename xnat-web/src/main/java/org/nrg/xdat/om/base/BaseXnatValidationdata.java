/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatValidationdata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatValidationdata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatValidationdata extends AutoXnatValidationdata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatValidationdata(ItemI item)
	{
		super(item);
	}

	public BaseXnatValidationdata(UserI user)
	{
		super(user);
	}

	public BaseXnatValidationdata()
	{}

	public BaseXnatValidationdata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
