// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Mon Feb 26 14:11:51 CST 2007
 *
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
