/*
 * core: org.nrg.xdat.om.XdatStoredSearchAllowedUserI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om;

/**
 * @author XDAT
 *
 */
public interface XdatStoredSearchAllowedUserI {

	public String getSchemaElementName();

	/**
	 * @return Returns the login.
	 */
	public String getLogin();

	/**
	 * Sets the value for login.
	 * @param v Value to Set.
	 */
	public void setLogin(String v);

	/**
	 * @return Returns the xdat_stored_search_allowed_user_id.
	 */
	public Integer getXdatStoredSearchAllowedUserId();

	/**
	 * Sets the value for xdat_stored_search_allowed_user_id.
	 * @param v Value to Set.
	 */
	public void setXdatStoredSearchAllowedUserId(Integer v);
}
