/*
 * core: org.nrg.xdat.om.base.BaseXdatFieldMapping
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om.base;
import java.util.Hashtable;

import org.nrg.xdat.om.base.auto.AutoXdatFieldMapping;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatFieldMapping extends AutoXdatFieldMapping {

	public BaseXdatFieldMapping(ItemI item)
	{
		super(item);
	}

	public BaseXdatFieldMapping(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatFieldMapping(UserI user)
	 **/
	public BaseXdatFieldMapping()
	{}

	public BaseXdatFieldMapping(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
