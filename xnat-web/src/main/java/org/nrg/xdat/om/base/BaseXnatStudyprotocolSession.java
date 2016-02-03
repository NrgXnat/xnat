/*
 * org.nrg.xdat.om.base.BaseXnatStudyprotocolSession
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatStudyprotocolSession;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatStudyprotocolSession extends AutoXnatStudyprotocolSession {

	public BaseXnatStudyprotocolSession(ItemI item)
	{
		super(item);
	}

	public BaseXnatStudyprotocolSession(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatStudyprotocolSession(UserI user)
	 **/
	public BaseXnatStudyprotocolSession()
	{}

	public BaseXnatStudyprotocolSession(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
