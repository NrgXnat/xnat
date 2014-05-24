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
public class ViewLink {
	private Mapping mapping = null;
	private String alias = "";
	/**
	 * @return
	 */
	public Mapping getMapping() {
		return mapping;
	}

	/**
	 * @param mapping
	 */
	public void setMapping(Mapping mapping) {
		this.mapping = mapping;
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

}

