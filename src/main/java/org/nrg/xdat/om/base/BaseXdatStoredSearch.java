/*
 * org.nrg.xdat.om.base.BaseXdatStoredSearch
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

import org.nrg.xdat.om.base.auto.AutoXdatStoredSearch;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatStoredSearch extends AutoXdatStoredSearch {

	public BaseXdatStoredSearch(ItemI item)
	{
		super(item);
	}

	public BaseXdatStoredSearch(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatStoredSearch(UserI user)
	 **/
	public BaseXdatStoredSearch()
	{}

	public BaseXdatStoredSearch(Hashtable properties, UserI user)
	{
		super(properties,user);
	}
}
