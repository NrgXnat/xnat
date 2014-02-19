/*
 * org.nrg.xdat.om.XdatFieldMappingSet
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

import org.nrg.xdat.om.base.BaseXdatFieldMappingSet;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatFieldMappingSet extends BaseXdatFieldMappingSet {

	public XdatFieldMappingSet(ItemI item)
	{
		super(item);
	}

	public XdatFieldMappingSet(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatFieldMappingSet(UserI user)
	 **/
	public XdatFieldMappingSet()
	{}

	public XdatFieldMappingSet(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
