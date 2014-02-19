/*
 * org.nrg.xdat.om.XdatUserLoginI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.om;
import org.nrg.xft.ItemI;

/**
 * @author XDAT
 *
 */
public interface XdatUserLoginI {

	public String getSchemaElementName();

	/**
	 * @return Returns the login_date.
	 */
	public Object getLoginDate();

	/**
	 * Sets the value for login_date.
	 * @param v Value to Set.
	 */
	public void setLoginDate(Object v);

	/**
	 * @return Returns the ip_address.
	 */
	public String getIpAddress();

	/**
	 * Sets the value for ip_address.
	 * @param v Value to Set.
	 */
	public void setIpAddress(String v);

	/**
	 * user
	 * @return org.nrg.xdat.om.XdatUserI
	 */
	public org.nrg.xdat.om.XdatUserI getuserProperty();

	/**
	 * Sets the value for user.
	 * @param v Value to Set.
	 */
	public void setuserProperty(ItemI v) throws Exception;

	/**
	 * @return Returns the xdat:user_login/user_xdat_user_id.
	 */
	public Integer getuserPropertyFK();

	/**
	 * Sets the value for xdat:user_login/user_xdat_user_id.
	 * @param v Value to Set.
	 */
	public void setuserPropertyFK(Integer v);

	/**
	 * @return Returns the xdat_user_login_id.
	 */
	public Integer getXdatUserLoginId();

	/**
	 * Sets the value for xdat_user_login_id.
	 * @param v Value to Set.
	 */
	public void setXdatUserLoginId(Integer v);
}
