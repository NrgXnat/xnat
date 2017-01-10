/*
 * core: org.nrg.xdat.om.XdatElementAccess
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om;
import java.util.Hashtable;

import org.nrg.xdat.om.base.BaseXdatElementAccess;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatElementAccess extends BaseXdatElementAccess {

	public XdatElementAccess(ItemI item)
	{
		super(item);
	}

	public XdatElementAccess(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatElementAccess(UserI user)
	 **/
	public XdatElementAccess()
	{}

	public XdatElementAccess(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
