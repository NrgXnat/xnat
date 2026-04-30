/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatDx3dcraniofacialsessiondata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatDx3dcraniofacialsessiondata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatDx3dcraniofacialsessiondata extends AutoXnatDx3dcraniofacialsessiondata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatDx3dcraniofacialsessiondata(ItemI item)
	{
		super(item);
	}

	public BaseXnatDx3dcraniofacialsessiondata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatDx3dcraniofacialsessiondata(UserI user)
	 **/
	public BaseXnatDx3dcraniofacialsessiondata()
	{}

	public BaseXnatDx3dcraniofacialsessiondata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
