// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Mon Feb 26 14:11:51 CST 2007
 *
 */
package org.nrg.xdat.om;
import org.nrg.xft.*;
import org.nrg.xdat.om.base.*;
import org.nrg.xft.security.UserI;

import java.util.*;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatElementSecurity extends BaseXdatElementSecurity {

	public XdatElementSecurity(ItemI item)
	{
		super(item);
	}

	public XdatElementSecurity(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatElementSecurity(UserI user)
	 **/
	public XdatElementSecurity()
	{}

	public XdatElementSecurity(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
