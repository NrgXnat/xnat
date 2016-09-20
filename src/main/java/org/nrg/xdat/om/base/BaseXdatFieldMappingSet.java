/*
 * core: org.nrg.xdat.om.base.BaseXdatFieldMappingSet
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om.base;
import java.util.Hashtable;

import org.nrg.xdat.om.base.auto.AutoXdatFieldMappingSet;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatFieldMappingSet extends AutoXdatFieldMappingSet {

	public BaseXdatFieldMappingSet(ItemI item)
	{
		super(item);
	}

	public BaseXdatFieldMappingSet(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatFieldMappingSet(UserI user)
	 **/
	public BaseXdatFieldMappingSet()
	{}

	public BaseXdatFieldMappingSet(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
