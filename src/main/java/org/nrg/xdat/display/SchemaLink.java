//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 3, 2005
 *
 */
package org.nrg.xdat.display;

/**
 * @author Tim
 *
 */
public class SchemaLink extends ViewLink{
	private String rootElement = "";
	private String element = "";
	private String type = "";
	private String alias = "";
	
	public SchemaLink(String root)
	{
		rootElement = root;
	}
	
	/**
	 * @return
	 */
	public String getElement() {
		return element;
	}

	/**
	 * @return
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param string
	 */
	public void setElement(String string) {
		element = string;
	}

	/**
	 * @param string
	 */
	public void setType(String string) {
		type = string;
	}

	/**
	 * @return
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * @param string
	 */
	public void setAlias(String string) {
		alias = string;
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
	public void setRootElement(String string) {
		rootElement = string;
	}

}

