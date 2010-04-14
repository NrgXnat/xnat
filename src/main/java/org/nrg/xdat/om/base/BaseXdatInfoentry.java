// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Tue Oct 23 10:47:21 CDT 2007
 *
 */
package org.nrg.xdat.om.base;
import java.util.Hashtable;

import org.nrg.xdat.om.base.auto.AutoXdatInfoentry;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatInfoentry extends AutoXdatInfoentry {

	public BaseXdatInfoentry(ItemI item)
	{
		super(item);
	}

	public BaseXdatInfoentry(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatInfoentry(UserI user)
	 **/
	public BaseXdatInfoentry()
	{}

	public BaseXdatInfoentry(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
