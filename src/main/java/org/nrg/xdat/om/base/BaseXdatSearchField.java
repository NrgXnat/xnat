/*
 * org.nrg.xdat.om.base.BaseXdatSearchField
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

import org.nrg.xdat.om.base.auto.AutoXdatSearchField;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatSearchField extends AutoXdatSearchField {

	public BaseXdatSearchField(ItemI item)
	{
		super(item);
	}

	public BaseXdatSearchField(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatSearchField(UserI user)
	 **/
	public BaseXdatSearchField()
	{}

	public BaseXdatSearchField(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
