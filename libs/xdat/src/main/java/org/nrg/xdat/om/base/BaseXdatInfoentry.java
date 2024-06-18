/*
 * core: org.nrg.xdat.om.base.BaseXdatInfoentry
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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
