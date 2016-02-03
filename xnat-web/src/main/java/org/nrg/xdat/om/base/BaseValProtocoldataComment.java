/*
 * org.nrg.xdat.om.base.BaseValProtocoldataComment
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoValProtocoldataComment;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseValProtocoldataComment extends AutoValProtocoldataComment {

	public BaseValProtocoldataComment(ItemI item)
	{
		super(item);
	}

	public BaseValProtocoldataComment(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseValProtocoldataComment(UserI user)
	 **/
	public BaseValProtocoldataComment()
	{}

	public BaseValProtocoldataComment(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
