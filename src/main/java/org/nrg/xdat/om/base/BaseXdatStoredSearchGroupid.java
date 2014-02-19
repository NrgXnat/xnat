/*
 * org.nrg.xdat.om.base.BaseXdatStoredSearchGroupid
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

import org.nrg.xdat.om.base.auto.AutoXdatStoredSearchGroupid;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatStoredSearchGroupid extends AutoXdatStoredSearchGroupid {

	public BaseXdatStoredSearchGroupid(ItemI item)
	{
		super(item);
	}

	public BaseXdatStoredSearchGroupid(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatStoredSearchGroupid(UserI user)
	 **/
	public BaseXdatStoredSearchGroupid()
	{}

	public BaseXdatStoredSearchGroupid(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
