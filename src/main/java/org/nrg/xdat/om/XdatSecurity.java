/*
 * org.nrg.xdat.om.XdatSecurity
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

import org.nrg.xdat.om.base.BaseXdatSecurity;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatSecurity extends BaseXdatSecurity {

	public XdatSecurity(ItemI item)
	{
		super(item);
	}

	public XdatSecurity(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatSecurity(UserI user)
	 **/
	public XdatSecurity()
	{}

	public XdatSecurity(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
