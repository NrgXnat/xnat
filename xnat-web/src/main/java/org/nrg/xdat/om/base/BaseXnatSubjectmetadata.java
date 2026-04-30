/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatSubjectmetadata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatSubjectmetadata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatSubjectmetadata extends AutoXnatSubjectmetadata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatSubjectmetadata(ItemI item)
	{
		super(item);
	}

	public BaseXnatSubjectmetadata(UserI user)
	{
		super(user);
	}

	public BaseXnatSubjectmetadata()
	{}

	public BaseXnatSubjectmetadata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
