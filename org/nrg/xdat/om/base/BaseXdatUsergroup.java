// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Fri Jun 29 12:54:15 CDT 2007
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
public abstract class BaseXdatUsergroup extends AutoXdatUsergroup {

	public BaseXdatUsergroup(ItemI item)
	{
		super(item);
	}

	public BaseXdatUsergroup(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatUsergroup(UserI user)
	 **/
	public BaseXdatUsergroup()
	{}

	public BaseXdatUsergroup(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
