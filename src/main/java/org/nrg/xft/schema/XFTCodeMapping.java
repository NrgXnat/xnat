/*
 * core: org.nrg.xft.schema.XFTCodeMapping
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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
	 * @return  Returns the brief description String
	 */
	public String getBriefDescription() {
		return briefDescription;
	}

	/**
	 * @return  Returns the code String
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return Returns the full description String
	 */
	public String getFullDescription() {
		return fullDescription;
	}

	/**
	 * @return Returns the log location String
	 */
	public String getLogLocation() {
		return logLocation;
	}

	/**
	 * @return Returns the model element String
	 */
	public String getModelElement() {
		return modelElement;
	}

	/**
	 * @return Returns the model ID String
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

