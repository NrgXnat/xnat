/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseValAdditionalval
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoValAdditionalval;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseValAdditionalval extends AutoValAdditionalval {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseValAdditionalval(ItemI item)
	{
		super(item);
	}

	public BaseValAdditionalval(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseValAdditionalval(UserI user)
	 **/
	public BaseValAdditionalval()
	{}

	public BaseValAdditionalval(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
