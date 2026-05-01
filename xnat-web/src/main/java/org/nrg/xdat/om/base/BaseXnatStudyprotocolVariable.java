/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatStudyprotocolVariable
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatStudyprotocolVariable;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatStudyprotocolVariable extends AutoXnatStudyprotocolVariable {

	public BaseXnatStudyprotocolVariable(ItemI item)
	{
		super(item);
	}

	public BaseXnatStudyprotocolVariable(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatStudyprotocolVariable(UserI user)
	 **/
	public BaseXnatStudyprotocolVariable()
	{}

	public BaseXnatStudyprotocolVariable(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
