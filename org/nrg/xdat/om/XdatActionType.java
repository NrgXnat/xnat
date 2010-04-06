// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Mon Feb 26 14:11:51 CST 2007
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
public class XdatActionType extends BaseXdatActionType {

	public XdatActionType(ItemI item)
	{
		super(item);
	}

	public XdatActionType(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatActionType(UserI user)
	 **/
	public XdatActionType()
	{}

	public XdatActionType(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
