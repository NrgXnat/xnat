//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 3, 2005
 *
 */
package org.nrg.xdat.display;

import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.utils.StringUtils;

/**
 * @author Tim
 *
 */
public class MappingColumn {
	private String rootElement = "";
	private String fieldElementXMLPath = "";
	private String fieldElementFullSQL = null;
	private String mapsTo = "";
	/**
	 * @return
	 */
	public String getFieldElementXMLPath() {
		return fieldElementXMLPath;
	}

	/**
	 * @return
	 */
	public String getFieldElementFullSQL() throws XFTInitException,ElementNotFoundException,Exception {
		if (fieldElementFullSQL == null)
		{
			String rootElement = StringUtils.GetRootElementName(fieldElementXMLPath);
			GenericWrapperElement root = GenericWrapperElement.GetElement(rootElement);
			
			fieldElementFullSQL= ViewManager.GetViewColumnName(root,fieldElementXMLPath,ViewManager.DEFAULT_LEVEL,false,true);
		}
		return fieldElementFullSQL;
	}

	/**
	 * @return
	 */
	public String getMapsTo() {
		return mapsTo;
	}

	/**
	 * @return
	 */
	public String getRootElement() {
		return rootElement;
	}

	/**
	 * @param string
	 */
	public void setFieldElementXMLPath(String string) {
		fieldElementXMLPath = string;
	}

	/**
	 * @param string
	 */
	public void setMapsTo(String string) {
		mapsTo = string;
	}

	/**
	 * @param string
	 */
	public void setRootElement(String string) {
		rootElement = string;
	}

}

