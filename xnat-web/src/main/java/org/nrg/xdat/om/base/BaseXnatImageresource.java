/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatImageresource
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatImageresource;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatImageresource extends AutoXnatImageresource {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatImageresource(ItemI item)
	{
		super(item);
	}

	public BaseXnatImageresource(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatImageresource(UserI user)
	 **/
	public BaseXnatImageresource()
	{}

	public BaseXnatImageresource(Hashtable properties, UserI user)
	{
		super(properties,user);
	}


}
