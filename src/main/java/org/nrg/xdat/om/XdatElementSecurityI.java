/*
 * org.nrg.xdat.om.XdatElementSecurityI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.om;
import java.util.ArrayList;

import org.nrg.xft.ItemI;

/**
 * @author XDAT
 *
 */
public interface XdatElementSecurityI {

	public String getSchemaElementName();

	/**
	 * primary_security_fields/primary_security_field
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatPrimarySecurityFieldI
	 */
	public ArrayList getPrimarySecurityFields_primarySecurityField();

	/**
	 * Sets the value for primary_security_fields/primary_security_field.
	 * @param v Value to Set.
	 */
	public void setPrimarySecurityFields_primarySecurityField(ItemI v) throws Exception;

	/**
	 * element_actions/element_action
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatElementActionTypeI
	 */
	public ArrayList getElementActions_elementAction();

	/**
	 * Sets the value for element_actions/element_action.
	 * @param v Value to Set.
	 */
	public void setElementActions_elementAction(ItemI v) throws Exception;

	/**
	 * listing_actions/listing_action
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatElementSecurityListingActionI
	 */
	public ArrayList getListingActions_listingAction();

	/**
	 * Sets the value for listing_actions/listing_action.
	 * @param v Value to Set.
	 */
	public void setListingActions_listingAction(ItemI v) throws Exception;

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
	 * Sets the value for sequence.
	 * @param v Value to Set.
	 */
	public void setSequence(Integer v);

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
}
