/*
 * core: org.nrg.xdat.model.XdatElementAccessSecureIpI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

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
public interface XdatElementAccessSecureIpI {

	public String getXSIType();

	public void toXML(java.io.Writer writer) throws java.lang.Exception;

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
}
