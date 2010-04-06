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
public abstract class BaseXdatSearchField extends AutoXdatSearchField {

	public BaseXdatSearchField(ItemI item)
	{
		super(item);
	}

	public BaseXdatSearchField(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatSearchField(UserI user)
	 **/
	public BaseXdatSearchField()
	{}

	public BaseXdatSearchField(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
