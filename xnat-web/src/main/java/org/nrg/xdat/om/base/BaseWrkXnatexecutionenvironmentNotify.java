/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseWrkXnatexecutionenvironmentNotify
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoWrkXnatexecutionenvironmentNotify;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseWrkXnatexecutionenvironmentNotify extends AutoWrkXnatexecutionenvironmentNotify {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseWrkXnatexecutionenvironmentNotify(ItemI item)
	{
		super(item);
	}

	public BaseWrkXnatexecutionenvironmentNotify(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseWrkXnatexecutionenvironmentNotify(UserI user)
	 **/
	public BaseWrkXnatexecutionenvironmentNotify()
	{}

	public BaseWrkXnatexecutionenvironmentNotify(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
