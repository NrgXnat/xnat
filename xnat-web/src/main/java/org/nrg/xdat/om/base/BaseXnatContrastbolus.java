/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatContrastbolus
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatContrastbolus;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatContrastbolus extends AutoXnatContrastbolus {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatContrastbolus(ItemI item)
	{
		super(item);
	}

	public BaseXnatContrastbolus(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatContrastbolus(UserI user)
	 **/
	public BaseXnatContrastbolus()
	{}

	public BaseXnatContrastbolus(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
