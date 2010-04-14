// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Wed Jul 18 12:37:49 CDT 2007
 *
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
