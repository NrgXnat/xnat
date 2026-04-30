/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatStudyprotocolGroup
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatStudyprotocolGroup;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatStudyprotocolGroup extends AutoXnatStudyprotocolGroup {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatStudyprotocolGroup(ItemI item)
	{
		super(item);
	}

	public BaseXnatStudyprotocolGroup(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatStudyprotocolGroup(UserI user)
	 **/
	public BaseXnatStudyprotocolGroup()
	{}

	public BaseXnatStudyprotocolGroup(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
