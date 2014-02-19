/*
 * org.nrg.xdat.om.XdatElementSecurity
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.om;
import java.util.Hashtable;

import org.nrg.xdat.om.base.BaseXdatElementSecurity;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatElementSecurity extends BaseXdatElementSecurity {

	public XdatElementSecurity(ItemI item)
	{
		super(item);
	}

	public XdatElementSecurity(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatElementSecurity(UserI user)
	 **/
	public XdatElementSecurity()
	{}

	public XdatElementSecurity(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
