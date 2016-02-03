/*
 * org.nrg.xdat.om.base.BaseArcProjectPipeline
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoArcProjectPipeline;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseArcProjectPipeline extends AutoArcProjectPipeline {

	public BaseArcProjectPipeline(ItemI item)
	{
		super(item);
	}

	public BaseArcProjectPipeline(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseArcProjectPipeline(UserI user)
	 **/
	public BaseArcProjectPipeline()
	{}

	public BaseArcProjectPipeline(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

    public boolean hasCustomwebpage() {
        boolean rtn = false;
        if (getCustomwebpage()!= null) {
            rtn = true;
        }
        return rtn;
    }


}
