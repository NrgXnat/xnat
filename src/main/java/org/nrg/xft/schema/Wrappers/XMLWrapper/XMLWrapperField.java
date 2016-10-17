/*
 * core: org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWrapperField
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.schema.Wrappers.XMLWrapper;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.XMLType;
import org.nrg.xft.schema.design.XFTFactoryI;

import java.util.ArrayList;
import java.util.Iterator;
public class XMLWrapperField extends GenericWrapperField implements XMLNode{

	/* (non-Javadoc)
	 * @see org.nrg.xft.schema.design.XFTFieldWrapper#getFactory()
	 */
	public XFTFactoryI getFactory()
	{
		return XMLWrapperFactory.GetInstance();
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.schema.Wrappers.XMLWrapper.XMLNode#getChildren()
	 */
	/**
	* Includes all child nodes where the field's expose is true and it is not an attribute.
	* @return ArrayList of XMLWrapperField
	*/
	public ArrayList getChildren()
	{
		ArrayList al = new ArrayList();
		
		Iterator iter = this.getDirectFields().iterator();
		while (iter.hasNext())
		{
			XMLWrapperField child = (XMLWrapperField) iter.next();
			if((child.getExpose()) && (!child.isAttribute()))
			{
				al.add(child);
			}
		}
		
		al.trimToSize();
		return al;
	}

	/**
	* Includes all child nodes where the field's expose is true and it is an attribute.
	* @return ArrayList of XMLWrapperField
	*/
	public ArrayList getAttributes()
	{
		ArrayList al = new ArrayList();
		
		Iterator iter = this.getDirectFields().iterator();
		while (iter.hasNext())
		{
			XMLWrapperField child = (XMLWrapperField) iter.next();
			if((child.getExpose()) && (child.isAttribute()))
			{
				al.add(child);
			}
		}
		
		al.trimToSize();
		return al;
	}
	

	
	/**
	 * if wrapped field has a displayName then that is returned, else
	 * the wrapped field's name is returned.
	 * @return Returns the String name of the wrapped field
	 */
	public String getName(boolean withPrefix)
	{
		if (withPrefix)
		    return this.getPrefix() +":" + wrapped.getName();
	    else{
		    return wrapped.getName();
	    }
	}
    
    public String getDisplayName()
    {
        if (wrapped.getDisplayName() != "")
        {
            return wrapped.getDisplayName();
        }else
        {
            return wrapped.getName();
        }
    }
    
    public XMLType getExternalXMLType(){
        return new XMLType(getName(true),this.getParentElement().getGenericXFTElement().getSchema());
    }
	
	public String getPrefix()
	{
	    return this.getParentElement().getGenericXFTElement().getType().getLocalPrefix();
	}
}

