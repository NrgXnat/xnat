/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatAsideeffectspittsburghdata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatAsideeffectspittsburghdata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatAsideeffectspittsburghdata extends AutoXnatAsideeffectspittsburghdata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatAsideeffectspittsburghdata(ItemI item)
	{
		super(item);
	}

	public BaseXnatAsideeffectspittsburghdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatAsideeffectspittsburghdata(UserI user)
	 **/
	public BaseXnatAsideeffectspittsburghdata()
	{}

	public BaseXnatAsideeffectspittsburghdata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}

