/*
 * core: org.nrg.xdat.om.XdatAccessLog
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om;
import java.util.Hashtable;

import org.nrg.xdat.om.base.BaseXdatAccessLog;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatAccessLog extends BaseXdatAccessLog {

	public XdatAccessLog(ItemI item)
	{
		super(item);
	}

	public XdatAccessLog(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatAccessLog(UserI user)
	 **/
	public XdatAccessLog()
	{}

	public XdatAccessLog(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
