/*
 * core: org.nrg.xdat.om.base.BaseXdatUserGroupid
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om.base;
import java.util.Hashtable;

import org.nrg.xdat.om.base.auto.AutoXdatUserGroupid;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatUserGroupid extends AutoXdatUserGroupid {

	public BaseXdatUserGroupid(ItemI item)
	{
		super(item);
	}

	public BaseXdatUserGroupid(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatUserGroupid(UserI user)
	 **/
	public BaseXdatUserGroupid()
	{}

	public BaseXdatUserGroupid(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
