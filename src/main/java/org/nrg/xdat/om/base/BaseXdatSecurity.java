// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Mon Feb 26 14:11:51 CST 2007
 *
 */
package org.nrg.xdat.om.base;
import org.nrg.xdat.om.base.auto.*;
import org.nrg.xft.*;
import org.nrg.xft.security.UserI;

import java.util.*;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatSecurity extends AutoXdatSecurity {

	public BaseXdatSecurity(ItemI item)
	{
		super(item);
	}

	public BaseXdatSecurity(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatSecurity(UserI user)
	 **/
	public BaseXdatSecurity()
	{}

	public BaseXdatSecurity(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
