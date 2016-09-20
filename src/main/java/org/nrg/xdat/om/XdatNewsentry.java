/*
 * core: org.nrg.xdat.om.XdatNewsentry
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om;
import java.util.Hashtable;

import org.nrg.xdat.om.base.BaseXdatNewsentry;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatNewsentry extends BaseXdatNewsentry {

	public XdatNewsentry(ItemI item)
	{
		super(item);
	}

	public XdatNewsentry(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatNewsentry(UserI user)
	 **/
	public XdatNewsentry()
	{}

	public XdatNewsentry(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
