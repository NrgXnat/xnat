/*
 * org.nrg.xdat.display.ViewLink
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
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

