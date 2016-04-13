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
public interface XdatStoredSearchGroupidI {

	public String getXSIType();

	public void toXML(java.io.Writer writer) throws java.lang.Exception;

	/**
	 * @return Returns the groupID.
	 */
	public String getGroupid();

	/**
	 * Sets the value for groupID.
	 * @param v Value to Set.
	 */
	public void setGroupid(String v);

	/**
	 * @return Returns the xdat_stored_search_groupID_id.
	 */
	public Integer getXdatStoredSearchGroupidId();
}
