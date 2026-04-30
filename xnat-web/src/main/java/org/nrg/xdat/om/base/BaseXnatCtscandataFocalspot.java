/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatCtscandataFocalspot
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatCtscandataFocalspot;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatCtscandataFocalspot extends AutoXnatCtscandataFocalspot {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatCtscandataFocalspot(ItemI item)
	{
		super(item);
	}

	public BaseXnatCtscandataFocalspot(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatCtscandataFocalspot(UserI user)
	 **/
	public BaseXnatCtscandataFocalspot()
	{}

	public BaseXnatCtscandataFocalspot(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}

