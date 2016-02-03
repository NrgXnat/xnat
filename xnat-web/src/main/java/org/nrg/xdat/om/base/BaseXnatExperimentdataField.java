/*
 * org.nrg.xdat.om.base.BaseXnatExperimentdataField
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatExperimentdataField;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatExperimentdataField extends AutoXnatExperimentdataField {

	public BaseXnatExperimentdataField(ItemI item)
	{
		super(item);
	}

	public BaseXnatExperimentdataField(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatExperimentdataField(UserI user)
	 **/
	public BaseXnatExperimentdataField()
	{}

	public BaseXnatExperimentdataField(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
