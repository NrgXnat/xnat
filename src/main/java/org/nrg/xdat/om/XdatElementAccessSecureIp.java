// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Mon Feb 26 14:11:51 CST 2007
 *
 */
package org.nrg.xdat.om;
import java.util.Hashtable;

import org.nrg.xdat.om.base.BaseXdatElementAccessSecureIp;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatElementAccessSecureIp extends BaseXdatElementAccessSecureIp {

	public XdatElementAccessSecureIp(ItemI item)
	{
		super(item);
	}

	public XdatElementAccessSecureIp(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatElementAccessSecureIp(UserI user)
	 **/
	public XdatElementAccessSecureIp()
	{}

	public XdatElementAccessSecureIp(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
