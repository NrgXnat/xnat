/*
 * core: org.nrg.xdat.om.XdatSecurityI
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
public interface XdatSecurityI {

    String getSchemaElementName();

    /**
     * users/user
     *
     * @return Returns an ArrayList of org.nrg.xdat.om.XdatUserI
     */
    ArrayList getUsers_user();

    /**
     * Sets the value for users/user.
     *
     * @param v Value to Set.
     * @throws Exception When an error occurs.
     */
    @SuppressWarnings("unused")
    void setUsers_user(ItemI v) throws Exception;

    /**
     * roles/role
     *
     * @return Returns an ArrayList of org.nrg.xdat.om.XdatRoleTypeI
     */
    ArrayList getRoles_role();

    /**
     * Sets the value for roles/role.
     *
     * @param v Value to Set.
     * @throws Exception When an error occurs.
     */
    @SuppressWarnings("unused")
    void setRoles_role(ItemI v) throws Exception;

    /**
     * actions/action
     *
     * @return Returns an ArrayList of org.nrg.xdat.om.XdatActionTypeI
     */
    ArrayList getActions_action();

    /**
     * Sets the value for actions/action.
     *
     * @param v Value to Set.
     * @throws Exception When an error occurs.
     */
    @SuppressWarnings("unused")
    void setActions_action(ItemI v) throws Exception;

    /**
     * element_security_set/element_security
     *
     * @return Returns an ArrayList of org.nrg.xdat.om.XdatElementSecurityI
     */
    ArrayList getElementSecuritySet_elementSecurity();

    /**
     * Sets the value for element_security_set/element_security.
     *
     * @param v Value to Set.
     * @throws Exception When an error occurs.
     */
    @SuppressWarnings("unused")
    void setElementSecuritySet_elementSecurity(ItemI v) throws Exception;

    /**
     * @return Returns the system.
     */
    String getSystem();

    /**
     * Sets the value for system.
     *
     * @param v Value to Set.
     */
    void setSystem(String v);

    /**
     * @return Returns the require_login.
     */
    @SuppressWarnings("unused")
    Boolean getRequireLogin();

    /**
     * Sets the value for require_login.
     *
     * @param v Value to Set.
     */
    @SuppressWarnings("unused")
    void setRequireLogin(Object v);

    /**
     * @return Returns the xdat_security_id.
     */
    @SuppressWarnings("unused")
    Integer getXdatSecurityId();

    /**
     * Sets the value for xdat_security_id.
     *
     * @param v Value to Set.
     */
    @SuppressWarnings("unused")
    void setXdatSecurityId(Integer v);
}
