/*
 * core: org.nrg.xdat.om.base.BaseXdatNewsentry
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om.base;
import java.util.Hashtable;

import org.nrg.xdat.om.base.auto.AutoXdatNewsentry;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatNewsentry extends AutoXdatNewsentry {

	public BaseXdatNewsentry(ItemI item)
	{
		super(item);
	}

	public BaseXdatNewsentry(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatNewsentry(UserI user)
	 **/
	public BaseXdatNewsentry()
	{}

	public BaseXdatNewsentry(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
