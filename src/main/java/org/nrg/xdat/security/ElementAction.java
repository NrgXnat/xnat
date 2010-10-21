//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 21, 2005
 *
 */
package org.nrg.xdat.security;

import org.nrg.xft.ItemI;
import org.nrg.xft.ItemWrapper;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;

/**
 * @author Tim
 *
 */
@SuppressWarnings("serial")
public class ElementAction extends ItemWrapper {
	public ElementAction()
	{
	}

	public ElementAction(ItemI item)
	{
		setItem(item);
	}
	
	
	public String getName() 
	{
	    try {
            return (String)getProperty("element_action_name");
        } catch (ElementNotFoundException e) {
            return null;
        } catch (FieldNotFoundException e) {
            return null;
        }

	}
	
	public String getSchemaElementName()
	{
	    return "xdat:element_action";
	}
	
	public String getDisplayName() throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		try {
			return (String)getProperty("display_name");
		} catch (FieldEmptyException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean hasImage() throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		try {
			if ( (String)getProperty("image")== null)
			{
			    return false;
			}else{
			    return true;
			}
		} catch (Exception e) {
			return false;
		}
	}
	
	public String getImage() throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		try {
			return (String)getProperty("image");
		} catch (FieldEmptyException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getPopup() throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		try {
			return (String)getProperty("popup");
		} catch (FieldEmptyException e) {
			e.printStackTrace();
			return "never";
		}
	}
	
	public String getSecureAccess() throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		try {
			return (String)getProperty("secureAccess");
		} catch (FieldEmptyException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public String getParameterString() throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		try {
			return (String)getProperty("parameterString");
		} catch (FieldEmptyException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public String getGrouping(){
		try {
			return (String)getProperty("grouping");
		} catch (Exception e) {
			return "";
		}
	}
}

