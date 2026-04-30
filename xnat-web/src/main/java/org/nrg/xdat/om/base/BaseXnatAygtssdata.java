/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatAygtssdata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatAygtssdata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatAygtssdata extends AutoXnatAygtssdata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatAygtssdata(ItemI item)
	{
		super(item);
	}

	public BaseXnatAygtssdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatAygtssdata(UserI user)
	 **/
	public BaseXnatAygtssdata()
	{}

	public BaseXnatAygtssdata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}

