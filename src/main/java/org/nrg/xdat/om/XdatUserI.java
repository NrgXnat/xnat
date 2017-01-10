/*
 * core: org.nrg.xdat.om.XdatUserI
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
public interface XdatUserI {

    String getSchemaElementName();

    /**
     * @return Returns the login.
     */
    String getLogin();

    /**
     * Sets the value for login.
     *
     * @param v Value to Set.
     */
    void setLogin(String v);

    /**
     * @return Returns the firstname.
     */
    String getFirstname();

    /**
     * Sets the value for firstname.
     *
     * @param v Value to Set.
     */
    void setFirstname(String v);

    /**
     * @return Returns the lastname.
     */
    String getLastname();

    /**
     * Sets the value for lastname.
     *
     * @param v Value to Set.
     */
    void setLastname(String v);

    /**
     * @return Returns the email.
     */
    String getEmail();

    /**
     * Sets the value for email.
     *
     * @param v Value to Set.
     */
    void setEmail(String v);

    /**
     * @return Returns the primary_password.
     */
    String getPrimaryPassword();

    /**
     * Sets the value for primary_password.
     *
     * @param v Value to Set.
     */
    void setPrimaryPassword(String v);

    /**
     * @return Returns the primary_password/encrypt.
     */
    @SuppressWarnings("unused")
    Boolean getPrimaryPassword_encrypt();

    /**
     * Sets the value for primary_password/encrypt.
     *
     * @param v Value to Set.
     */
    @SuppressWarnings("unused")
    void setPrimaryPassword_encrypt(Object v);

    /**
     * element_access
     *
     * @return Returns an ArrayList of org.nrg.xdat.om.XdatElementAccessI
     */
    ArrayList getElementAccess();

    /**
     * Sets the value for element_access.
     *
     * @param v Value to Set.
     * @throws Exception When an error occurs.
     */
    void setElementAccess(ItemI v) throws Exception;

    /**
     * assigned_roles/assigned_role
     *
     * @return Returns an ArrayList of org.nrg.xdat.om.XdatRoleTypeI
     */
    ArrayList getAssignedRoles_assignedRole();

    /**
     * Sets the value for assigned_roles/assigned_role.
     *
     * @param v Value to Set.
     * @throws Exception When an error occurs.
     */
    @SuppressWarnings("unused")
    void setAssignedRoles_assignedRole(ItemI v) throws Exception;

    /**
     * @return Returns the quarantine_path.
     */
    @SuppressWarnings("unused")
    String getQuarantinePath();

    /**
     * Sets the value for quarantine_path.
     *
     * @param v Value to Set.
     */
    @SuppressWarnings("unused")
    void setQuarantinePath(String v);

    /**
     * @return Returns the enabled.
     */
    Boolean getEnabled();

    /**
     * Sets the value for enabled.
     *
     * @param v Value to Set.
     */
    void setEnabled(Object v);

    /**
     * @return Returns the verified.
     */
    Boolean getVerified();

    /**
     * Sets the value for verified.
     *
     * @param v Value to Set.
     */
    void setVerified(Object v);

    /**
     * @return Returns the xdat_user_id.
     */
    Integer getXdatUserId();

    /**
     * Sets the value for xdat_user_id.
     *
     * @param v Value to Set.
     */
    @SuppressWarnings("unused")
    void setXdatUserId(Integer v);
}
