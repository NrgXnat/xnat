/*
 * core: org.nrg.xdat.om.XdatAccessLogI
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
public interface XdatAccessLogI {

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
	 * @return Returns the access_date.
	 */
	public Object getAccessDate();

	/**
	 * Sets the value for access_date.
	 * @param v Value to Set.
	 */
	public void setAccessDate(Object v);

	/**
	 * @return Returns the ip.
	 */
	public String getIp();

	/**
	 * Sets the value for ip.
	 * @param v Value to Set.
	 */
	public void setIp(String v);

	/**
	 * @return Returns the method.
	 */
	public String getMethod();

	/**
	 * Sets the value for method.
	 * @param v Value to Set.
	 */
	public void setMethod(String v);

	/**
	 * @return Returns the xdat_access_log_id.
	 */
	public Integer getXdatAccessLogId();

	/**
	 * Sets the value for xdat_access_log_id.
	 * @param v Value to Set.
	 */
	public void setXdatAccessLogId(Integer v);
}
