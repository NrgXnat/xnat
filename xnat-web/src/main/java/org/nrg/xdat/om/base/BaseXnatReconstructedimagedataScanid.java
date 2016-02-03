/*
 * org.nrg.xdat.om.base.BaseXnatReconstructedimagedataScanid
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatReconstructedimagedataScanid;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatReconstructedimagedataScanid extends AutoXnatReconstructedimagedataScanid {

	public BaseXnatReconstructedimagedataScanid(ItemI item)
	{
		super(item);
	}

	public BaseXnatReconstructedimagedataScanid(UserI user)
	{
		super(user);
	}

	public BaseXnatReconstructedimagedataScanid()
	{}

	public BaseXnatReconstructedimagedataScanid(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
