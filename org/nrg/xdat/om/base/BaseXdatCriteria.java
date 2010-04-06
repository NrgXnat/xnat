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
public abstract class BaseXdatCriteria extends AutoXdatCriteria {

	public BaseXdatCriteria(ItemI item)
	{
		super(item);
	}

	public BaseXdatCriteria(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatCriteria(UserI user)
	 **/
	public BaseXdatCriteria()
	{}

	public BaseXdatCriteria(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
