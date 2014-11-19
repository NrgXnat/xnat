/*
 * org.nrg.xdat.om.XdatInfoentry
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

import org.nrg.xdat.om.base.BaseXdatInfoentry;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatInfoentry extends BaseXdatInfoentry {

	public XdatInfoentry(ItemI item)
	{
		super(item);
	}

	public XdatInfoentry(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatInfoentry(UserI user)
	 **/
	public XdatInfoentry()
	{}

	public XdatInfoentry(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
