/*
 * org.nrg.xdat.om.base.BaseXdatElementActionType
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

import org.nrg.xdat.om.base.auto.AutoXdatElementActionType;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatElementActionType extends AutoXdatElementActionType {

	public BaseXdatElementActionType(ItemI item)
	{
		super(item);
	}

	public BaseXdatElementActionType(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatElementActionType(UserI user)
	 **/
	public BaseXdatElementActionType()
	{}

	public BaseXdatElementActionType(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
