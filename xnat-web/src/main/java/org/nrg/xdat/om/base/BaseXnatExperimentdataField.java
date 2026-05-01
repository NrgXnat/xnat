/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatExperimentdataField
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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



	/**
	 * Sets the value for field.
	 * @param v Value to Set.
	 */
	public void setField(String v){
		try{
			super.setField((Object)v);
		} catch (Exception e1) {logger.error(e1);}
	}

}
