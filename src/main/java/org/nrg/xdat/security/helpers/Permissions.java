/*
 * core: org.nrg.xdat.security.helpers.Permissions
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.helpers;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.services.ContextService;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.exceptions.InvalidSearchException;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplayCriteria;
import org.nrg.xdat.security.*;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xdat.services.cache.UserProjectCache;
import org.nrg.xft.ItemI;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.exception.InvalidItemException;
import org.nrg.xft.exception.MetaDataException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.SearchCriteria;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.io.IOException;
import java.util.*;

@Slf4j
public class Permissions {
    /**
     * Returns the currently configured permissions service. You can customize the implementation returned by adding a
     * new implementation to the org.nrg.xdat.security.user.custom package (or a differently configured package). You
     * can change the default implementation returned via the security.userManagementService.default configuration
     * parameter.
     *
     * @return The permissions service.
     */
    public static PermissionsServiceI getPermissionsService() {
        // MIGRATION: All of these services need to switch from having the implementation in the prefs service to autowiring from the context.
        if (_service == null) {
            // First find out if it exists in the application context.
            final ContextService contextService = XDAT.getContextService();
            if (contextService != null) {
                try {
                    return _service = contextService.getBean(PermissionsServiceI.class);
                } catch (NoSuchBeanDefinitionException ignored) {
                    // This is OK, we'll just create it from the indicated class.
                }
            }
            try {
                List<Class<?>> classes = Reflection.getClassesForPackage(XDAT.safeSiteConfigProperty("security.permissionsService.package", "org.nrg.xdat.permissions.custom"));

                if (classes != null && classes.size() > 0) {
                    for (Class<?> clazz : classes) {
                        if (PermissionsServiceI.class.isAssignableFrom(clazz)) {
                            _service = (PermissionsServiceI) clazz.newInstance();
                        }
                    }
                }
            } catch (ClassNotFoundException | InstantiationException | IOException | IllegalAccessException e) {
                log.error("", e);
            }

            //default to PermissionsServiceImpl implementation (unless a different default is configured)
            if (_service == null) {
                try {
                    String className = XDAT.safeSiteConfigProperty("security.permissionsService.default", "org.nrg.xdat.security.PermissionsServiceImpl");
                    _service = (PermissionsServiceI) Class.forName(className).newInstance();
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    log.error("", e);
                }
            }
        }
        return _service;
    }

    /**
     * Returns the {@link UserProjectCache user project cache}.
     *
     * @return The user project cache.
     */
    public static UserProjectCache getUserProjectCache() {
        // MIGRATION: All of these services need to switch from having the implementation in the prefs service to autowiring from the context.
        if (_cache == null) {
            // First find out if it exists in the application context.
            final ContextService contextService = XDAT.getContextService();
            if (contextService != null) {
                try {
                    return _cache = contextService.getBean(UserProjectCache.class);
                } catch (NoSuchBeanDefinitionException ignored) {
                    log.warn("Unable to find an instance of the UserProjectCache class.");
                }
            }
        }
        return _cache;
    }

    /**
     * Get current XDAT criteria objects for current permission settings.  The XDAT criteria are used within the search engine to build long ugly WHERE clauses which limit the users access.  We'll want to refactor this if it isn't rewritten.
     *
     * @param user        The user.
     * @param rootElement The root element.
     *
     * @return The requested criteria collection.
     *
     * @throws Exception When something goes wrong.
     */
    public static CriteriaCollection getCriteriaForXDATRead(UserI user, SchemaElement rootElement) throws Exception {
        return getPermissionsService().getCriteriaForXDATRead(user, rootElement);
    }

    /**
     * Get current XFT criteria used when querying XFT items out of the database.
     *
     * @param user        The user.
     * @param rootElement The root element.
     *
     * @return The requested criteria collection.
     *
     * @throws Exception When something goes wrong.
     */
    public static CriteriaCollection getCriteriaForXFTRead(UserI user, SchemaElementI rootElement) throws Exception {
        return getPermissionsService().getCriteriaForXFTRead(user, rootElement);
    }

    /**
     * Can the user create an element based on a collection of key/value pairs {@link SecurityValues}.
     *
     * This is similar to running canCreate(user, String, Object) for each row in the SecurityValues object.
     *
     * @param user   The user.
     * @param root   The root element.
     * @param values The security values.
     *
     * @return Whether the user can create an element of the indicated type.
     *
     * @throws Exception When something goes wrong.
     */
    @SuppressWarnings("unused")
    public static boolean canCreate(UserI user, SchemaElementI root, SecurityValues values) throws Exception {
        return getPermissionsService().canCreate(user, root, values);
    }

    /**
     * Can the user read an element based on a collection of key/value pairs {@link SecurityValues}.
     *
     * This is similar to running canRead(user, String, Object) for each row in the SecurityValues object.
     *
     * @param user   The user.
     * @param root   The root element.
     * @param values The security values.
     *
     * @return Whether the user can read an element of the indicated type.
     *
     * @throws Exception When something goes wrong.
     */
    public static boolean canRead(UserI user, SchemaElementI root, SecurityValues values) throws Exception {
        return getPermissionsService().canRead(user, root, values);
    }

    /**
     * Can the user edit an element based on a collection of key/value pairs {@link SecurityValues}.
     *
     * This is similar to running canEdit(user, String, Object) for each row in the SecurityValues object.
     *
     * @param user   The user.
     * @param root   The root element.
     * @param values The security values.
     *
     * @return Whether the user can edit an element of the indicated type.
     *
     * @throws Exception When something goes wrong.
     */
    @SuppressWarnings("unused")
    public static boolean canEdit(UserI user, SchemaElementI root, SecurityValues values) throws Exception {
        return getPermissionsService().canEdit(user, root, values);
    }

    /**
     * Can the user activate an element based on a collection of key/value pairs {@link SecurityValues}.
     *
     * This is similar to running canActivate(user, String, Object) for each row in the SecurityValues object.
     *
     * @param user   The user.
     * @param root   The root element.
     * @param values The security values.
     *
     * @return Whether the user can activate an element of the indicated type.
     *
     * @throws Exception When something goes wrong.
     */
    @SuppressWarnings("unused")
    public static boolean canActivate(UserI user, SchemaElementI root, SecurityValues values) throws Exception {
        return getPermissionsService().canActivate(user, root, values);
    }

    /**
     * Can the user delete an element based on a collection of key/value pairs {@link SecurityValues}.
     *
     * This is similar to running canDelete(user, String, Object) for each row in the SecurityValues object.
     *
     * @param user   The user.
     * @param root   The root element.
     * @param values The security values.
     *
     * @return Whether the user can delete an element of the indicated type.
     *
     * @throws Exception When something goes wrong.
     */
    @SuppressWarnings("unused")
    public static boolean canDelete(UserI user, SchemaElementI root, SecurityValues values) throws Exception {
        return getPermissionsService().canDelete(user, root, values);
    }

    /**
     * Can the user do the specified action for the item
     *
     * @param user   The user to test.
     * @param item   The item to test.
     * @param action The action to be performed.
     *
     * @return Whether the user can perform the specified action on the item.
     *
     * @throws Exception When something goes wrong.
     */
    public static boolean can(UserI user, ItemI item, String action) throws Exception {
        return getPermissionsService().can(user, item, action);
    }

    /**
     * Can the user do the specified action for the String/Object pair
     *
     * @param user    The user.
     * @param xmlPath The XML path to the attribute.
     * @param value   The value to test.
     * @param action  The action to be performed.
     *
     * @return Whether the user can perform the specified action.
     *
     * @throws Exception When something goes wrong.
     */
    public static boolean can(UserI user, String xmlPath, Object value, String action) throws Exception {
        return getPermissionsService().can(user, xmlPath, value, action);
    }

    /**
     * Can the user read the specified item
     *
     * @param user The user.
     * @param item The item.
     *
     * @return Whether the user can read the specified item.
     *
     * @throws Exception When something goes wrong.
     */
    public static boolean canRead(UserI user, ItemI item) throws Exception {
        return getPermissionsService().canRead(user, item);
    }

    /**
     * Can the user edit the specified item
     *
     * @param user The user.
     * @param item The item.
     *
     * @return Whether the user can edit the specified item.
     *
     * @throws Exception When something goes wrong.
     */
    public static boolean canEdit(UserI user, ItemI item) throws Exception {
        return getPermissionsService().canEdit(user, item);
    }

    /**
     * Can the user create the specified item
     *
     * @param user The user.
     * @param item The item.
     *
     * @return Whether the user can create the specified item.
     *
     * @throws Exception When something goes wrong.
     */
    public static boolean canCreate(UserI user, ItemI item) throws Exception {
        return getPermissionsService().canCreate(user, item);
    }

    /**
     * Can the user activate the specified item
     *
     * @param user The user.
     * @param item The item.
     *
     * @return Whether the user can activate the specified item.
     *
     * @throws Exception When something goes wrong.
     */
    public static boolean canActivate(UserI user, ItemI item) throws Exception {
        return getPermissionsService().canActivate(user, item);
    }

    /**
     * Can the user delete the specified item
     *
     * @param user The user.
     * @param item The item.
     *
     * @return Whether the user can delete the specified item.
     *
     * @throws Exception When something goes wrong.
     */
    public static boolean canDelete(UserI user, ItemI item) throws Exception {
        return getPermissionsService().canDelete(user, item);
    }

    /**
     * Can the user create/update this item and potentially all of its descendants
     *
     * @param user    The user.
     * @param item    The item.
     * @param descend Whether the descendants should be tested.
     *
     * @return Whether the user can store the item.
     *
     * @throws Exception When something goes wrong.
     */
    public static String canStoreItem(UserI user, ItemI item, boolean descend) throws Exception {
        return getPermissionsService().canStoreItem(user, item, descend);
    }

    /**
     * Review the passed item and remove any child items that this user doesn't have access to.
     *
     * @param user The user.
     * @param item The item.
     *
     * @return The cleared item.
     *
     * @throws IllegalAccessException When the user is not permitted to access the item.
     * @throws MetaDataException      When an error occurs with the item metadata.
     */
    public static ItemI secureItem(UserI user, ItemI item) throws IllegalAccessException, MetaDataException {
        return getPermissionsService().secureItem(user, item);
    }

    /**
     * Can the user read items for the String/Object pair
     *
     * @param user    The user to test.
     * @param xmlPath The property to test.
     * @param value   The value to test.
     *
     * @return True or false.
     *
     * @throws InvalidItemException When the submitted properties don't resolve to a valid item.
     * @throws Exception            When an unknown or unexpected error occurs.
     */
    public static boolean canRead(UserI user, String xmlPath, Object value) throws Exception {
        return getPermissionsService().canRead(user, xmlPath, value);
    }

    /**
     * Can the user edit items for the String/Object pair
     *
     * @param user    The user to test.
     * @param xmlPath The property to test.
     * @param value   The value to test.
     *
     * @return True or false.
     *
     * @throws InvalidItemException When the submitted properties don't resolve to a valid item.
     * @throws Exception            When an unknown or unexpected error occurs.
     */
    public static boolean canEdit(UserI user, String xmlPath, Object value) throws Exception {
        return getPermissionsService().canEdit(user, xmlPath, value);
    }

    /**
     * Can the user create items for the String/Object pair
     *
     * @param user    The user to test.
     * @param xmlPath The property to test.
     * @param value   The value to test.
     *
     * @return True or false.
     *
     * @throws InvalidItemException When the submitted properties don't resolve to a valid item.
     * @throws Exception            When an unknown or unexpected error occurs.
     */
    public static boolean canCreate(UserI user, String xmlPath, Object value) throws Exception {
        return getPermissionsService().canCreate(user, xmlPath, value);
    }

    /**
     * Can the user activate items for the String/Object pair
     *
     * @param user    The user to test.
     * @param xmlPath The property to test.
     * @param value   The value to test.
     *
     * @return True or false.
     *
     * @throws InvalidItemException When the submitted properties don't resolve to a valid item.
     * @throws Exception            When an unknown or unexpected error occurs.
     */
    public static boolean canActivate(UserI user, String xmlPath, Object value) throws Exception {
        return getPermissionsService().canActivate(user, xmlPath, value);
    }

    /**
     * Can the user delete items for the String/Object pair
     *
     * @param user    The user to test.
     * @param xmlPath The property to test.
     * @param value   The value to test.
     *
     * @return True or false.
     *
     * @throws InvalidItemException When the submitted properties don't resolve to a valid item.
     * @throws Exception            When an unknown or unexpected error occurs.
     */
    public static boolean canDelete(UserI user, String xmlPath, Object value) throws Exception {
        return getPermissionsService().canDelete(user, xmlPath, value);
    }

    /**
     * Can the user read any of the given elementName/xmlPath/action combination
     *
     * @param user        The user requesting access.
     * @param elementName The name of the element being requested.
     * @param xmlPath     The XML path being requested.
     * @param action      The action being requested.
     *
     * @return Returns whether the user can read any of the given elementName/xmlPath/action combination
     */
    public static boolean canAny(UserI user, String elementName, String xmlPath, @SuppressWarnings("SameParameterValue") String action) {
        return getPermissionsService().canAny(user, elementName, xmlPath, action);
    }

    /**
     * Can the user read any of the given elementName/action combination
     *
     * @param user        The user requesting access.
     * @param elementName The name of the element being requested.
     * @param action      The action being requested.
     *
     * @return Returns whether the user can read any of the given elementName/action combination
     */
    public static boolean canAny(UserI user, String elementName, String action) {
        return getPermissionsService().canAny(user, elementName, action);
    }

    /**
     * Get current XDAT criteria objects for current permission settings.  The XDAT criteria are used within the search
     * engine to build long ugly WHERE clauses which limit the users access.  We'll want to refactor this if it isn't
     * rewritten.
     *
     * @param set    The permission set.
     * @param root   The root schema element.
     * @param action The action being requested.
     *
     * @return Returns current XDAT criteria objects for current permission settings
     *
     * @throws IllegalAccessException When the user is not permitted to access the item.
     * @throws Exception              When an unknown or unexpected error occurs.
     */
    public static CriteriaCollection getXDATCriteria(PermissionSetI set, SchemaElement root, String action) throws Exception {
        final CriteriaCollection coll = new CriteriaCollection(set.getMethod());

        for (PermissionCriteriaI c : set.getPermCriteria()) {
            if (c.isActive()) {
                if (c.getAction(action)) {
                    coll.addClause(DisplayCriteria.buildCriteria(root, c));
                }
            }
        }

        for (PermissionSetI subset : set.getPermSets()) {
            final CriteriaCollection sub = getXDATCriteria(subset, root, action);
            coll.addClause(sub);
        }
        return coll;
    }

    /**
     * Get current XFT criteria used when querying XFT items out of the database.
     *
     * @param set    The permission set.
     * @param action The requested action.
     *
     * @return Returns a collection of the current XFT criteria used when querying XFT items out of the database
     *
     * @throws Exception When an unknown or unexpected error occurs.
     */
    public static CriteriaCollection getXFTCriteria(PermissionSetI set, String action) throws Exception {
        final CriteriaCollection coll = new CriteriaCollection(set.getMethod());

        for (PermissionCriteriaI c : set.getPermCriteria()) {
            if (c.isActive()) {
                if (c.getAction(action)) {
                    coll.addClause(SearchCriteria.buildCriteria(c));
                }
            }
        }

        for (PermissionSetI subset : set.getPermSets()) {
            final CriteriaCollection sub = getXFTCriteria(subset, action);
            coll.addClause(sub);
        }
        return coll;
    }

    /**
     * Checks the security settings of the data type to see if the user can perform this query.
     * To check if users have been specifically allowed to see any objects of that type, use canReadAny.
     *
     * @param user        The user to test.
     * @param elementName The element to test.
     *
     * @return Returns whether the user can perform this query
     */
    public static boolean canQuery(final UserI user, final String elementName) {
        try {
            Authorizer.getInstance().authorizeRead(GenericWrapperElement.GetElement(elementName), user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks the security settings of the data type and the user's permissions to see if the user can access any items of this type.
     *
     * @param user        The user to test.
     * @param elementName The element to test.
     *
     * @return Returns whether the user can access any items of this type
     */
    @SuppressWarnings("unused")
    public static boolean canReadAny(final UserI user, final String elementName) {
        try {
            Authorizer.getInstance().authorizeRead(GenericWrapperElement.GetElement(elementName), user);

            return Permissions.canAny(user, elementName, SecurityManager.READ);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the values that this user can do the specified action on for the given element/xmlpath combo
     *
     * @param user        The user requesting access.
     * @param elementName The name of the element being requested.
     * @param xmlPath     The XML path being requested.
     * @param action      The action being requested.
     *
     * @return Returns a list of the values that this user can do the specified action on for the given element/xmlpath combo
     */
    public static List<Object> getAllowedValues(UserI user, String elementName, String xmlPath, String action) {
        return getPermissionsService().getAllowedValues(user, elementName, xmlPath, action);
    }

    /**
     * Get the xmlpath/value combos that this user can do the specified action on for the given element
     *
     * @param user        The user requesting access.
     * @param elementName The name of the element being requested.
     * @param action      The action being requested.
     *
     * @return Returns a map of the xmlpath/value combos that this user can do the specified action on for the given element
     */
    public static Map<String, Object> getAllowedValues(UserI user, String elementName, String action) {
        return getPermissionsService().getAllowedValues(user, elementName, action);
    }

    /**
     * initialize or update the permissions of the 'affected' user based on the parameters
     *
     * @param affected        The affected user.
     * @param authenticated   The authenticated user.
     * @param elementName     The name of the element.
     * @param psf             Path to a property on the element.
     * @param value           The value to set.
     * @param create          Whether the user can perform a create operation.
     * @param read            Whether the user can perform a read operation.
     * @param delete          Whether the user can perform a delete operation.
     * @param edit            Whether the user can perform an edit operation.
     * @param activate        Whether the user can perform an activate operation.
     * @param activateChanges Whether the user can perform an activate changes operation.
     * @param ci              Event metadata.
     */
    @SuppressWarnings("SameParameterValue")
    public static void setPermissions(UserI affected, UserI authenticated, String elementName, String psf, String value, Boolean create, Boolean read, Boolean delete, Boolean edit, Boolean activate, boolean activateChanges, EventMetaI ci) {
        getPermissionsService().setPermissions(affected, authenticated, elementName, psf, value, create, read, delete, edit, activate, activateChanges, ci);
    }

    /**
     * Set the accessibility (public/protected/private) of the entity represented by the tag
     *
     * @param tag               The tag to set.
     * @param accessibility     The accessibility level to set.
     * @param forceInit         Whether the entity should be initialized.
     * @param authenticatedUser The user setting the accessibility.
     * @param ci                Event metadata.
     *
     * @return Returns whether the accessibility was set for entity
     *
     * @throws Exception When an unknown or unexpected error occurs.
     */
    public static boolean setDefaultAccessibility(String tag, String accessibility, boolean forceInit, final UserI authenticatedUser, EventMetaI ci) throws Exception {
        return getPermissionsService().setDefaultAccessibility(tag, accessibility, forceInit, authenticatedUser, ci);
    }

    /**
     * Get all active permission criteria for this user account / data type combination (including group permissions, etc).
     *
     * @param user     The user to evaluate.
     * @param dataType The datatype to evaluate.
     *
     * @return Returns a list of active permission criteria for this user account / data type combination (including group permissions, etc)
     */
    @SuppressWarnings("unused")
    public static List<PermissionCriteriaI> getPermissionsForUser(UserI user, String dataType) {
        return getPermissionsService().getPermissionsForUser(user, dataType);
    }

    /**
     * Get all active permission criteria for this user group / data type combination.
     *
     * @param group    The group to set permissions for.
     * @param dataType The datatype to set permissions for.
     *
     * @return Returns a list of active permission criteria for this user group / data type combination
     */
    @SuppressWarnings("unused")
    public static List<PermissionCriteriaI> getPermissionsForGroup(UserGroupI group, String dataType) {
        return getPermissionsService().getPermissionsForGroup(group, dataType);
    }

    /**
     * Get all active permission criteria for this user group (organized by data type).
     *
     * @param group The group to retrieve permissions for.
     *
     * @return Returns a map of active permission criteria for this user group (organized by data type)
     */
    @SuppressWarnings("unused")
    public static Map<String, List<PermissionCriteriaI>> getPermissionsForGroup(UserGroupI group) {
        return getPermissionsService().getPermissionsForGroup(group);
    }

    /**
     * Adds/modifies specified permissions for this group.  However, nothing is saved to the database.
     *
     * Call Groups.save() to save the modifications.
     *
     * @param group             The group to modify.
     * @param criteria          The criteria to set.
     * @param meta              Event metadata.
     * @param authenticatedUser The user setting the permissions.
     *
     * @throws Exception When an unknown or unexpected error occurs.
     */
    public static void setPermissionsForGroup(UserGroupI group, List<PermissionCriteriaI> criteria, EventMetaI meta, UserI authenticatedUser) throws Exception {
        getPermissionsService().setPermissionsForGroup(group, criteria, meta, authenticatedUser);
    }

    /**
     * Return an SQL statement that will return a list of this user's permissions
     *
     * @param user The user to evaluate.
     *
     * @return Returns the SQL statement that will return a list of this user's permissions
     */
    public static String getUserPermissionsSQL(UserI user) {
        return getPermissionsService().getUserPermissionsSQL(user);
    }

    public static boolean hasAccess(final UserI user, final AccessLevel accessLevel) throws Exception {
        switch (accessLevel) {
            case Null:
                return true;

            case Admin:
                return Roles.isSiteAdmin(user);

            case Authenticated:
                return !user.isGuest();

            default:
                return false;
        }
    }

    public static boolean hasAccess(final UserI user, final String projectId, final AccessLevel accessLevel) throws Exception {
        switch (accessLevel) {
            case Null:
            case Admin:
            case Authenticated:
                return hasAccess(user, accessLevel);

            case Read:
                return Permissions.canReadProject(user, projectId);

            case Edit:
                return Permissions.canEditProject(user, projectId);

            case Delete:
                return Permissions.canDeleteProject(user, projectId);

            case Owner:
                return Permissions.isProjectOwner(user, projectId);

            case Member:
                return Permissions.isProjectMember(user, projectId) || Permissions.isProjectOwner(user, projectId);

            case Collaborator:
                return Permissions.isProjectCollaborator(user, projectId) || Permissions.isProjectMember(user, projectId) || Permissions.isProjectOwner(user, projectId);

            default:
                return false;
        }
    }

    public static boolean canReadProject(final UserI user, final String projectId) throws Exception {
        return Roles.isSiteAdmin(user) || isProjectPublic(projectId) || StringUtils.isNotBlank(getUserProjectAccess(user, projectId));
    }

    public static boolean canEditProject(final UserI user, final String projectId) {
        if (Roles.isSiteAdmin(user)) {
            return true;
        }
        final String access = getUserProjectAccess(user, projectId);
        return StringUtils.isNotBlank(access) && PROJECT_GROUPS.subList(1, PROJECT_GROUP_COUNT).contains(access);
    }

    public static boolean canDeleteProject(final UserI user, final String projectId) {
        if (Roles.isSiteAdmin(user)) {
            return true;
        }
        final String access = getUserProjectAccess(user, projectId);
        return StringUtils.isNotBlank(access) && PROJECT_GROUPS.subList(2, PROJECT_GROUP_COUNT).contains(access);
    }

    public static boolean isProjectOwner(final UserI user, final String projectId) {
        return AccessLevel.Owner.equals(getUserProjectAccess(user, projectId));
    }

    public static boolean isProjectMember(final UserI user, final String projectId) {
        return AccessLevel.Member.equals(getUserProjectAccess(user, projectId));
    }

    public static boolean isProjectCollaborator(final UserI user, final String projectId) {
        return AccessLevel.Collaborator.equals(getUserProjectAccess(user, projectId));
    }

    public static String getUserProjectAccess(final UserI user, final String projectId) {
        try {
            final List<UserGroupI> groups      = Groups.getGroupsByTag(projectId);
            final List<String>     usersGroups = Groups.getGroupIdsForUser(user);
            if (CollectionUtils.isNotEmpty(usersGroups) && CollectionUtils.isNotEmpty(groups)) {
                for (final UserGroupI group : groups) {
                    final String groupId = group.getId();
                    if (groupId != null && usersGroups.contains(groupId)) {
                        return groupId.substring(projectId.length() + 1);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("An error occurred trying to find the access level to the project " + projectId + " for the user " + user.getUsername(), e);
        }
        return null;
    }

    public static boolean isProjectPublic(final String projectId) throws Exception {
        return StringUtils.equals("public", getProjectAccess(projectId));
    }

    @SuppressWarnings("unused")
    public static boolean isProjectProtected(final String projectId) throws Exception {
        return StringUtils.equals("protected", getProjectAccess(projectId));
    }

    @SuppressWarnings("unused")
    public static boolean isProjectPrivate(final String projectId) throws Exception {
        return StringUtils.equals("private", getProjectAccess(projectId));
    }

    public static String getProjectAccess(final String projectId) throws Exception {
        final UserI guest = Users.getGuest();
        if (Permissions.canRead(guest, "xnat:subjectData/project", projectId)) {
            return "public";
        } else if (Permissions.canRead(guest, "xnat:projectData/ID", projectId)) {
            return "protected";
        } else {
            return "private";
        }
    }

    public static Multimap<String, String> verifyAccessToSessions(final NamedParameterJdbcTemplate template, final UserI user, final List<String> sessionIds) throws InsufficientPrivilegesException {
        return verifyAccessToSessions(template, user, new HashSet<>(sessionIds), null);
    }

    public static Multimap<String, String> verifyAccessToSessions(final NamedParameterJdbcTemplate template, final UserI user, final Set<String> sessionIds) throws InsufficientPrivilegesException {
        return verifyAccessToSessions(template, user, sessionIds, null);
    }

    public static Multimap<String, String> verifyAccessToSessions(final NamedParameterJdbcTemplate template, final UserI user, final List<String> sessionIds, final String scopedProjectId) throws InsufficientPrivilegesException {
        return verifyAccessToSessions(template, user, new HashSet<>(sessionIds), scopedProjectId);
    }

    public static Multimap<String, String> verifyAccessToSessions(final NamedParameterJdbcTemplate template, final UserI user, final Set<String> sessionIds, final String scopedProjectId) throws InsufficientPrivilegesException {
        final List<Map<String, Object>> locatedTypes = template.queryForList(QUERY_GET_XSI_TYPES_FROM_EXPTS, new HashMap<String, Object>() {{
            put("sessionIds", sessionIds);
        }});
        HashSet<String> sessionsUserCanRead = new HashSet<>();
        for (final Map<String, Object> session : locatedTypes) {
            try{
                if(StringUtils.isBlank(scopedProjectId)){
                    Set<String> sessionSet = new HashSet<>();
                    sessionSet.add(session.get("id").toString());
                    Multimap<String, String> projMap = getProjectsForSessions(template, sessionSet);
                    Set<String> projectsSessionIsIn = projMap.keySet();
                    boolean canReadOne = false;
                    for(String pr : projectsSessionIsIn){
                        if(canRead(user, session.get("xsi").toString()+"/project", pr)){
                            canReadOne = true;
                        }
                    }
                    if(canReadOne){
                        sessionsUserCanRead.add(session.get("id").toString());
                    }
                }
                else {
                    if (canRead(user, session.get("xsi").toString() + "/project", scopedProjectId)) {
                        sessionsUserCanRead.add(session.get("id").toString());
                    }
                }
            }
            catch(Exception e){
                throw new InsufficientPrivilegesException(user.getUsername(), scopedProjectId, sessionIds);
            }
        }

        // Get all projects, primary and shared, that contain the specified session IDs.
        final Multimap<String, String> projectSessionMap = getProjectsForSessions(template, sessionsUserCanRead);

        // If they specified a project ID...
        final Set<String> projectIds = projectSessionMap.keySet();
        if (StringUtils.isNotBlank(scopedProjectId)) {
            // Make sure that it's in the list of projects associated with the session IDs.
            if (!projectSessionMap.containsKey(scopedProjectId)) {
                // If it's not, then it's time to freak out.
                throw new InsufficientPrivilegesException(user.getUsername(), scopedProjectId, sessionsUserCanRead);
            }

            // Now check that all of the requested sessions are available in the scoped project.
            final Collection<String> located = projectSessionMap.get(scopedProjectId);
            if (!located.containsAll(sessionsUserCanRead)) {
                throw new InsufficientPrivilegesException(user.getUsername(), scopedProjectId, Sets.difference(new HashSet<>(sessionsUserCanRead), new HashSet<>(located)));
            }

            // Limit the map to just the specified project.
            for (final String projectId : new ArrayList<>(projectIds)) {
                if (!StringUtils.equals(scopedProjectId, projectId)) {
                    projectSessionMap.removeAll(projectId);
                }
            }
        }

        final List<String> unauthorized = Lists.newArrayList();
        for (final String projectId : projectIds) {
            try {
                if (!Permissions.canReadProject(user, projectId)) {
                    unauthorized.add(projectId);
                }
            } catch (Exception e) {
                log.warn("An exception occurred trying to test read access for user " + user.getUsername() + " on project " + projectId + ". Adding project as unauthorized but this may be incorrect depending on the nature of the error.", e);
                unauthorized.add(projectId);
            }
        }

        // Remove any projects to which the user doesn't have access from consideration.
        if (unauthorized.size() > 0) {
            for (final String unauthorizedProjectId : unauthorized) {
                projectSessionMap.removeAll(unauthorizedProjectId);
            }

            // Now get the sessions that are available in the remaining authorized projects.
            final Set<String> authorized = new HashSet<>(projectSessionMap.values());

            // The list of sessions from accessible projects should be the same as the submitted list of sessions or
            // else the user requested sessions that aren't accessible. In that case, freak out.
            if (authorized.size() != sessionsUserCanRead.size()) {
                throw new InsufficientPrivilegesException(user.getUsername(), Sets.difference(sessionsUserCanRead, authorized));
            }
        }

        return projectSessionMap;
    }

    public static Multimap<String, String> getProjectsForSessions(final NamedParameterJdbcTemplate template, final Set<String> sessions) {
        final ArrayListMultimap<String, String> projectSessionMap = ArrayListMultimap.create();
        if(sessions.size()<=0){
            return projectSessionMap;
        }

        final List<Map<String, Object>> located = template.queryForList(QUERY_GET_PROJECTS_FROM_EXPTS, new HashMap<String, Object>() {{
            put("sessionIds", sessions);
        }});
        if (located.size() == 0) {
            throw new InvalidSearchException("The submitted sessions are not associated with any projects:\n * Sessions: " + Joiner.on(", ").join(sessions));
        }

        for (final Map<String, Object> session : located) {
            projectSessionMap.put(session.get("project").toString(), session.get("experiment").toString());
        }
        return projectSessionMap;
    }

    public static Set<String> getInvalidProjectIds(final JdbcTemplate template, final Set<String> projectIds) {
        return Sets.difference(projectIds, getAllProjectIds(template));
    }

    public static Set<String> getInvalidProjectIds(final NamedParameterJdbcTemplate template, final Set<String> projectIds) {
        return Sets.difference(projectIds, getAllProjectIds(template));
    }

    public static Set<String> getAllProjectIds(final JdbcTemplate template) {
        return new HashSet<>(template.queryForList("SELECT DISTINCT id from xnat_projectData", String.class));
    }

    public static Set<String> getAllProjectIds(final NamedParameterJdbcTemplate template) {
        return new HashSet<>(template.queryForList("SELECT DISTINCT id from xnat_projectData", Collections.<String, Object>emptyMap(), String.class));
    }

    /**
     * Requires one parameter:
     *
     * <ul>
     * <li><b>sessions</b> is a list of session IDs</li>
     * </ul>
     */
    private static final String QUERY_GET_PROJECTS_FROM_EXPTS = "SELECT "
            + "  expt.project AS project, "
            + "  expt.id      AS experiment "
            + "FROM xnat_experimentdata expt "
            + "WHERE expt.id IN (:sessionIds) "
            + "UNION DISTINCT "
            + "SELECT "
            + "  share.project                            AS project, "
            + "  share.sharing_share_xnat_experimentda_id AS experiment "
            + "FROM xnat_experimentdata_share share "
            + "WHERE share.sharing_share_xnat_experimentda_id IN (:sessionIds) "
            + "ORDER BY project";

    /**
     * Requires one parameter:
     *
     * <ul>
     * <li><b>sessions</b> is a list of session IDs</li>
     * </ul>
     */
    private static final String QUERY_GET_XSI_TYPES_FROM_EXPTS = "SELECT "
            + "  xnat_experimentdata.id         AS id, "
            + "  xdat_meta_element.element_name AS xsi "
            + "FROM xnat_experimentdata "
            + "LEFT JOIN xdat_meta_element "
            + "ON xnat_experimentdata.extension=xdat_meta_element.xdat_meta_element_id "
            + "WHERE xnat_experimentdata.id IN (:sessionIds)";

    private static final List<String> PROJECT_GROUPS        = Arrays.asList(AccessLevel.Collaborator.code(), AccessLevel.Member.code(), AccessLevel.Owner.code());
    private static final int          PROJECT_GROUP_COUNT   = PROJECT_GROUPS.size();

    private static PermissionsServiceI _service = null;
    private static UserProjectCache    _cache   = null;
}
