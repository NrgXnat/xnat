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
public class XdatStoredSearchAllowedUser extends BaseXdatStoredSearchAllowedUser {

	public XdatStoredSearchAllowedUser(ItemI item)
	{
		super(item);
	}

	public XdatStoredSearchAllowedUser(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatStoredSearchAllowedUser(UserI user)
	 **/
	public XdatStoredSearchAllowedUser()
	{}

	public XdatStoredSearchAllowedUser(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
