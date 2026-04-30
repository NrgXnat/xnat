/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatFielddefinitiongroupFieldPossiblevalue
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatFielddefinitiongroupFieldPossiblevalue;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatFielddefinitiongroupFieldPossiblevalue extends AutoXnatFielddefinitiongroupFieldPossiblevalue {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatFielddefinitiongroupFieldPossiblevalue(ItemI item)
	{
		super(item);
	}

	public BaseXnatFielddefinitiongroupFieldPossiblevalue(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatFielddefinitiongroupFieldPossiblevalue(UserI user)
	 **/
	public BaseXnatFielddefinitiongroupFieldPossiblevalue()
	{}

	public BaseXnatFielddefinitiongroupFieldPossiblevalue(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
