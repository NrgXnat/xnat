/*
 * core: org.nrg.xdat.om.base.BaseXdatElementAccess
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om.base;
import java.util.Hashtable;

import org.nrg.xdat.om.base.auto.AutoXdatElementAccess;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatElementAccess extends AutoXdatElementAccess {

	public BaseXdatElementAccess(ItemI item)
	{
		super(item);
	}

	public BaseXdatElementAccess(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatElementAccess(UserI user)
	 **/
	public BaseXdatElementAccess()
	{}

	public BaseXdatElementAccess(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
