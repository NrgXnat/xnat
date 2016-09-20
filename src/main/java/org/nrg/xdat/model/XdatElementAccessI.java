/*
 * core: org.nrg.xdat.model.XdatElementAccessI
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
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
public interface XdatElementAccessI {

	public String getXSIType();

	public void toXML(java.io.Writer writer) throws java.lang.Exception;

	/**
	 * @return Returns the secondary_password.
	 */
	public String getSecondaryPassword();

	/**
	 * Sets the value for secondary_password.
	 * @param v Value to Set.
	 */
	public void setSecondaryPassword(String v);

	/**
	 * @return Returns the secondary_password/encrypt.
	 */
	public Boolean getSecondaryPassword_encrypt();

	/**
	 * Sets the value for secondary_password/encrypt.
	 * @param v Value to Set.
	 */
	public void setSecondaryPassword_encrypt(Object v);

	/**
	 * secure_ip
	 * @return Returns an List of org.nrg.xdat.model.XdatElementAccessSecureIpI
	 */
	public <A extends org.nrg.xdat.model.XdatElementAccessSecureIpI> List<A> getSecureIp();

	/**
	 * secure_ip
	 * @return Returns an List of org.nrg.xdat.model.XdatElementAccessSecureIpI
	 */
	public <A extends org.nrg.xdat.model.XdatElementAccessSecureIpI> void addSecureIp(A item) throws Exception;

	/**
	 * permissions/allow_set
	 * @return Returns an List of org.nrg.xdat.model.XdatFieldMappingSetI
	 */
	public <A extends org.nrg.xdat.model.XdatFieldMappingSetI> List<A> getPermissions_allowSet();

	/**
	 * permissions/allow_set
	 * @return Returns an List of org.nrg.xdat.model.XdatFieldMappingSetI
	 */
	public <A extends org.nrg.xdat.model.XdatFieldMappingSetI> void addPermissions_allowSet(A item) throws Exception;

	/**
	 * @return Returns the element_name.
	 */
	public String getElementName();

	/**
	 * Sets the value for element_name.
	 * @param v Value to Set.
	 */
	public void setElementName(String v);

	/**
	 * @return Returns the xdat_element_access_id.
	 */
	public Integer getXdatElementAccessId();
}
