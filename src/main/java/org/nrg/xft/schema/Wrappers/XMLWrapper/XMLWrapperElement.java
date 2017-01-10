/*
 * core: org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWrapperElement
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.schema.Wrappers.XMLWrapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
@SuppressWarnings({"unchecked","rawtypes"})
public class XMLWrapperElement extends GenericWrapperElement implements XMLNode {

	public org.nrg.xft.schema.design.XFTFactoryI getFactory()
	{
		return XMLWrapperFactory.GetInstance();
	}

	private ArrayList children=null;
	
	/**
	* Includes all child nodes where the field's expose is true and it is not an attribute.
	* @return ArrayList of XMLWrapperField
	*/
	public ArrayList getChildren()
	{
	    if (children ==null)
	    {
	        children= new ArrayList();
			
			Iterator iter = this.getDirectFieldsNoFilter().iterator();
			while (iter.hasNext())
			{
				XMLWrapperField child = (XMLWrapperField) iter.next();
				if((child.getExpose()) && (!child.isAttribute()))
				{
				    children.add(child);
				}
			}
			
			children.trimToSize();
			return children;
	    }
	    return children;
	}

	private ArrayList attributes=null;
	/**
	* Includes all child nodes where the field's expose is true and it is an attribute.
	* @return ArrayList of XMLWrapperField
	*/
	public ArrayList getAttributes()
	{
	    if (attributes==null)
	    {
	        attributes=new ArrayList();
			
			Iterator iter = this.getDirectFields().iterator();
			while (iter.hasNext())
			{
				XMLWrapperField child = (XMLWrapperField) iter.next();
				if((child.getExpose()) && (child.isAttribute()))
				{
				    attributes.add(child);
				}
			}
			
			attributes.trimToSize();
			return attributes;
	    }
		return attributes;
	}
	

	
	/**
	 * Returns the sql names of the pk fields for this item.
	 * @return ArrayList of strings
	 */
	public List<String> getPkNames() throws org.nrg.xft.exception.XFTInitException
	{
		List<String> keyNames = new ArrayList<String>();

		Iterator keys = getAllPrimaryKeys().iterator();

		while (keys.hasNext())
		{
			keyNames.add(((GenericWrapperField)keys.next()).getName());
		}

		return keyNames;
	}



}

