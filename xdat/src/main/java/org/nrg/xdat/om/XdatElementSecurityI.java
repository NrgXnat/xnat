/*
 * core: org.nrg.xdat.om.XdatElementSecurityI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.om;

import java.util.ArrayList;

import org.nrg.xft.ItemI;

/**
 * @author XDAT
 */
public interface XdatElementSecurityI {

    /**
     * Gets the schema element name.
     *
     * @return The schema element name.
     */
    String getSchemaElementName();

    /**
     * primary_security_fields/primary_security_field
     *
     * @return Returns an ArrayList of org.nrg.xdat.om.XdatPrimarySecurityFieldI
     */
    ArrayList getPrimarySecurityFields_primarySecurityField();

    /**
     * Sets the value for primary_security_fields/primary_security_field.
     *
     * @param v Value to Set.
     * @throws Exception When an error occurs.
     */
    @SuppressWarnings("unused")
    void setPrimarySecurityFields_primarySecurityField(ItemI v) throws Exception;

    /**
     * element_actions/element_action
     *
     * @return Returns an ArrayList of org.nrg.xdat.om.XdatElementActionTypeI
     */
    ArrayList getElementActions_elementAction();

    /**
     * Sets the value for element_actions/element_action.
     *
     * @param v Value to Set.
     * @throws Exception When an error occurs.
     */
    @SuppressWarnings("unused")
    void setElementActions_elementAction(ItemI v) throws Exception;

    /**
     * listing_actions/listing_action
     *
     * @return Returns an ArrayList of org.nrg.xdat.om.XdatElementSecurityListingActionI
     */
    ArrayList getListingActions_listingAction();

    /**
     * Sets the value for listing_actions/listing_action.
     *
     * @param v Value to Set.
     * @throws Exception When an error occurs.
     */
    @SuppressWarnings("unused")
    void setListingActions_listingAction(ItemI v) throws Exception;

    /**
     * @return Returns the element_name.
     */
    String getElementName();

    /**
     * Sets the value for element_name.
     *
     * @param v Value to Set.
     */
    void setElementName(String v);

    /**
     * @return Returns the secondary_password.
     */
    @SuppressWarnings("unused")
    Boolean getSecondaryPassword();

    /**
     * Sets the value for secondary_password.
     *
     * @param v Value to Set.
     */
    @SuppressWarnings("unused")
    void setSecondaryPassword(Object v);

    /**
     * @return Returns the secure_ip.
     */
    @SuppressWarnings("unused")
    Boolean getSecureIp();

    /**
     * Sets the value for secure_ip.
     *
     * @param v Value to Set.
     */
    @SuppressWarnings("unused")
    void setSecureIp(Object v);

    /**
     * @return Returns the secure.
     */
    Boolean getSecure();

    /**
     * Sets the value for secure.
     *
     * @param v Value to Set.
     */
    void setSecure(Object v);

    /**
     * @return Returns the browse.
     */
    Boolean getBrowse();

    /**
     * Sets the value for browse.
     *
     * @param v Value to Set.
     */
    void setBrowse(Object v);

    /**
     * @return Returns the sequence.
     */
    Integer getSequence();

    /**
     * Sets the value for sequence.
     *
     * @param v Value to Set.
     */
    void setSequence(Integer v);

    /**
     * @return Returns the quarantine.
     */
    Boolean getQuarantine();

    /**
     * Sets the value for quarantine.
     *
     * @param v Value to Set.
     */
    void setQuarantine(Object v);

    /**
     * @return Returns the pre_load.
     */
    Boolean getPreLoad();

    /**
     * Sets the value for pre_load.
     *
     * @param v Value to Set.
     */
    void setPreLoad(Object v);
}
