/*
 * core: org.nrg.xdat.security.PermissionSetI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security;

import java.util.List;

public interface PermissionSetI {

    /**
     * @return The schema element name.
     */
    String getSchemaElementName();

    /**
     * @param access The thing to access.
     * @param row    The security values.
     * @return Whether the element can be accessed.
     * @throws Exception When something goes wrong.
     */
    boolean canAccess(String access, SecurityValues row) throws Exception;

    /**
     * @return Whether anyone can read the element.
     */
    boolean canReadAny();

    /**
     * @return Whether anyone can create the element.
     */
    boolean canCreateAny();

    /**
     * @return Whether anyone can edit the element.
     */
    boolean canEditAny();

    /**
     * @return Whether the element is active.
     */
    boolean isActive();

    /**
     * @return What method the element supports.
     */
    String getMethod();

    /**
     * @return The permission criteria for the element.
     */
    List<PermissionCriteriaI> getAllCriteria();

    /**
     * @return The permission criteria for the element.
     */
    List<PermissionCriteriaI> getPermCriteria();

    /**
     * @return The permission sets for the element.
     */
    List<PermissionSetI> getPermSets();

    /**
     * Gets the permission criteria that matches the submitted values.
     *
     * @param fieldName The field name to test.
     * @param value     The value to test.
     * @return The matching permission criteria, if it exists.
     * @throws Exception When something goes wrong.
     */
    PermissionCriteriaI getMatchingPermissions(String fieldName, Object value) throws Exception;
}
