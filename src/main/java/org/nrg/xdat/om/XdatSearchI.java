/*
 * core: org.nrg.xdat.om.XdatSearchI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.om;

import org.nrg.xft.ItemI;

/**
 * @author XDAT
 *
 */
public interface XdatSearchI extends XdatStoredSearchI {

	/**
	 * Gets the schema element name.
	 *
	 * @return The schema element name.
	 */
	String getSchemaElementName();

	/**
	 * stored_search
	 * @return XdatStoredSearchI
	 */
	XdatStoredSearchI getStoredSearch();

	/**
	 * Sets the value for stored_search.
	 * @param v Value to Set.
     * @throws Exception When an error occurs.
	 */
    @SuppressWarnings("unused")
	void setStoredSearch(ItemI v) throws Exception;

	/**
	 * @return Returns the page.
	 */
	Integer getPage();

	/**
	 * Sets the value for page.
	 * @param v Value to Set.
	 */
	void setPage(Integer v);
}
