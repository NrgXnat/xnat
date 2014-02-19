/*
 * org.nrg.xdat.om.base.BaseXdatPrimarySecurityField
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

import org.nrg.xdat.om.base.auto.AutoXdatPrimarySecurityField;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatPrimarySecurityField extends AutoXdatPrimarySecurityField {

	public BaseXdatPrimarySecurityField(ItemI item)
	{
		super(item);
	}

	public BaseXdatPrimarySecurityField(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatPrimarySecurityField(UserI user)
	 **/
	public BaseXdatPrimarySecurityField()
	{}

	public BaseXdatPrimarySecurityField(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
