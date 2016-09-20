/*
 * core: org.nrg.xdat.om.XdatPrimarySecurityField
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om;
import java.util.Hashtable;

import org.nrg.xdat.om.base.BaseXdatPrimarySecurityField;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatPrimarySecurityField extends BaseXdatPrimarySecurityField {

	public XdatPrimarySecurityField(ItemI item)
	{
		super(item);
	}

	public XdatPrimarySecurityField(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatPrimarySecurityField(UserI user)
	 **/
	public XdatPrimarySecurityField()
	{}

	public XdatPrimarySecurityField(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
