// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Mon Feb 26 14:11:51 CST 2007
 *
 */
package org.nrg.xdat.om.base;
import org.nrg.xdat.om.base.auto.*;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.XdatCriteriaSet;
import org.nrg.xft.*;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.security.UserI;

import java.util.*;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatStoredSearch extends AutoXdatStoredSearch {

	public BaseXdatStoredSearch(ItemI item)
	{
		super(item);
	}

	public BaseXdatStoredSearch(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatStoredSearch(UserI user)
	 **/
	public BaseXdatStoredSearch()
	{}

	public BaseXdatStoredSearch(Hashtable properties, UserI user)
	{
		super(properties,user);
	}
}
