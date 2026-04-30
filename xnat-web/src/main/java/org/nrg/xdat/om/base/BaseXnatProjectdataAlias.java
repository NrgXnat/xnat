/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatProjectdataAlias
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatProjectdataAlias;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatProjectdataAlias extends AutoXnatProjectdataAlias {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatProjectdataAlias(ItemI item)
	{
		super(item);
	}

	public BaseXnatProjectdataAlias(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatProjectdataAlias(UserI user)
	 **/
	public BaseXnatProjectdataAlias()
	{}

	public BaseXnatProjectdataAlias(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
