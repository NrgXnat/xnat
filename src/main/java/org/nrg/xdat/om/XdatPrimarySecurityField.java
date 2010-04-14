// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Mon Feb 26 14:11:51 CST 2007
 *
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
