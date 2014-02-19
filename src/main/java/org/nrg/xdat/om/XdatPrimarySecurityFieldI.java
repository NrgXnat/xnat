/*
 * org.nrg.xdat.om.XdatPrimarySecurityFieldI
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
public interface XdatPrimarySecurityFieldI {

	public String getSchemaElementName();

	/**
	 * @return Returns the primary_security_field.
	 */
	public String getPrimarySecurityField();

	/**
	 * Sets the value for primary_security_field.
	 * @param v Value to Set.
	 */
	public void setPrimarySecurityField(String v);

	/**
	 * @return Returns the xdat_primary_security_field_id.
	 */
	public Integer getXdatPrimarySecurityFieldId();

	/**
	 * Sets the value for xdat_primary_security_field_id.
	 * @param v Value to Set.
	 */
	public void setXdatPrimarySecurityFieldId(Integer v);
}
