// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Mon Feb 26 14:11:52 CST 2007
 *
 */
package org.nrg.xdat.om;
import org.nrg.xft.*;
import org.nrg.xft.security.UserI;
import org.nrg.xdat.om.*;

import java.util.*;

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
