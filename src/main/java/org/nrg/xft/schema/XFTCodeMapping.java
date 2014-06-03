/*
 * org.nrg.xft.schema.XFTCodeMapping
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.schema;

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

