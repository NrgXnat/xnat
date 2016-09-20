/*
 * core: org.nrg.xdat.om.XdatActionType
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om;
import java.util.Hashtable;

import org.nrg.xdat.om.base.BaseXdatActionType;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatActionType extends BaseXdatActionType {

	public XdatActionType(ItemI item)
	{
		super(item);
	}

	public XdatActionType(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatActionType(UserI user)
	 **/
	public XdatActionType()
	{}

	public XdatActionType(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
