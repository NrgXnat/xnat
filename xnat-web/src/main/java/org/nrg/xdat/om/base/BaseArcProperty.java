/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseArcProperty
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoArcProperty;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseArcProperty extends AutoArcProperty {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseArcProperty(ItemI item)
	{
		super(item);
	}

	public BaseArcProperty(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseArcProperty(UserI user)
	 **/
	public BaseArcProperty()
	{}

	public BaseArcProperty(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
