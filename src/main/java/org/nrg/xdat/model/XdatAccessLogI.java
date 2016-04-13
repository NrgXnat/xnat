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
public interface XdatAccessLogI {

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
}
