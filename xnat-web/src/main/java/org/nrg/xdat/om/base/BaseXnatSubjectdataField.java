/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatSubjectdataField
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatSubjectdataField;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatSubjectdataField extends AutoXnatSubjectdataField {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatSubjectdataField(ItemI item)
	{
		super(item);
	}

	public BaseXnatSubjectdataField(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatSubjectdataField(UserI user)
	 **/
	public BaseXnatSubjectdataField()
	{}

	public BaseXnatSubjectdataField(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
