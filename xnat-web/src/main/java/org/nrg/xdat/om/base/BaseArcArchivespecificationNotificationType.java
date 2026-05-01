/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseArcArchivespecificationNotificationType
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoArcArchivespecificationNotificationType;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseArcArchivespecificationNotificationType extends AutoArcArchivespecificationNotificationType {

	public BaseArcArchivespecificationNotificationType(ItemI item)
	{
		super(item);
	}

	public BaseArcArchivespecificationNotificationType(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseArcArchivespecificationNotificationType(UserI user)
	 **/
	public BaseArcArchivespecificationNotificationType()
	{}

	public BaseArcArchivespecificationNotificationType(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
