/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatVoiceaudioscandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatVoiceaudioscandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatVoiceaudioscandata extends AutoXnatVoiceaudioscandata {

	public BaseXnatVoiceaudioscandata(ItemI item)
	{
		super(item);
	}

	public BaseXnatVoiceaudioscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatVoiceaudioscandata(UserI user)
	 **/
	public BaseXnatVoiceaudioscandata()
	{}

	public BaseXnatVoiceaudioscandata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
