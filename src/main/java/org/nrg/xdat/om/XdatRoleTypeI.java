/*
 * org.nrg.xdat.om.XdatRoleTypeI
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
public interface XdatRoleTypeI {

    /**
     * Gets the schema element name.
     *
     * @return The schema element name.
     */
    String getSchemaElementName();

    /**
     * allowed_actions/allowed_action
     *
     * @return Returns an ArrayList of org.nrg.xdat.om.XdatActionTypeI
     */
    ArrayList getAllowedActions_allowedAction();

    /**
     * Sets the value for allowed_actions/allowed_action.
     *
     * @param v Value to Set.
     * @throws Exception When an error occurs.
     */
    @SuppressWarnings("unused")
    void setAllowedActions_allowedAction(ItemI v) throws Exception;

    /**
     * @return Returns the role_name.
     */
    @SuppressWarnings("unused")
    String getRoleName();

    /**
     * Sets the value for role_name.
     *
     * @param v Value to Set.
     */
    @SuppressWarnings("unused")
    void setRoleName(String v);

    /**
     * @return Returns the description.
     */
    String getDescription();

    /**
     * Sets the value for description.
     *
     * @param v Value to Set.
     */
    void setDescription(String v);

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
}
