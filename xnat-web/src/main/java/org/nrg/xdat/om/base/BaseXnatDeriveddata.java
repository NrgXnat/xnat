/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatDeriveddata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatDeriveddata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatDeriveddata extends AutoXnatDeriveddata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatDeriveddata(ItemI item)
	{
		super(item);
	}

	public BaseXnatDeriveddata(UserI user)
	{
		super(user);
	}

	public BaseXnatDeriveddata()
	{}

	public BaseXnatDeriveddata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
