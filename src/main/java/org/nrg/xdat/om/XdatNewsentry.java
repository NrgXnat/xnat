// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Tue Oct 23 10:47:21 CDT 2007
 *
 */
package org.nrg.xdat.om;
import org.nrg.xft.*;
import org.nrg.xdat.om.base.*;
import org.nrg.xft.security.UserI;

import java.util.*;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatNewsentry extends BaseXdatNewsentry {

	public XdatNewsentry(ItemI item)
	{
		super(item);
	}

	public XdatNewsentry(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatNewsentry(UserI user)
	 **/
	public XdatNewsentry()
	{}

	public XdatNewsentry(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
