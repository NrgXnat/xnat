/*
 * core: org.nrg.xdat.om.base.BaseXdatElementAccessSecureIp
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om.base;
import java.util.Hashtable;

import org.nrg.xdat.om.base.auto.AutoXdatElementAccessSecureIp;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatElementAccessSecureIp extends AutoXdatElementAccessSecureIp {

	public BaseXdatElementAccessSecureIp(ItemI item)
	{
		super(item);
	}

	public BaseXdatElementAccessSecureIp(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatElementAccessSecureIp(UserI user)
	 **/
	public BaseXdatElementAccessSecureIp()
	{}

	public BaseXdatElementAccessSecureIp(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
