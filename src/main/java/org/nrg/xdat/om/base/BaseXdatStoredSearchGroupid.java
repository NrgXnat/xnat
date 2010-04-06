// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Wed Jul 18 12:37:49 CDT 2007
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
public abstract class BaseXdatStoredSearchGroupid extends AutoXdatStoredSearchGroupid {

	public BaseXdatStoredSearchGroupid(ItemI item)
	{
		super(item);
	}

	public BaseXdatStoredSearchGroupid(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatStoredSearchGroupid(UserI user)
	 **/
	public BaseXdatStoredSearchGroupid()
	{}

	public BaseXdatStoredSearchGroupid(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
