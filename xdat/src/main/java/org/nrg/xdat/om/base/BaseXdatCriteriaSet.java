/*
 * core: org.nrg.xdat.om.base.BaseXdatCriteriaSet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om.base;
import java.util.Hashtable;

import org.nrg.xdat.om.base.auto.AutoXdatCriteriaSet;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatCriteriaSet extends AutoXdatCriteriaSet {

	public BaseXdatCriteriaSet(ItemI item)
	{
		super(item);
	}

	public BaseXdatCriteriaSet(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatCriteriaSet(UserI user)
	 **/
	public BaseXdatCriteriaSet()
	{}

	public BaseXdatCriteriaSet(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
