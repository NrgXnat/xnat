/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatRegionresource
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatRegionresource;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatRegionresource extends AutoXnatRegionresource {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatRegionresource(ItemI item)
	{
		super(item);
	}

	public BaseXnatRegionresource(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatRegionresource(UserI user)
	 **/
	public BaseXnatRegionresource()
	{}

	public BaseXnatRegionresource(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
