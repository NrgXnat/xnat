// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Mon Feb 26 14:11:51 CST 2007
 *
 */
package org.nrg.xdat.om;
import java.util.Hashtable;

import org.nrg.xdat.om.base.BaseXdatFieldMapping;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatFieldMapping extends BaseXdatFieldMapping {

	public XdatFieldMapping(ItemI item)
	{
		super(item);
	}

	public XdatFieldMapping(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatFieldMapping(UserI user)
	 **/
	public XdatFieldMapping()
	{}

	public XdatFieldMapping(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
