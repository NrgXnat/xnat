/*
 * org.nrg.xdat.om.XdatElementAccessSecureIp
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
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
