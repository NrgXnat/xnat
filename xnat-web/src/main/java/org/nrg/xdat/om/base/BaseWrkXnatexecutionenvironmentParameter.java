/*
 * org.nrg.xdat.om.base.BaseWrkXnatexecutionenvironmentParameter
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoWrkXnatexecutionenvironmentParameter;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseWrkXnatexecutionenvironmentParameter extends AutoWrkXnatexecutionenvironmentParameter {

	public BaseWrkXnatexecutionenvironmentParameter(ItemI item)
	{
		super(item);
	}

	public BaseWrkXnatexecutionenvironmentParameter(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseWrkXnatexecutionenvironmentParameter(UserI user)
	 **/
	public BaseWrkXnatexecutionenvironmentParameter()
	{}

	public BaseWrkXnatexecutionenvironmentParameter(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
