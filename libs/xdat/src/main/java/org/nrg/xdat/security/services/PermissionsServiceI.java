/*
 * core: org.nrg.xdat.security.services.PermissionsServiceI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.services;

import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.PermissionCriteriaI;
import org.nrg.xdat.security.SecurityValues;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xft.ItemI;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.exception.MetaDataException;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.security.UserI;

import java.util.List;
import java.util.Map;

public interface PermissionsServiceI {
    /**
     * Get all active permission criteria for this user account (including group permissions, etc).
     *
     * @param user     The user for which to retrieve permissions.
     * @param dataType The data type on which you want to retrieve permissions for the indicated user.
     * @return A list of the permissions for the user on the indicated data type.
     */
    List<PermissionCriteriaI> getPermissionsForUser(UserI user, String dataType);

    List<PermissionCriteriaI> getPermissionsForUser(String username, String dataType);

    /**
     * Get current XDAT criteria objects for current permission settings.  The XDAT criteria are used within the search engine to build long ugly WHERE clauses which limit the users access.  We'll want to refactor this if it isn't rewritten.
     *
     * @param user        The user for which to retrieve permissions.
     * @param rootElement The root element on which you want to retrieve permissions for the indicated user.
     * @return The {@link CriteriaCollection collection of criteria} for the user on the indicated root element.
     * @throws Exception When something goes wrong. 
     */
    CriteriaCollection getCriteriaForXDATRead(UserI user, SchemaElement rootElement) throws Exception;

    /**
     * Get current XFT criteria used when querying XFT items out of the database.
     *
     * @param user        The user for which to retrieve permissions.
     * @param rootElement The root element on which you want to retrieve permissions for the indicated user.
     * @return The {@link CriteriaCollection collection of criteria} for the user on the indicated root element.
     * @throws Exception When something goes wrong. 
     */
    CriteriaCollection getCriteriaForXFTRead(UserI user, SchemaElementI rootElement) throws Exception;

    /**
     * Can the user create an element based on a collection of key/value pairs {@link SecurityValues}.
     * 
     * This is similar to running canCreate(user, String, Object) for each row in the SecurityValues object.
     *
     * @param user        The user for which to retrieve permissions.
     * @param rootElement The root element on which you want to retrieve permissions for the indicated user.
     * @param values      The security values for the current context.
     * @return True if the user can create the element, false otherwise.
     * @throws Exception When something goes wrong. 
     */
    boolean canCreate(UserI user, SchemaElementI rootElement, SecurityValues values) throws Exception;

    /**
     * Can the user read an element based on a collection of key/value pairs {@link SecurityValues}.
     * 
     * This is similar to running canRead(user, String, Object) for each row in the SecurityValues object.
     *
     * @param user        The user for which to retrieve permissions.
     * @param rootElement The root element on which you want to retrieve permissions for the indicated user.
     * @param values      The security values for the current context.
     * @return True if the user can read the element, false otherwise.
     * @throws Exception When something goes wrong. 
     */
    boolean canRead(UserI user, SchemaElementI rootElement, SecurityValues values) throws Exception;

    /**
     * Can the user edit an element based on a collection of key/value pairs {@link SecurityValues}.
     * 
     * This is similar to running canEdit(user, String, Object) for each row in the SecurityValues object.
     *
     * @param user        The user for which to retrieve permissions.
     * @param rootElement The root element on which you want to retrieve permissions for the indicated user.
     * @param values      The security values for the current context.
     * @return True if the user can edit the element, false otherwise.
     * @throws Exception When something goes wrong. 
     */
    boolean canEdit(UserI user, SchemaElementI rootElement, SecurityValues values) throws Exception;

    /**
     * Can the user activate an element based on a collection of key/value pairs {@link SecurityValues}.
     * 
     * This is similar to running canActivate(user, String, Object) for each row in the SecurityValues object.
     *
     * @param user        The user for which to retrieve permissions.
     * @param rootElement The root element on which you want to retrieve permissions for the indicated user.
     * @param values      The security values for the current context.
     * @return True if the user can activate the element, false otherwise.
     * @throws Exception When something goes wrong. 
     */
    boolean canActivate(UserI user, SchemaElementI rootElement, SecurityValues values) throws Exception;

    /**
     * Can the user delete an element based on a collection of key/value pairs {@link SecurityValues}.
     * 
     * This is similar to running canDelete(user, String, Object) for each row in the SecurityValues object.
     *
     * @param user        The user for which to retrieve permissions.
     * @param rootElement The root element on which you want to retrieve permissions for the indicated user.
     * @param values      The security values for the current context.
     * @return True if the user can delete the element, false otherwise.
     * @throws Exception When something goes wrong. 
     */
    boolean canDelete(UserI user, SchemaElementI rootElement, SecurityValues values) throws Exception;

    /**
     * Can the user read the specified item
     *
     * @param user The user for which to retrieve permissions.
     * @param item The item on which you want to retrieve permissions for the indicated user.
     * @return True if the user can read the item, false otherwise.
     * @throws Exception When something goes wrong. 
     */
    boolean canRead(UserI user, ItemI item) throws Exception;

    /**
     * Can the user edit the specified item
     *
     * @param user The user for which to retrieve permissions.
     * @param item The item on which you want to retrieve permissions for the indicated user.
     * @return True if the user can edit the item, false otherwise.
     * @throws Exception When something goes wrong. 
     */
    boolean canEdit(UserI user, ItemI item) throws Exception;

    /**
     * Can the user create the specified item
     *
     * @param user The user for which to retrieve permissions.
     * @param item The item on which you want to retrieve permissions for the indicated user.
     * @return True if the user can create the item, false otherwise.
     * @throws Exception When something goes wrong. 
     */
    boolean canCreate(UserI user, ItemI item) throws Exception;

    /**
     * Can the user activate the specified item
     *
     * @param user The user for which to retrieve permissions.
     * @param item The item on which you want to retrieve permissions for the indicated user.
     * @return True if the user can activate the item, false otherwise.
     * @throws Exception When something goes wrong. 
     */
    boolean canActivate(UserI user, ItemI item) throws Exception;

    /**
     * Can the user delete the specified item
     *
     * @param user The user for which to retrieve permissions.
     * @param item The item on which you want to retrieve permissions for the indicated user.
     * @return True if the user can delete the item, false otherwise.
     * @throws Exception When something goes wrong.
     */
    boolean canDelete(UserI user, ItemI item) throws Exception;

    /**
     * Can the user read any of the given elementName/xmlPath/action combination
     *
     * @param user        The user for which to retrieve permissions.
     * @param elementName The element name.
     * @param xmlPath     The XML path.
     * @param action      The action.
     * @return True if the user can read any of the element, XML path, or action, false otherwise.
     */
    boolean canAny(UserI user, String elementName, String xmlPath, String action);

    /**
     * Can the user read any of the given elementName/action combination
     *
     * @param user        The user for which to retrieve permissions.
     * @param elementName The element name.
     * @param action      The action.
     * @return True if the user can read any of the element or action, false otherwise.
     */
    @SuppressWarnings("unused")
    boolean canAny(UserI user, String elementName, String action);

    /**
     * Can the user read any of the given elementName/action combination
     *
     * @param username    The user for which to retrieve permissions.
     * @param elementName The element name.
     * @param action      The action.
     * @return True if the user can read any of the element or action, false otherwise.
     */
    boolean canAny(final String username, String elementName, String action);

    /**
     * Can the user do the specified action for the String/Object pair
     *
     * @param user    The user for which to retrieve permissions.
     * @param xmlPath The XML path.
     * @param value   The value.
     * @param action  The action.
     * @return True if the user can perform the specified action for the XML path and value, false otherwise.
     * @throws Exception When something goes wrong. 
     */
    boolean can(UserI user, String xmlPath, Object value, String action) throws Exception;

    /**
     * Can the user do the specified action for the item
     *
     * @param user   The user for which to retrieve permissions.
     * @param item   The item on which you want to retrieve permissions for the indicated user.
     * @param action The action.
     * @return True if the user can perform the specified action for the item, false otherwise.
     * @throws Exception When something goes wrong.
     */
    boolean can(UserI user, ItemI item, String action) throws Exception;

    /**
     * Can the user read items for the String/Object pair
     *
     * @param user    The user for which to retrieve permissions.
     * @param xmlPath The XML path.
     * @param value   The value.
     * @return True if the user can perform the specified action for the XML path and value, false otherwise.
     * @throws Exception When something goes wrong. 
     */
    boolean canRead(UserI user, String xmlPath, Object value) throws Exception;

    /**
     * Can the user edit items for the String/Object pair
     *
     * @param user    The user for which to retrieve permissions.
     * @param xmlPath The XML path.
     * @param value   The value.
     * @return True if the user can edit for the XML path and value, false otherwise.
     * @throws Exception When something goes wrong. 
     */
    boolean canEdit(UserI user, String xmlPath, Object value) throws Exception;

    /**
     * Can the user create items for the String/Object pair
     *
     * @param user    The user for which to retrieve permissions.
     * @param xmlPath The XML path.
     * @param value   The value.
     * @return True if the user can create for the XML path and value, false otherwise.
     * @throws Exception When something goes wrong. 
     */
    boolean canCreate(UserI user, String xmlPath, Object value) throws Exception;

    /**
     * Can the user activate items for the String/Object pair
     *
     * @param user    The user for which to retrieve permissions.
     * @param xmlPath The XML path.
     * @param value   The value.
     * @return True if the user can activate for the XML path and value, false otherwise.
     * @throws Exception When something goes wrong. 
     */
    boolean canActivate(UserI user, String xmlPath, Object value) throws Exception;

    /**
     * Can the user delete items for the String/Object pair
     *
     * @param user    The user for which to retrieve permissions.
     * @param xmlPath The XML path.
     * @param value   The value.
     * @return True if the user can delete for the XML path and value, false otherwise.
     * @throws Exception When something goes wrong. 
     */
    boolean canDelete(UserI user, String xmlPath, Object value) throws Exception;

    /**
     * Indicates whether the user read the subject or experiment with the specified ID.
     *
     * @param user     The user for which to retrieve permissions.
     * @param entityId Represents the ID of the subject or experiment object.
     *
     * @return True if the user can perform the specified action for the XML path and value, false otherwise.
     *
     * @throws Exception When something goes wrong.
     */
    boolean canRead(final UserI user, final String entityId) throws Exception;

    /**
     * Indicates whether the user edit the subject or experiment with the specified ID.
     *
     * @param user     The user for which to retrieve permissions.
     * @param entityId Represents the ID of the subject or experiment object.
     *
     * @return True if the user can perform the specified action for the XML path and value, false otherwise.
     *
     * @throws Exception When something goes wrong.
     */
    boolean canEdit(final UserI user, final String entityId) throws Exception;

    /**
     * Indicates whether the user create the subject or experiment with the specified ID.
     *
     * @param user     The user for which to retrieve permissions.
     * @param entityId Represents the ID of the subject or experiment object.
     *
     * @return True if the user can perform the specified action for the XML path and value, false otherwise.
     *
     * @throws Exception When something goes wrong.
     */
    boolean canCreate(final UserI user, final String entityId) throws Exception;

    /**
     * Indicates whether the user delete the subject or experiment with the specified ID.
     *
     * @param user     The user for which to retrieve permissions.
     * @param entityId Represents the ID of the subject or experiment object.
     *
     * @return True if the user can perform the specified action for the XML path and value, false otherwise.
     *
     * @throws Exception When something goes wrong.
     */
    boolean canDelete(final UserI user, final String entityId) throws Exception;

    /**
     * Indicates whether the user activate the subject or experiment with the specified ID.
     *
     * @param user     The user for which to retrieve permissions.
     * @param entityId Represents the ID of the subject or experiment object.
     *
     * @return True if the user can perform the specified action for the XML path and value, false otherwise.
     *
     * @throws Exception When something goes wrong.
     */
    boolean canActivate(final UserI user, final String entityId) throws Exception;

    /**
     * Indicates whether the user read the subject or experiment with the specified ID in the context of the specified
     * project and property.
     *
     * @param user            The user for which to retrieve permissions.
     * @param project         The ID of the project.
     * @param entityId        Represents the ID of the subject or experiment object.
     *
     * @return True if the user can perform the specified action for the XML path and value, false otherwise.
     *
     * @throws Exception When something goes wrong.
     */
    boolean canRead(final UserI user, final String project, final String entityId) throws Exception;

    /**
     * Indicates whether the user edit the subject or experiment with the specified ID.
     *
     * @param user            The user for which to retrieve permissions.
     * @param project         The ID of the project.
     * @param entityId        Represents the ID of the subject or experiment object.
     *
     * @return True if the user can perform the specified action for the XML path and value, false otherwise.
     *
     * @throws Exception When something goes wrong.
     */
    boolean canEdit(final UserI user, final String project, final String entityId) throws Exception;

    /**
     * Indicates whether the user create the subject or experiment with the specified ID.
     *
     * @param user            The user for which to retrieve permissions.
     * @param project         The ID of the project.
     * @param entityId        Represents the ID of the subject or experiment object.
     *
     * @return True if the user can perform the specified action for the XML path and value, false otherwise.
     *
     * @throws Exception When something goes wrong.
     */
    boolean canCreate(final UserI user, final String project, final String entityId) throws Exception;

    /**
     * Indicates whether the user delete the subject or experiment with the specified ID.
     *
     * @param user            The user for which to retrieve permissions.
     * @param project         The ID of the project.
     * @param entityId        Represents the ID of the subject or experiment object.
     *
     * @return True if the user can perform the specified action for the XML path and value, false otherwise.
     *
     * @throws Exception When something goes wrong.
     */
    boolean canDelete(final UserI user, final String project, final String entityId) throws Exception;

    /**
     * Indicates whether the user activate the subject or experiment with the specified ID.
     *
     * @param user            The user for which to retrieve permissions.
     * @param project         The ID of the project.
     * @param entityId        Represents the ID of the subject or experiment object.
     *
     * @return True if the user can perform the specified action for the XML path and value, false otherwise.
     *
     * @throws Exception When something goes wrong.
     */
    boolean canActivate(final UserI user, final String project, final String entityId) throws Exception;

    /**
     * Can the user create/update this item and potentially all of its descendants
     *
     * @param user    The user for which to retrieve permissions.
     * @param item    The item on which you want to retrieve permissions for the indicated user.
     * @param descend Whether the create/update should affect descendants.
     * @return True if the user can perform the specified action for the item, false otherwise.
     * @throws Exception When something goes wrong.
     */
    String canStoreItem(UserI user, ItemI item, boolean descend) throws Exception;

    /**
     * Review the passed item and remove any child items that this user doesn't have access to.
     *
     * @param user The user for which to retrieve permissions.
     * @param item The item on which you want to retrieve permissions for the indicated user.
     * @return The secured item.
     * @throws IllegalAccessException When the user can't access the item.
     * @throws MetaDataException When there's an error in the item metadata.
     */
    ItemI secureItem(UserI user, ItemI item) throws IllegalAccessException, MetaDataException;

    /**
     * Get the values that this user can do the specified action on for the given element/XMLPath combo
     *
     * @param user        The user for which to retrieve permissions.
     * @param elementName The element name.
     * @param xmlPath     The XML path.
     * @param action      The action.
     * @return The allowed values for the user on the combination of factors.
     */
    List<Object> getAllowedValues(UserI user, String elementName, String xmlPath, String action);

    List<Object> getAllowedValues(String username, String elementName, String xmlPath, String action);

    /**
     * Get the XMLPath/value combos that this user can do the specified action on for the given element
     *
     * @param user        The user for which to retrieve permissions.
     * @param elementName The element name.
     * @param action      The action.
     * @return The allowed values for the user on the combination of factors.
     */
    Map<String, Object> getAllowedValues(UserI user, String elementName, String action);

    Map<String, Object> getAllowedValues(String username, String elementName, String action);

    /**
     * initialize or update the permissions of the 'effected' user based on thee parameters
     *
     * @param effected        The 'effected' user.
     * @param authenticated   The authenticated user.
     * @param elementName     The element name.
     * @param psf             Permissions.
     * @param value           The value.
     * @param create          Whether the user can create the indicated object.
     * @param read            Whether the user can read the indicated object.
     * @param delete          Whether the user can delete the indicated object.
     * @param edit            Whether the user can edit the indicated object.
     * @param activate        Whether the user can activate the indicated object.
     * @param activateChanges Whether the changes should be activated.
     * @param ci              Associated event metadata.
     */
    void setPermissions(UserI effected, UserI authenticated, String elementName, String psf, String value, Boolean create, Boolean read, Boolean delete, Boolean edit, Boolean activate, boolean activateChanges, EventMetaI ci);

    /**
     * Set the accessibility (public/protected/private) of the entity represented by the tag. This is
     * used when creating a new entity so that events aren't triggered for the permissions updates,
     * just the actual create operation.
     *
     * @param tag               The tag.
     * @param accessibility     The accessibility setting for the tag.
     * @param forceInit         Whether initialization of the tag should be forced.
     * @param authenticatedUser The authenticated user.
     * @param ci                Associated event metadata.
     * @return True if the accessibility was set for entity, false otherwise.
     * @throws Exception When something goes wrong. 
     */
    boolean initializeDefaultAccessibility(String tag, String accessibility, boolean forceInit, final UserI authenticatedUser, EventMetaI ci) throws Exception;

    /**
     * Set the accessibility (public/protected/private) of the entity represented by the tag
     *
     * @param tag               The tag.
     * @param accessibility     The accessibility setting for the tag.
     * @param forceInit         Whether initialization of the tag should be forced.
     * @param authenticatedUser The authenticated user.
     * @param ci                Associated event metadata.
     * @return True if the accessibility was set for entity, false otherwise.
     * @throws Exception When something goes wrong.
     */
    boolean setDefaultAccessibility(String tag, String accessibility, boolean forceInit, final UserI authenticatedUser, EventMetaI ci) throws Exception;

    /**
     * Get all active permission criteria for this user group / data type combination.
     *
     * @param group    The group for which to retrieve permissions.
     * @param dataType The data type for which to retrieve permissions.
     * @return The list of permissions for the group on the data type.
     */
    List<PermissionCriteriaI> getPermissionsForGroup(UserGroupI group, String dataType);

    /**
     * Get all active permission criteria for this user group (organized by data type).
     *
     * @param group The user group for which to retrieve permissions.
     * @return A map of the permission groups for the user group.
     */
    Map<String, List<PermissionCriteriaI>> getPermissionsForGroup(UserGroupI group);

    /**
     * Adds specified permissions for this group.
     *
     * @param group             The user group for which to retrieve permissions.
     * @param criteria          The permission criteria.
     * @param meta              The event meta.
     * @param authenticatedUser The authenticated user.
     * @throws Exception When something goes wrong. 
     */
    void setPermissionsForGroup(UserGroupI group, List<PermissionCriteriaI> criteria, EventMetaI meta, UserI authenticatedUser) throws Exception;

    /**
     * Return an SQL statement that will return a list of this user's permissions
     *
     * @param user The user for which to retrieve permissions.
     * @return The user's permissions.
     */
    String getUserPermissionsSQL(UserI user);

    /**
     * Gets a list of IDs of the projects that a user can read.
     *
     * @param user The user for which to retrieve permissions.
     *
     * @return A list of the IDs of the projects that the specified user can read.
     */
    @SuppressWarnings("unused")
    List<String> getUserReadableProjects(UserI user);

    List<String> getUserReadableProjects(String username);

    /**
     * Gets a list of IDs of the projects that a user can edit.
     *
     * @param user The user for which to retrieve permissions.
     *
     * @return A list of the IDs of the projects that the specified user can edit.
     */
    List<String> getUserEditableProjects(UserI user);

    List<String> getUserEditableProjects(String username);

    /**
     * Gets a list of IDs of the projects that a user owns (i.e. which the user can delete)
     *
     * @param user The user for which to retrieve permissions.
     *
     * @return A list of the IDs of the projects that the specified user owns.
     */
    @SuppressWarnings("unused")
    List<String> getUserOwnedProjects(UserI user);

    List<String> getUserOwnedProjects(String username);
}