// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Fri Jun 29 12:54:15 CDT 2007
 *
 */
package org.nrg.xdat.om.base;
import java.util.Hashtable;

import org.nrg.xdat.om.base.auto.AutoXdatUserGroupid;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatUserGroupid extends AutoXdatUserGroupid {

	public BaseXdatUserGroupid(ItemI item)
	{
		super(item);
	}

	public BaseXdatUserGroupid(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatUserGroupid(UserI user)
	 **/
	public BaseXdatUserGroupid()
	{}

	public BaseXdatUserGroupid(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
