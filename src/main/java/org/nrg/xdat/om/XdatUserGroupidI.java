/*
 * org.nrg.xdat.om.XdatUserGroupidI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.om;

/**
 * @author XDAT
 *
 */
public interface XdatUserGroupidI {

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
	 * @return Returns the xdat_user_groupID_id.
	 */
	public Integer getXdatUserGroupidId();

	/**
	 * Sets the value for xdat_user_groupID_id.
	 * @param v Value to Set.
	 */
	public void setXdatUserGroupidId(Integer v);
}
