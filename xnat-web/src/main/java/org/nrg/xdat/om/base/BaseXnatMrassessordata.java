/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatMrassessordata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.model.XnatAddfieldI;
import org.nrg.xdat.om.XnatMrsessiondata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.io.Serial;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatMrassessordata extends org.nrg.xdat.om.base.auto.AutoXnatMrassessordata {

    @Serial
    private static final long serialVersionUID = 1;

	public BaseXnatMrassessordata(ItemI item)
	{
		super(item);
	}

	public BaseXnatMrassessordata(UserI user)
	{
		super(user);
	}

	public BaseXnatMrassessordata()
	{}

	public BaseXnatMrassessordata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}


	public XnatMrsessiondata getMrSessionData()
	{
	    return (XnatMrsessiondata)this.getImageSessionData();
	}

    Hashtable parametersByName = null;
    public Hashtable getAddParametersByName(){
        if (parametersByName == null){
            parametersByName=new Hashtable();
            Iterator iter = this.getParameters_addparam().iterator();
            while (iter.hasNext()){
                XnatAddfieldI field = (XnatAddfieldI)iter.next();
                parametersByName.put(field.getName(), field);
            }
        }

        return parametersByName;
    }

    public Object getAddParameterByName(String s){
        XnatAddfieldI field = (XnatAddfieldI)getAddParametersByName().get(s);
        if (field!=null){
            return field.getAddfield();
        }else{
            return null;
        }
    }
}
