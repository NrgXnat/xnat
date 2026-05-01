/*
 * core: org.nrg.xdat.om.base.BaseXdatElementSecurityListingAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om.base;
import java.util.Hashtable;

import org.nrg.xdat.om.base.auto.AutoXdatElementSecurityListingAction;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
public abstract class BaseXdatElementSecurityListingAction extends AutoXdatElementSecurityListingAction {

	public BaseXdatElementSecurityListingAction(ItemI item)
	{
		super(item);
	}

	public BaseXdatElementSecurityListingAction(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatElementSecurityListingAction(UserI user)
	 **/
	public BaseXdatElementSecurityListingAction()
	{}

	public BaseXdatElementSecurityListingAction(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
