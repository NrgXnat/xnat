// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Mon Feb 26 14:11:51 CST 2007
 *
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
