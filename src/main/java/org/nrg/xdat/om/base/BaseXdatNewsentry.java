// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Tue Oct 23 10:47:21 CDT 2007
 *
 */
package org.nrg.xdat.om.base;
import java.util.Hashtable;

import org.nrg.xdat.om.base.auto.AutoXdatNewsentry;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatNewsentry extends AutoXdatNewsentry {

	public BaseXdatNewsentry(ItemI item)
	{
		super(item);
	}

	public BaseXdatNewsentry(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatNewsentry(UserI user)
	 **/
	public BaseXdatNewsentry()
	{}

	public BaseXdatNewsentry(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
