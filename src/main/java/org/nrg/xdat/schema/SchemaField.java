/*
 * org.nrg.xdat.schema.SchemaField
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.schema;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.identifier.Identifier;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.schema.design.SchemaFieldI;
import org.nrg.xft.utils.ValidationUtils.XFTValidator;

/**
 * @author Tim
 *
 */
public class SchemaField implements Identifier, SchemaFieldI{
	static org.apache.log4j.Logger logger = Logger.getLogger(SchemaField.class);
	GenericWrapperField wrapped = null;
	Hashtable possibleValues = null;
	
	public SchemaField(GenericWrapperField f)
	{
		wrapped = f;
	}
	
	public SchemaField(String xmlPath) throws ElementNotFoundException,FieldNotFoundException
	{
	    try {
            GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(xmlPath);
            wrapped = f;
        } catch (XFTInitException e) {
            logger.error("",e);
        }
	}
	
	public GenericWrapperField getWrapped()
	{
		return wrapped;
	}
	
	public String getId()
	{
		return wrapped.getId();
	}
	
	public String getName()
	{
		return wrapped.getName();
	}
	
	public String getSQLName()
	{
		return wrapped.getSQLName();
	}

	public String getXMLPathString(String elementName)
	{
	    return wrapped.getXMLPathString(elementName);
	}
	
	public boolean isReference()
	{
		return wrapped.isReference();
	}
	
	public boolean hasPossibleValues(String login) throws Exception
	{
		if (getPossibleValues(login).size() > 0)
		{
			return true;
		}else
		{
			return false;
		}
	}
	
	public SchemaElementI getParentElement()
	{
		return new SchemaElement((wrapped.getParentElement().getGenericXFTElement()));
	}
	
	public SchemaElementI getReferenceElement() throws XFTInitException,ElementNotFoundException
	{
		try {
			return new SchemaElement(((SchemaElementI)wrapped.getReferenceElement()).getGenericXFTElement());
		} catch (XFTInitException e) {
			return null;
		}
	}
	public Hashtable getPossibleValues(String login)throws Exception
	{
		if (possibleValues == null)
		{
			possibleValues = new Hashtable();
			
			if (isReference())
			{
				possibleValues = ((SchemaElement)getReferenceElement()).getDistinctIdentifierValues(login);
			}else{
				try {
					ArrayList al = XFTValidator.GetPossibleValues(wrapped.getParentElement().getFullXMLName(),wrapped.getSQLName());
					if (al.size() > 0)
					{
						Iterator iter = al.iterator();
						while (iter.hasNext())
						{
							String o = (String)iter.next();
							possibleValues.put(o,o);
						}
					}
				} catch (XFTInitException e) {
					logger.error("",e);
				} catch (Exception e) {
					logger.error("",e);
				}
			}
		}
		
		return possibleValues;
	}
	
	public boolean isBooleanField()
	{
		if (isReference())
		{
			return false;
		}else{
			if (wrapped.getXMLType().getLocalType().equalsIgnoreCase("boolean"))
			{
				return true;
			}else{
				return false;
			}
		}
	}
	
	public GenericWrapperField getGenericXFTField(){
	    return this.wrapped;
	}
	
}

