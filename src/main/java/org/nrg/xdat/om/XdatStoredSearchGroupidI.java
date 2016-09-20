/*
 * core: org.nrg.xdat.om.XdatStoredSearchGroupidI
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om;

/**
 * @author XDAT
 *
 */
public interface XdatStoredSearchGroupidI {

	public String getSchemaElementName();

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

	/**
	 * Sets the value for xdat_stored_search_groupID_id.
	 * @param v Value to Set.
	 */
	public void setXdatStoredSearchGroupidId(Integer v);
}
