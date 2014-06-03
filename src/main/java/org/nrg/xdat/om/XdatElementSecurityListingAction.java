/*
 * org.nrg.xdat.om.XdatElementSecurityListingAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.om;
import java.util.Hashtable;

import org.nrg.xdat.om.base.BaseXdatElementSecurityListingAction;
import org.nrg.xft.ItemI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatElementSecurityListingAction extends BaseXdatElementSecurityListingAction {

	public XdatElementSecurityListingAction(ItemI item)
	{
		super(item);
	}

	public XdatElementSecurityListingAction(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatElementSecurityListingAction(UserI user)
	 **/
	public XdatElementSecurityListingAction()
	{}

	public XdatElementSecurityListingAction(Hashtable properties, UserI user)
	{
		super(properties,user);
	}


	public boolean hasImage() throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		try {
			if (getImage()== null)
			{
			    return false;
			}else{
			    return true;
			}
		} catch (Exception e) {
			return false;
		}
	}
}
