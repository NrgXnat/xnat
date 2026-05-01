/*
 * core: org.nrg.xdat.om.base.BaseXdatSearchField
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om.base;
import java.util.Hashtable;

import org.nrg.xdat.om.base.auto.AutoXdatSearchField;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatSearchField extends AutoXdatSearchField {

	public BaseXdatSearchField(ItemI item)
	{
		super(item);
	}

	public BaseXdatSearchField(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatSearchField(UserI user)
	 **/
	public BaseXdatSearchField()
	{}

	public BaseXdatSearchField(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
