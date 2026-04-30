/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatSubjectvariablesdataVariable
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatSubjectvariablesdataVariable;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatSubjectvariablesdataVariable extends AutoXnatSubjectvariablesdataVariable {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatSubjectvariablesdataVariable(ItemI item)
	{
		super(item);
	}

	public BaseXnatSubjectvariablesdataVariable(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatSubjectvariablesdataVariable(UserI user)
	 **/
	public BaseXnatSubjectvariablesdataVariable()
	{}

	public BaseXnatSubjectvariablesdataVariable(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
