/*
 * org.nrg.xdat.om.XdatFieldMapping
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

import org.nrg.xdat.om.base.BaseXdatFieldMapping;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatFieldMapping extends BaseXdatFieldMapping {

	public XdatFieldMapping(ItemI item)
	{
		super(item);
	}

	public XdatFieldMapping(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatFieldMapping(UserI user)
	 **/
	public XdatFieldMapping()
	{}

	public XdatFieldMapping(Hashtable properties, UserI user)
	{
		super(properties,user);
	}
	
	public void init(String psf,String value,Boolean create,Boolean read,Boolean delete,Boolean edit,Boolean activate){
		this.setField(psf);
		this.setFieldValue(value);

		this.setCreateElement(create);
		this.setReadElement(read);
		this.setEditElement(edit);
		this.setDeleteElement(delete);
		this.setActiveElement(activate);
		this.setComparisonType("equals");
	}

}
