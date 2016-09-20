/*
 * core: org.nrg.xdat.om.base.BaseXdatElementSecurity
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om.base;
import java.util.Hashtable;

import org.nrg.xdat.om.base.auto.AutoXdatElementSecurity;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatElementSecurity extends AutoXdatElementSecurity {

	public BaseXdatElementSecurity(ItemI item)
	{
		super(item);
	}

	public BaseXdatElementSecurity(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatElementSecurity(UserI user)
	 **/
	public BaseXdatElementSecurity()
	{}

	public BaseXdatElementSecurity(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
