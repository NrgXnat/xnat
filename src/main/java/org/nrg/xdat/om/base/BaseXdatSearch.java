/*
 * core: org.nrg.xdat.om.base.BaseXdatSearch
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om.base;
import java.util.Hashtable;

import org.nrg.xdat.om.base.auto.AutoXdatSearch;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatSearch extends AutoXdatSearch {

	public BaseXdatSearch(ItemI item)
	{
		super(item);
	}

	public BaseXdatSearch(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatSearch(UserI user)
	 **/
	public BaseXdatSearch()
	{}

	public BaseXdatSearch(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
