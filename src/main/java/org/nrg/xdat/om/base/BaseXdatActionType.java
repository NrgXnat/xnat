/*
 * org.nrg.xdat.om.base.BaseXdatActionType
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

import org.nrg.xdat.om.base.auto.AutoXdatActionType;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatActionType extends AutoXdatActionType {

	public BaseXdatActionType(ItemI item)
	{
		super(item);
	}

	public BaseXdatActionType(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatActionType(UserI user)
	 **/
	public BaseXdatActionType()
	{}

	public BaseXdatActionType(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
