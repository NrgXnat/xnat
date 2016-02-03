/*
 * org.nrg.xdat.om.base.BaseXnatFielddefinitiongroupField
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatFielddefinitiongroupField;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatFielddefinitiongroupField extends AutoXnatFielddefinitiongroupField {

	public BaseXnatFielddefinitiongroupField(ItemI item)
	{
		super(item);
	}

	public BaseXnatFielddefinitiongroupField(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatFielddefinitiongroupField(UserI user)
	 **/
	public BaseXnatFielddefinitiongroupField()
	{}

	public BaseXnatFielddefinitiongroupField(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

    public String getCleanedXMLPath(){
        String xmlPath = this.getXmlpath();
        while(xmlPath.indexOf("[")>-1){
            xmlPath= xmlPath.substring(0,xmlPath.indexOf("[")) + xmlPath.substring(xmlPath.indexOf("]")+1);
        }
        return xmlPath;
    }
}
