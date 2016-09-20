/*
 * core: org.nrg.xdat.security.PermissionCriteriaI
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security;

/**
 * @author Tim Olsen &lt;tim@deck5consulting.com&gt;
 *         Interface used to track individual permissions within XNAT.
 *         Schema element name identifies the data type for which acceess is being restricted/permitted.
 *         Field is used to identify the relationship between the data object and the value being referenced.  (this is typically an xml path to a primary project field, or a xml path to a sharing/share field.
 *         Field value is typically the project ID that is being secured.
 *         So, the permissions could be ready, someone with this criteria can {ACTION} a {SCHEMA_ELEMENT_NAME} where {FIELD} is {FIELD_VALUE}.
 *         example: can 'CREATE' a 'xnat:subjectData' where 'xnat:subjectData/project' is 'project_id'
 *         example: can 'READ' a 'xnat:mrSessionData' where 'xnat:mrSessionData/sharing/share/project' is 'project_id2'
 */
public interface PermissionCriteriaI {

    /**
     * Gets the element name.
     *
     * @return The element name.
     */
    String getElementName();

    /**
     * Gets the field.
     *
     * @return The field.
     */
    String getField();

    /**
     * Gets the field value.
     *
     * @return The field value.
     */
    Object getFieldValue();

    /**
     * Indicates whether creation is allowed.
     *
     * @return Whether creation is allowed.
     */
    boolean getCreate();

    /**
     * Indicates whether reads are allowed.
     *
     * @return Whether reads are allowed.
     */
    boolean getRead();

    /**
     * Indicates whether edits are allowed.
     *
     * @return Whether edits are allowed.
     */
    boolean getEdit();

    /**
     * Indicates whether deletes are allowed.
     *
     * @return Whether deletes are allowed.
     */
    boolean getDelete();

    /**
     * Indicates whether activates are allowed.
     *
     * @return Whether activates are allowed.
     */
    boolean getActivate();

    /**
     * Does this criteria object allow access to any of the field/value combinations in this list of values.
     *
     * @param action The action to test: read, create, edit, delete, activate.
     * @param values The security values to test.
     * @return Returns true if the actions are permitted, false otherwise.
     * @throws Exception When something goes wrong.
     */
    boolean canAccess(final String action, final SecurityValues values) throws Exception;

    /**
     * Indicates whether this is active or not.
     *
     * @return Returns true if active, false otherwise.
     */
    boolean isActive();

    /**
     * Does this criteria object allow access to any of the actions?
     *
     * @param action The action to test: read, create, edit, delete, activate.
     * @return Returns true f the criteria object allows the action, false otherwise.
     */
    boolean getAction(final String action);
}