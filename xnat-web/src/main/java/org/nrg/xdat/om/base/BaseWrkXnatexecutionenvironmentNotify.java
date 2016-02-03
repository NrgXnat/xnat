/*
 * org.nrg.xdat.om.base.BaseWrkXnatexecutionenvironmentNotify
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoWrkXnatexecutionenvironmentNotify;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseWrkXnatexecutionenvironmentNotify extends AutoWrkXnatexecutionenvironmentNotify {

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
