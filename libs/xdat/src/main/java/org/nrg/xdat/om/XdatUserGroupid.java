/*
 * core: org.nrg.xdat.om.XdatUserGroupid
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om;
import java.util.Hashtable;

import org.nrg.xdat.om.base.BaseXdatUserGroupid;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatUserGroupid extends BaseXdatUserGroupid {

	public XdatUserGroupid(ItemI item)
	{
		super(item);
	}

	public XdatUserGroupid(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatUserGroupid(UserI user)
	 **/
	public XdatUserGroupid()
	{}

	public XdatUserGroupid(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
