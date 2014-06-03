/*
 * org.nrg.xdat.om.base.BaseXdatSecurity
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

import org.nrg.xdat.om.base.auto.AutoXdatSecurity;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatSecurity extends AutoXdatSecurity {

	public BaseXdatSecurity(ItemI item)
	{
		super(item);
	}

	public BaseXdatSecurity(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatSecurity(UserI user)
	 **/
	public BaseXdatSecurity()
	{}

	public BaseXdatSecurity(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
