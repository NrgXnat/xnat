/*
 * core: org.nrg.xdat.om.XdatUserLoginI
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
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
public interface XdatUserLoginI {

	/**
	 * Gets the schema element name.
	 * @return The schema element name
     */
	String getSchemaElementName();

	/**
	 * @return Returns the login_date.
	 */
    @SuppressWarnings("unused")
    Object getLoginDate();

	/**
	 * Sets the value for login_date.
	 * @param v Value to Set.
	 */
    @SuppressWarnings("unused")
    void setLoginDate(Object v);

	/**
	 * @return Returns the ip_address.
	 */
    @SuppressWarnings("unused")
    String getIpAddress();

	/**
	 * Sets the value for ip_address.
	 * @param v Value to Set.
	 */
    @SuppressWarnings("unused")
    void setIpAddress(String v);

	/**
	 * user
	 * @return XdatUserI
	 */
	XdatUserI getUserProperty();

	/**
	 * Sets the value for user.
	 * @param v Value to Set.
     * @throws Exception When an error occurs.
	 */
    @SuppressWarnings("unused")
    void setuserProperty(ItemI v) throws Exception;

	/**
	 * @return Returns the xdat:user_login/user_xdat_user_id.
	 */
    @SuppressWarnings("unused")
    Integer getUserPropertyFK();

	/**
	 * Sets the value for xdat:user_login/user_xdat_user_id.
	 * @param v Value to Set.
	 */
    @SuppressWarnings("unused")
    void setuserPropertyFK(Integer v);

	/**
	 * @return Returns the xdat_user_login_id.
	 */
    @SuppressWarnings("unused")
    Integer getXdatUserLoginId();

	/**
	 * Sets the value for xdat_user_login_id.
	 * @param v Value to Set.
	 */
    @SuppressWarnings("unused")
    void setXdatUserLoginId(Integer v);
}
