// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Thu Sep 06 11:16:12 CDT 2007
 *
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
