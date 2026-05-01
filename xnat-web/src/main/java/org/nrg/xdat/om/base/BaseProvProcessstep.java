/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseProvProcessstep
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoProvProcessstep;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseProvProcessstep extends AutoProvProcessstep {

	public BaseProvProcessstep(ItemI item)
	{
		super(item);
	}

	public BaseProvProcessstep(UserI user)
	{
		super(user);
	}

	public BaseProvProcessstep()
	{}

	public BaseProvProcessstep(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
