/*
 * org.nrg.xdat.om.base.BaseXdatAccessLog
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

import org.nrg.xdat.om.base.auto.AutoXdatAccessLog;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatAccessLog extends AutoXdatAccessLog {

	public BaseXdatAccessLog(ItemI item)
	{
		super(item);
	}

	public BaseXdatAccessLog(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatAccessLog(UserI user)
	 **/
	public BaseXdatAccessLog()
	{}

	public BaseXdatAccessLog(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
