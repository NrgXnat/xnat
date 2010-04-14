// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Mon Feb 26 14:11:51 CST 2007
 *
 */
package org.nrg.xdat.om;
import java.util.Hashtable;

import org.nrg.xdat.om.base.BaseXdatFieldMappingSet;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatFieldMappingSet extends BaseXdatFieldMappingSet {

	public XdatFieldMappingSet(ItemI item)
	{
		super(item);
	}

	public XdatFieldMappingSet(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatFieldMappingSet(UserI user)
	 **/
	public XdatFieldMappingSet()
	{}

	public XdatFieldMappingSet(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

}
