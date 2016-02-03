/*
 * org.nrg.xdat.om.base.BaseXnatAbstractresourceTag
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatAbstractresourceTag;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatAbstractresourceTag extends AutoXnatAbstractresourceTag {

	public BaseXnatAbstractresourceTag(ItemI item)
	{
		super(item);
	}

	public BaseXnatAbstractresourceTag(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatAbstractresourceTag(UserI user)
	 **/
	public BaseXnatAbstractresourceTag()
	{}

	public BaseXnatAbstractresourceTag(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
