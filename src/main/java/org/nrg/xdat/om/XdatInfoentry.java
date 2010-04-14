// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Tue Oct 23 10:47:21 CDT 2007
 *
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
