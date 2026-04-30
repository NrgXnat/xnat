/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatStatisticsdataAdditionalstatistics
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatStatisticsdataAdditionalstatistics;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatStatisticsdataAdditionalstatistics extends AutoXnatStatisticsdataAdditionalstatistics {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatStatisticsdataAdditionalstatistics(ItemI item)
	{
		super(item);
	}

	public BaseXnatStatisticsdataAdditionalstatistics(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatStatisticsdataAdditionalstatistics(UserI user)
	 **/
	public BaseXnatStatisticsdataAdditionalstatistics()
	{}

	public BaseXnatStatisticsdataAdditionalstatistics(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
