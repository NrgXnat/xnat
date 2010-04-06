// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Thu Sep 06 11:16:12 CDT 2007
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
public abstract class BaseXdatSearch extends AutoXdatSearch {

	public BaseXdatSearch(ItemI item)
	{
		super(item);
	}

	public BaseXdatSearch(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatSearch(UserI user)
	 **/
	public BaseXdatSearch()
	{}

	public BaseXdatSearch(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
