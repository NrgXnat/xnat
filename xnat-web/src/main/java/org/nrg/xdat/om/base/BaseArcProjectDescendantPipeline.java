/*
 * org.nrg.xdat.om.base.BaseArcProjectDescendantPipeline
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoArcProjectDescendantPipeline;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseArcProjectDescendantPipeline extends AutoArcProjectDescendantPipeline {

	public BaseArcProjectDescendantPipeline(ItemI item)
	{
		super(item);
	}

	public BaseArcProjectDescendantPipeline(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseArcProjectDescendantPipeline(UserI user)
	 **/
	public BaseArcProjectDescendantPipeline()
	{}

	public BaseArcProjectDescendantPipeline(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

    public boolean hasCustomwebpage() {
        boolean rtn = false;
        if (getCustomwebpage() !=null)
            rtn = true;
        return rtn;
    }

}
