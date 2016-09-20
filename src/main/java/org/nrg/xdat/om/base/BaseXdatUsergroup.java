/*
 * core: org.nrg.xdat.om.base.BaseXdatUsergroup
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om.base;
import java.util.Hashtable;

import org.nrg.xdat.om.base.auto.AutoXdatUsergroup;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatUsergroup extends AutoXdatUsergroup {

	public BaseXdatUsergroup(ItemI item)
	{
		super(item);
	}

	public BaseXdatUsergroup(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatUsergroup(UserI user)
	 **/
	public BaseXdatUsergroup()
	{}

	public BaseXdatUsergroup(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
