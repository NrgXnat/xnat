/*
 * core: org.nrg.xdat.om.XdatElementSecurityListingActionI
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om;

/**
 * @author XDAT
 *
 */
public interface XdatElementSecurityListingActionI {

	public String getSchemaElementName();

	/**
	 * @return Returns the element_action_name.
	 */
	public String getElementActionName();

	/**
	 * Sets the value for element_action_name.
	 * @param v Value to Set.
	 */
	public void setElementActionName(String v);

	/**
	 * @return Returns the display_name.
	 */
	public String getDisplayName();

	/**
	 * Sets the value for display_name.
	 * @param v Value to Set.
	 */
	public void setDisplayName(String v);

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
	 * @return Returns the image.
	 */
	public String getImage();

	/**
	 * Sets the value for image.
	 * @param v Value to Set.
	 */
	public void setImage(String v);

	/**
	 * @return Returns the popup.
	 */
	public String getPopup();

	/**
	 * Sets the value for popup.
	 * @param v Value to Set.
	 */
	public void setPopup(String v);

	/**
	 * @return Returns the secureAccess.
	 */
	public String getSecureaccess();

	/**
	 * Sets the value for secureAccess.
	 * @param v Value to Set.
	 */
	public void setSecureaccess(String v);

	/**
	 * @return Returns the parameterString.
	 */
	public String getParameterstring();

	/**
	 * Sets the value for parameterString.
	 * @param v Value to Set.
	 */
	public void setParameterstring(String v);

	/**
	 * @return Returns the xdat_element_security_listing_action_id.
	 */
	public Integer getXdatElementSecurityListingActionId();

	/**
	 * Sets the value for xdat_element_security_listing_action_id.
	 * @param v Value to Set.
	 */
	public void setXdatElementSecurityListingActionId(Integer v);
}
