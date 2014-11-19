/*
 * org.nrg.xft.schema.design.XFTElementWrapper
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.schema.design;
import java.util.ArrayList;
import java.util.Iterator;

import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.meta.XFTMetaManager;
import org.nrg.xft.schema.XFTElement;
import org.nrg.xft.schema.XFTField;
import org.nrg.xft.schema.XMLType;
public abstract class XFTElementWrapper implements SchemaElementI {
	private ArrayList attributes = null;
	private ArrayList childElements = null;
	private Boolean hasAttributes = null;
	private Boolean hasChildElements = null;

	private String label = "";
	protected XFTElement wrapped = null;
	private String fullXMLName = null;

	public abstract  XFTFactoryI getFactory();
	
	/**
	 * Gets the name of the wrapped element
	 * @return String name of wrapped element
	 */
	public String getDirectXMLName() {
		return wrapped.getName();
	}
	
	/**
	 * Gets the localType of the Wrapped element's XMLType
	 * @return
	 * @see XMLType
	 */
	public String getLocalXMLName()
	{
		return wrapped.getType().getLocalType();
	}
	
	/**
	 * Gets the fullLocalType of the Wrapped element's XMLType
	 * @return
	 * @see XMLType
	 */
	public String getFullXMLName()
	{
	    if (fullXMLName==null)
	    {
	        fullXMLName = wrapped.getType().getFullLocalType();
	    }
		return fullXMLName;
	}
	
	public String getXSIType()
	{
	    return getFullXMLName();
	}
	
	/**
	 * Gets wrapped element
	 * @return XFTElement
	 */
	public XFTElement getWrapped() {
	    try {
            wrapped = XFTMetaManager.FindElement(wrapped.getType());
        } catch (XFTInitException e) {
        } catch (ElementNotFoundException e) {
        }
        return wrapped;
	}


	/**
	 * Iterates through sortedFields and returns a wrapped version of the XFTField with the matching name.
	 * @param name of wrapped field to return
	 * @return wrapped XFTField
	 */
	public XFTFieldWrapper getWrappedField(String name) {
		Iterator iter = wrapped.getSortedFields().iterator();
		while (iter.hasNext()) {
			XFTField xf = (XFTField) iter.next();
			if (xf.getName().equalsIgnoreCase(name)) {
				return getFactory().wrapField(xf);
			}
		}
		return null;
	}

	/**
	 * Wrap the given element.
	 * @param element XFTElement to wrap
	 */
	public void setWrapped(XFTElement element) {
		wrapped = element;
		if (wrapped != null)
			label = wrapped.getName();
	}
}

