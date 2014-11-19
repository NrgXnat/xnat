/*
 * org.nrg.xdat.om.XdatStoredSearchGroupid
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.om;
import java.util.Hashtable;

import org.nrg.xdat.om.base.BaseXdatStoredSearchGroupid;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatStoredSearchGroupid extends BaseXdatStoredSearchGroupid {

	public XdatStoredSearchGroupid(ItemI item)
	{
		super(item);
	}

	public XdatStoredSearchGroupid(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatStoredSearchGroupid(UserI user)
	 **/
	public XdatStoredSearchGroupid()
	{}

	public XdatStoredSearchGroupid(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
