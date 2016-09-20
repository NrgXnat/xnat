/*
 * core: org.nrg.xdat.om.XdatElementActionType
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om;
import java.util.Hashtable;

import org.nrg.xdat.om.base.BaseXdatElementActionType;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatElementActionType extends BaseXdatElementActionType {

	public XdatElementActionType(ItemI item)
	{
		super(item);
	}

	public XdatElementActionType(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatElementActionType(UserI user)
	 **/
	public XdatElementActionType()
	{}

	public XdatElementActionType(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
