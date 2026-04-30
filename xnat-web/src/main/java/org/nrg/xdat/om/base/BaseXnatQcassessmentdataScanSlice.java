/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatQcassessmentdataScanSlice
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatQcassessmentdataScanSlice;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatQcassessmentdataScanSlice extends AutoXnatQcassessmentdataScanSlice {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatQcassessmentdataScanSlice(ItemI item)
	{
		super(item);
	}

	public BaseXnatQcassessmentdataScanSlice(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatQcassessmentdataScanSlice(UserI user)
	 **/
	public BaseXnatQcassessmentdataScanSlice()
	{}

	public BaseXnatQcassessmentdataScanSlice(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
