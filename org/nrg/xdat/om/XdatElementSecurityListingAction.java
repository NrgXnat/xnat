// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Mon Feb 26 14:11:51 CST 2007
 *
 */
package org.nrg.xdat.om;
import org.nrg.xft.*;
import org.nrg.xdat.om.base.*;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;

import java.util.*;

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
