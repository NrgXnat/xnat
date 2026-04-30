/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatDemographicdata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatDemographicdata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatDemographicdata extends AutoXnatDemographicdata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatDemographicdata(ItemI item)
	{
		super(item);
	}

	public BaseXnatDemographicdata(UserI user)
	{
		super(user);
	}

	public BaseXnatDemographicdata()
	{}

	public BaseXnatDemographicdata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
