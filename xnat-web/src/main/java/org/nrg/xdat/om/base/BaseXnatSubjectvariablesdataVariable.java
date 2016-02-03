/*
 * org.nrg.xdat.om.base.BaseXnatSubjectvariablesdataVariable
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatSubjectvariablesdataVariable;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatSubjectvariablesdataVariable extends AutoXnatSubjectvariablesdataVariable {

	public BaseXnatSubjectvariablesdataVariable(ItemI item)
	{
		super(item);
	}

	public BaseXnatSubjectvariablesdataVariable(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatSubjectvariablesdataVariable(UserI user)
	 **/
	public BaseXnatSubjectvariablesdataVariable()
	{}

	public BaseXnatSubjectvariablesdataVariable(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
