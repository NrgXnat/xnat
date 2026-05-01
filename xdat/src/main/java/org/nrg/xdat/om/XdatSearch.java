/*
 * core: org.nrg.xdat.om.XdatSearch
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om;
import java.util.Hashtable;

import org.nrg.xdat.om.base.BaseXdatSearch;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatSearch extends BaseXdatSearch {

	public XdatSearch(ItemI item)
	{
		super(item);
	}

	public XdatSearch(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatSearch(UserI user)
	 **/
	public XdatSearch()
	{}

	public XdatSearch(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
