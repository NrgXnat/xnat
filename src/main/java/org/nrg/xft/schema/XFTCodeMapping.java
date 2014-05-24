//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Oct 11, 2004
 */
package org.nrg.xft.schema;

/**
 * This class is used to map abbreviated type codes to specific elements.
 * 
 * <BR><BR>This class is used to track the connection between a model element and its code.
 * It also includes description information about the element.  The values for this object 
 * are stored in the InstanceSettings.xml as &#60;Type_Code_Mapping&#62; elements.  
 * 
 * This element is used in the legacy version of XDAT.
 * 
 * @author Tim
 */
public class XFTCodeMapping {
	private String modelId = "";
	private String modelElement = "";
	private String code = "";
	private String briefDescription = "";
	private String fullDescription = "";
	private String logLocation = "";
	/**
	 * @return
	 */
	public String getBriefDescription() {
		return briefDescription;
	}

	/**
	 * @return
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return
	 */
	public String getFullDescription() {
		return fullDescription;
	}

	/**
	 * @return
	 */
	public String getLogLocation() {
		return logLocation;
	}

	/**
	 * @return
	 */
	public String getModelElement() {
		return modelElement;
	}

	/**
	 * @return
	 */
	public String getModelId() {
		return modelId;
	}

	/**
	 * @param string
	 */
	public void setBriefDescription(String string) {
		briefDescription = string;
	}

	/**
	 * @param string
	 */
	public void setCode(String string) {
		code = string;
	}

	/**
	 * @param string
	 */
	public void setFullDescription(String string) {
		fullDescription = string;
	}

	/**
	 * @param string
	 */
	public void setLogLocation(String string) {
		logLocation = string;
	}

	/**
	 * @param string
	 */
	public void setModelElement(String string) {
		modelElement = string;
	}

	/**
	 * @param string
	 */
	public void setModelId(String string) {
		modelId = string;
	}

}

