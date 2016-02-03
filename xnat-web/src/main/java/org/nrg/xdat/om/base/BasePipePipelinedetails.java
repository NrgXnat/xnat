/*
 * org.nrg.xdat.om.base.BasePipePipelinedetails
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoPipePipelinedetails;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BasePipePipelinedetails extends AutoPipePipelinedetails {

	public BasePipePipelinedetails(ItemI item)
	{
		super(item);
	}

	public BasePipePipelinedetails(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BasePipePipelinedetails(UserI user)
	 **/
	public BasePipePipelinedetails()
	{}

	public BasePipePipelinedetails(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
