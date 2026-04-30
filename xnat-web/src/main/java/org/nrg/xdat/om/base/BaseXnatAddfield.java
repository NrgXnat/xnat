/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatAddfield
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatAddfield;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatAddfield extends AutoXnatAddfield {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatAddfield(ItemI item)
	{
		super(item);
	}

	public BaseXnatAddfield(UserI user)
	{
		super(user);
	}

	public BaseXnatAddfield()
	{}

	public BaseXnatAddfield(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
