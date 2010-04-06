// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Tue Oct 23 10:47:21 CDT 2007
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
public abstract class BaseXdatInfoentry extends AutoXdatInfoentry {

	public BaseXdatInfoentry(ItemI item)
	{
		super(item);
	}

	public BaseXdatInfoentry(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatInfoentry(UserI user)
	 **/
	public BaseXdatInfoentry()
	{}

	public BaseXdatInfoentry(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
