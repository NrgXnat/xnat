/*
 * core: org.nrg.xdat.model.XdatSearchI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*
 * GENERATED FILE
 * Created on Thu Mar 31 18:38:30 CDT 2016
 *
 */
package org.nrg.xdat.model;

import java.util.List;

/**
 * @author XDAT
 *
 */
public interface XdatSearchI extends XdatStoredSearchI {

	public String getXSIType();

	public void toXML(java.io.Writer writer) throws java.lang.Exception;

	/**
	 * @return Returns the page.
	 */
	public Integer getPage();

	/**
	 * Sets the value for xdat:Search/page.
	 * @param v Value to Set.
	 */
	public void setPage(Integer v) ;
}
