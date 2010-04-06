// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Fri Jun 29 12:54:15 CDT 2007
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
public class XdatUserGroupid extends BaseXdatUserGroupid {

	public XdatUserGroupid(ItemI item)
	{
		super(item);
	}

	public XdatUserGroupid(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatUserGroupid(UserI user)
	 **/
	public XdatUserGroupid()
	{}

	public XdatUserGroupid(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
