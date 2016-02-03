/*
 * org.nrg.xdat.om.base.BaseXnatRegionresourceLabel
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatRegionresourceLabel;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatRegionresourceLabel extends AutoXnatRegionresourceLabel {

	public BaseXnatRegionresourceLabel(ItemI item)
	{
		super(item);
	}

	public BaseXnatRegionresourceLabel(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatRegionresourceLabel(UserI user)
	 **/
	public BaseXnatRegionresourceLabel()
	{}

	public BaseXnatRegionresourceLabel(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
