//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Mar 26, 2004
 */
package org.nrg.xft.schema.Wrappers.XMLWrapper;

import java.util.ArrayList;
import java.util.Iterator;

import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
/**
 * Alternative version of the GenericWrapperElement used to summarize the element
 * from an XML perspective.
 * 
 * @author Tim
 */
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
}

