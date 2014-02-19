/*
 * org.nrg.xdat.om.base.BaseXdatInfoentry
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.om.base;
import java.util.Hashtable;

import org.nrg.xdat.om.base.auto.AutoXdatInfoentry;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatInfoentry extends AutoXdatInfoentry {

	public BaseXdatInfoentry(ItemI item)
	{
		super(item);
	}

	public BaseXdatInfoentry(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatInfoentry(UserI user)
	 **/
	public BaseXdatInfoentry()
	{}

	public BaseXdatInfoentry(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
