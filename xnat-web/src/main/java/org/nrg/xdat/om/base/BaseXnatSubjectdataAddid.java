/*
 * org.nrg.xdat.om.base.BaseXnatSubjectdataAddid
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatSubjectdataAddid;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatSubjectdataAddid extends AutoXnatSubjectdataAddid {

	public BaseXnatSubjectdataAddid(ItemI item)
	{
		super(item);
	}

	public BaseXnatSubjectdataAddid(UserI user)
	{
		super(user);
	}

	public BaseXnatSubjectdataAddid()
	{}

	public BaseXnatSubjectdataAddid(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
