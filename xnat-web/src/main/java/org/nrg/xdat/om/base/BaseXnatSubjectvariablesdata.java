/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatSubjectvariablesdata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatSubjectvariablesdata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatSubjectvariablesdata extends AutoXnatSubjectvariablesdata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatSubjectvariablesdata(ItemI item)
	{
		super(item);
	}

	public BaseXnatSubjectvariablesdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatSubjectvariablesdata(UserI user)
	 **/
	public BaseXnatSubjectvariablesdata()
	{}

	public BaseXnatSubjectvariablesdata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
