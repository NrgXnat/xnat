//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Mar 26, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.nrg.xft.schema.design;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.nrg.xft.identifier.Identifier;
import org.nrg.xft.schema.XFTDataField;
import org.nrg.xft.schema.XFTField;
import org.nrg.xft.utils.StringUtils;
/**
 * Generic wrapper for access to XFTFields
 * 
 * @author Tim
 */
public abstract class XFTFieldWrapper implements Identifier{
	static org.apache.log4j.Logger logger = Logger.getLogger(XFTFieldWrapper.class);
	protected XFTField wrapped = null;
	
	private ArrayList childElements = null;
	private Boolean hasChildElements = null;
	private ArrayList attributes = null;
	private Boolean hasAttributes = null;
	private Boolean isRef = null;

	private String label = "";
	/**
	 * Gets factory for the defined wrapper.
	 * @return XFTFactoryI
	 */
	public abstract XFTFactoryI getFactory();
	
	/**
	 * Set wrapped field.
	 * @param xe
	 */
	public void loadElement(XFTField xe)
	{
		wrapped = xe;
		label = StringUtils.intern(xe.getFullName() + " -> " + xe.getXMLType());
	}
	/**
	 * Get wrapped field
	 * @return
	 */
	public XFTField getWrapped() {
		return wrapped;
	}

	/**
	 * Set wrapped field
	 * @param element
	 */
	public void setWrapped(XFTField element) {
		label = StringUtils.intern(element.getFullName() + " -> " + element.getXMLType());
		wrapped = element;
	}
	
	/**
	 * Specifies if the current wrapped element is of type org.nrg.xft.schema.XFTDataField
	 * @return
	 */
	public boolean isReference()
	{
		if (isRef == null)
		{
			if (wrapped instanceof XFTDataField)
			{
				isRef = Boolean.FALSE;
			}else
			{
				isRef = Boolean.TRUE;
			}
		}
	
		return isRef.booleanValue();
	}

	/**
	 * If maxOccurs is unbounded or > 1
	 * @return
	 */
	public boolean isMultiple()
	{
		String s = wrapped.getMaxOccurs();
		if (s.equalsIgnoreCase("unbounded"))
		{
			return true;
		}else if (s != "") 
		{
			try {
				int i = Integer.valueOf(s).intValue();
				if (i>1)
				{
					return true;
				}
			} catch (Exception e) {
				logger.error("'" + this.getParentE().getFullXMLName() + "' -> '" + this.getName() + "'",e);
			}
			return false;
		}else
		{
			return false;
		}
	}

	/**
	 * @return
	 */
	protected XFTElementWrapper getParentE() throws ClassCastException
	{
		return getFactory().wrapElement(this.getWrapped().getParentElement());
	}
	
	/**
	 * Get name from wrapped field.
	 * @return
	 */
	public String getName()
	{
		return wrapped.getName();
	}
	
	/**
	 * Checks if the item is shown in XML version only (i.e. output only)
	 * @return
	 */
	public boolean isHidden()
	{
		if(wrapped.getXmlOnly().equalsIgnoreCase("true"))
		{
			return true;
		}else
		{
			return false;
		}
	}
	
	public abstract String getXPATH();

}

