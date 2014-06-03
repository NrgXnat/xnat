/*
 * org.nrg.xdat.om.XdatElementAccessSecureIpI
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
public interface XdatElementAccessSecureIpI {

	public String getSchemaElementName();

	/**
	 * @return Returns the secure_ip.
	 */
	public String getSecureIp();

	/**
	 * Sets the value for secure_ip.
	 * @param v Value to Set.
	 */
	public void setSecureIp(String v);

	/**
	 * @return Returns the xdat_element_access_secure_ip_id.
	 */
	public Integer getXdatElementAccessSecureIpId();

	/**
	 * Sets the value for xdat_element_access_secure_ip_id.
	 * @param v Value to Set.
	 */
	public void setXdatElementAccessSecureIpId(Integer v);
}
