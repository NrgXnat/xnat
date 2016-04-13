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
public interface XdatStoredSearchAllowedUserI {

	public String getXSIType();

	public void toXML(java.io.Writer writer) throws java.lang.Exception;

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
}
