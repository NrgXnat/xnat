/*
 * org.nrg.xdat.om.base.BaseXnatVolumetricregion
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.XnatVolumetricregionSubregion;
import org.nrg.xdat.om.base.auto.AutoXnatVolumetricregion;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatVolumetricregion extends AutoXnatVolumetricregion {

	public BaseXnatVolumetricregion(ItemI item)
	{
		super(item);
	}

	public BaseXnatVolumetricregion(UserI user)
	{
		super(user);
	}

	public BaseXnatVolumetricregion()
	{}

	public BaseXnatVolumetricregion(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

    public BaseXnatVolumetricregionSubregion getSubRegionByName(String name)
    {
        List al = this.getSubregions_subregion();
        Iterator iter = al.iterator();
        while (iter.hasNext())
        {
            XnatVolumetricregionSubregion sub = (XnatVolumetricregionSubregion)iter.next();
            if (sub.getName().equalsIgnoreCase(name))
            {
                return sub;
            }
        }
        
        return null;
    }
}
