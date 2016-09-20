/*
 * core: org.nrg.xdat.model.XdatElementSecurityI
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
public interface XdatElementSecurityI {

	public String getXSIType();

	public void toXML(java.io.Writer writer) throws java.lang.Exception;

	/**
	 * primary_security_fields/primary_security_field
	 * @return Returns an List of org.nrg.xdat.model.XdatPrimarySecurityFieldI
	 */
	public <A extends org.nrg.xdat.model.XdatPrimarySecurityFieldI> List<A> getPrimarySecurityFields_primarySecurityField();

	/**
	 * primary_security_fields/primary_security_field
	 * @return Returns an List of org.nrg.xdat.model.XdatPrimarySecurityFieldI
	 */
	public <A extends org.nrg.xdat.model.XdatPrimarySecurityFieldI> void addPrimarySecurityFields_primarySecurityField(A item) throws Exception;

	/**
	 * element_actions/element_action
	 * @return Returns an List of org.nrg.xdat.model.XdatElementActionTypeI
	 */
	public <A extends org.nrg.xdat.model.XdatElementActionTypeI> List<A> getElementActions_elementAction();

	/**
	 * element_actions/element_action
	 * @return Returns an List of org.nrg.xdat.model.XdatElementActionTypeI
	 */
	public <A extends org.nrg.xdat.model.XdatElementActionTypeI> void addElementActions_elementAction(A item) throws Exception;

	/**
	 * listing_actions/listing_action
	 * @return Returns an List of org.nrg.xdat.model.XdatElementSecurityListingActionI
	 */
	public <A extends org.nrg.xdat.model.XdatElementSecurityListingActionI> List<A> getListingActions_listingAction();

	/**
	 * listing_actions/listing_action
	 * @return Returns an List of org.nrg.xdat.model.XdatElementSecurityListingActionI
	 */
	public <A extends org.nrg.xdat.model.XdatElementSecurityListingActionI> void addListingActions_listingAction(A item) throws Exception;

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
	 * @return Returns the secondary_password.
	 */
	public Boolean getSecondaryPassword();

	/**
	 * Sets the value for secondary_password.
	 * @param v Value to Set.
	 */
	public void setSecondaryPassword(Object v);

	/**
	 * @return Returns the secure_ip.
	 */
	public Boolean getSecureIp();

	/**
	 * Sets the value for secure_ip.
	 * @param v Value to Set.
	 */
	public void setSecureIp(Object v);

	/**
	 * @return Returns the secure.
	 */
	public Boolean getSecure();

	/**
	 * Sets the value for secure.
	 * @param v Value to Set.
	 */
	public void setSecure(Object v);

	/**
	 * @return Returns the browse.
	 */
	public Boolean getBrowse();

	/**
	 * Sets the value for browse.
	 * @param v Value to Set.
	 */
	public void setBrowse(Object v);

	/**
	 * @return Returns the sequence.
	 */
	public Integer getSequence();

	/**
	 * Sets the value for xdat:element_security/sequence.
	 * @param v Value to Set.
	 */
	public void setSequence(Integer v) ;

	/**
	 * @return Returns the quarantine.
	 */
	public Boolean getQuarantine();

	/**
	 * Sets the value for quarantine.
	 * @param v Value to Set.
	 */
	public void setQuarantine(Object v);

	/**
	 * @return Returns the pre_load.
	 */
	public Boolean getPreLoad();

	/**
	 * Sets the value for pre_load.
	 * @param v Value to Set.
	 */
	public void setPreLoad(Object v);

	/**
	 * @return Returns the searchable.
	 */
	public Boolean getSearchable();

	/**
	 * Sets the value for searchable.
	 * @param v Value to Set.
	 */
	public void setSearchable(Object v);

	/**
	 * @return Returns the secure_read.
	 */
	public Boolean getSecureRead();

	/**
	 * Sets the value for secure_read.
	 * @param v Value to Set.
	 */
	public void setSecureRead(Object v);

	/**
	 * @return Returns the secure_edit.
	 */
	public Boolean getSecureEdit();

	/**
	 * Sets the value for secure_edit.
	 * @param v Value to Set.
	 */
	public void setSecureEdit(Object v);

	/**
	 * @return Returns the secure_create.
	 */
	public Boolean getSecureCreate();

	/**
	 * Sets the value for secure_create.
	 * @param v Value to Set.
	 */
	public void setSecureCreate(Object v);

	/**
	 * @return Returns the secure_delete.
	 */
	public Boolean getSecureDelete();

	/**
	 * Sets the value for secure_delete.
	 * @param v Value to Set.
	 */
	public void setSecureDelete(Object v);

	/**
	 * @return Returns the accessible.
	 */
	public Boolean getAccessible();

	/**
	 * Sets the value for accessible.
	 * @param v Value to Set.
	 */
	public void setAccessible(Object v);

	/**
	 * @return Returns the usage.
	 */
	public String getUsage();

	/**
	 * Sets the value for usage.
	 * @param v Value to Set.
	 */
	public void setUsage(String v);

	/**
	 * @return Returns the singular.
	 */
	public String getSingular();

	/**
	 * Sets the value for singular.
	 * @param v Value to Set.
	 */
	public void setSingular(String v);

	/**
	 * @return Returns the plural.
	 */
	public String getPlural();

	/**
	 * Sets the value for plural.
	 * @param v Value to Set.
	 */
	public void setPlural(String v);

	/**
	 * @return Returns the category.
	 */
	public String getCategory();

	/**
	 * Sets the value for category.
	 * @param v Value to Set.
	 */
	public void setCategory(String v);

	/**
	 * @return Returns the code.
	 */
	public String getCode();

	/**
	 * Sets the value for code.
	 * @param v Value to Set.
	 */
	public void setCode(String v);
}
