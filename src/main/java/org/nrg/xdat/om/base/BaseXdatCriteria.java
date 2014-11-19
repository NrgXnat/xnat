/*
 * org.nrg.xdat.om.base.BaseXdatCriteria
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

import org.nrg.xdat.om.base.auto.AutoXdatCriteria;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatCriteria extends AutoXdatCriteria {

	public BaseXdatCriteria(ItemI item)
	{
		super(item);
	}

	public BaseXdatCriteria(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatCriteria(UserI user)
	 **/
	public BaseXdatCriteria()
	{}

	public BaseXdatCriteria(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
