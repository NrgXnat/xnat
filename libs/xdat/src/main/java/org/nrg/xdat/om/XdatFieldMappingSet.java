/*
 * core: org.nrg.xdat.om.XdatFieldMappingSet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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
