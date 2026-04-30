/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseProvProcessstepLibrary
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoProvProcessstepLibrary;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseProvProcessstepLibrary extends AutoProvProcessstepLibrary {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseProvProcessstepLibrary(ItemI item)
	{
		super(item);
	}

	public BaseProvProcessstepLibrary(UserI user)
	{
		super(user);
	}

	public BaseProvProcessstepLibrary()
	{}

	public BaseProvProcessstepLibrary(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
