// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Mon Feb 26 14:11:51 CST 2007
 *
 */
package org.nrg.xdat.om;
import java.util.Hashtable;

import org.nrg.xdat.om.base.BaseXdatRoleType;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatRoleType extends BaseXdatRoleType {

	public XdatRoleType(ItemI item)
	{
		super(item);
	}

	public XdatRoleType(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatRoleType(UserI user)
	 **/
	public XdatRoleType()
	{}

	public XdatRoleType(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
