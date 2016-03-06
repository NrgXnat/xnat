/*
 * org.nrg.xdat.om.XdatElementAccessI
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
 */
public interface XdatElementAccessI {

    /**
     * Gets the schema element name.
     *
     * @return The schema element name.
     */
    String getSchemaElementName();

    /**
     * @return Returns the secondary_password.
     */
    @SuppressWarnings("unused")
    String getSecondaryPassword();

    /**
     * Sets the value for secondary_password.
     *
     * @param v Value to Set.
     */
    @SuppressWarnings("unused")
    void setSecondaryPassword(String v);

    /**
     * @return Returns the secondary_password/encrypt.
     */
    @SuppressWarnings("unused")
    Boolean getSecondaryPassword_encrypt();

    /**
     * Sets the value for secondary_password/encrypt.
     *
     * @param v Value to Set.
     */
    @SuppressWarnings("unused")
    void setSecondaryPassword_encrypt(Object v);

    /**
     * secure_ip
     *
     * @return Returns an ArrayList of org.nrg.xdat.om.XdatElementAccessSecureIpI
     */
    ArrayList getSecureIp();

    /**
     * Sets the value for secure_ip.
     *
     * @param v Value to Set.
     * @throws Exception When an error occurs.
     */
    @SuppressWarnings("unused")
    void setSecureIp(ItemI v) throws Exception;

    /**
     * permissions/allow_set
     *
     * @return Returns an ArrayList of org.nrg.xdat.om.XdatFieldMappingSetI
     */
    ArrayList getPermissions_allowSet();

    /**
     * Sets the value for permissions/allow_set.
     *
     * @param v Value to Set.
     * @throws Exception When an error occurs.
     */
    void setPermissions_allowSet(ItemI v) throws Exception;

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
     * @return Returns the xdat_element_access_id.
     */
    Integer getXdatElementAccessId();

    /**
     * Sets the value for xdat_element_access_id.
     *
     * @param v Value to Set.
     */
    @SuppressWarnings("unused")
    void setXdatElementAccessId(Integer v);
}
