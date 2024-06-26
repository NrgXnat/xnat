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
import com.google.common.collect.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.nrg.framework.services.ContextService;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.exceptions.InvalidSearchException;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplayCriteria;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.*;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
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
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

@SuppressWarnings("RedundantThrows")
@Slf4j
public class Permissions {

    private static final int QUERY_GROUP_SIZE = 10000;

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
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
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
        return canAny(user.getUsername(), elementName, action);
    }

    /**
     * Can the user read any of the given elementName/action combination
     *
     * @param username    The user requesting access.
     * @param elementName The name of the element being requested.
     * @param action      The action being requested.
     *
     * @return Returns whether the user can read any of the given elementName/action combination
     */
    public static boolean canAny(final String username, final String elementName, final String action) {
        return getPermissionsService().canAny(username, elementName, action);
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
     * Set the accessibility (public/protected/private) of the entity represented by the tag. This is
     * used when creating a new project so that events aren't triggered for the permissions updates,
     * just the actual create operation.
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
    @SuppressWarnings("unused")
    public static boolean initializeDefaultAccessibility(String tag, String accessibility, boolean forceInit, final UserI authenticatedUser, EventMetaI ci) throws Exception {
        return getPermissionsService().initializeDefaultAccessibility(tag, accessibility, forceInit, authenticatedUser, ci);
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
     * <p>
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

    public static List<String> getAllProtectedProjects(final JdbcTemplate template) {
        return template.queryForList(QUERY_GET_PROTECTED_PROJECTS, String.class);
    }

    public static List<String> getAllProtectedProjects(final NamedParameterJdbcTemplate template) {
        return template.queryForList(QUERY_GET_PROTECTED_PROJECTS, EmptySqlParameterSource.INSTANCE, String.class);
    }

    public static List<String> getAllPublicProjects(final JdbcTemplate template) {
        return template.queryForList(QUERY_GET_PUBLIC_PROJECTS, String.class);
    }

    public static List<String> getAllPublicProjects(final NamedParameterJdbcTemplate template) {
        return template.queryForList(QUERY_GET_PUBLIC_PROJECTS, EmptySqlParameterSource.INSTANCE, String.class);
    }

    public static boolean canReadProject(final UserI user, final String projectId) {
        return canReadProject(null, user, projectId);
    }

    public static boolean canReadProject(final JdbcTemplate template, final UserI user, final String projectId) {
        if (template != null && user.isGuest()) {
            return getAllPublicProjects(template).contains(projectId) || getAllProtectedProjects(template).contains(projectId);
        }
        return Roles.isSiteAdmin(user) || Groups.isDataAdmin(user) || Groups.isDataAccess(user) || !isProjectPrivate(projectId) || StringUtils.isNotBlank(getUserProjectAccess(user, projectId));
    }

    public static boolean canEditProject(final UserI user, final String projectId) {
        if (Roles.isSiteAdmin(user) || Groups.isDataAdmin(user)) {
            return true;
        }
        final String access = getUserProjectAccess(user, projectId);
        return StringUtils.isNotBlank(access) && PROJECT_GROUPS.subList(1, PROJECT_GROUP_COUNT).contains(access);
    }

    public static boolean canDeleteProject(final UserI user, final String projectId) {
        if (Roles.isSiteAdmin(user) || Groups.isDataAdmin(user)) {
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
        final UserGroupI group = Groups.getGroupForUserAndTag(user, projectId);
        return group == null ? null : group.getId().substring(projectId.length() + 1);
    }

    public static boolean isProjectPublic(final NamedParameterJdbcTemplate template, final String projectId) {
        return StringUtils.equals("public", getProjectAccess(template, projectId));
    }

    public static boolean isProjectPublic(final String projectId) {
        return StringUtils.equals("public", getProjectAccess(projectId));
    }

    @SuppressWarnings("unused")
    public static boolean isProjectProtected(final NamedParameterJdbcTemplate template, final String projectId) {
        return StringUtils.equals("protected", getProjectAccess(template, projectId));
    }

    @SuppressWarnings("unused")
    public static boolean isProjectProtected(final String projectId) {
        return StringUtils.equals("protected", getProjectAccess(projectId));
    }

    @SuppressWarnings("unused")
    public static boolean isProjectPrivate(final NamedParameterJdbcTemplate template, final String projectId) {
        return StringUtils.equals("private", getProjectAccess(template, projectId));
    }

    @SuppressWarnings("unused")
    public static boolean isProjectPrivate(final String projectId) {
        return StringUtils.equals("private", getProjectAccess(projectId));
    }

    public static String getProjectAccess(final String projectId) {
        return getProjectAccess(null, projectId);
    }

    public static String getProjectAccess(final NamedParameterJdbcTemplate template, final String projectId) {
        final NamedParameterJdbcTemplate found = getTemplate(template);
        if (found != null) {
            try {
                return getProjectAccessByQuery(found, projectId);
            } catch (Exception e) {
                log.debug("Unable to get project access by query", e);
            }
        }
        try {
            final UserI guest = Users.getGuest();
            if (Permissions.canRead(guest, "xnat:subjectData/project", projectId)) {
                return "public";
            } else if (Permissions.canRead(guest, "xnat:projectData/ID", projectId)) {
                return "protected";
            } else {
                return "private";
            }
        } catch (Exception e) {
            log.error("An error occurred trying to retrieve accessibility for project {} through XFT", projectId, e);
            return null;
        }
    }

    @SuppressWarnings("unused")
    public static List<String> getOwnedProjects(final UserI user) {
        return getOwnedProjects(user.getUsername());
    }

    public static List<String> getOwnedProjects(final String username) {
        return getPermissionsService().getUserOwnedProjects(username);
    }

    @SuppressWarnings("unused")
    public static List<String> getEditableProjects(final UserI user) {
        return getEditableProjects(user.getUsername());
    }

    public static List<String> getEditableProjects(final String username) {
        return getPermissionsService().getUserEditableProjects(username);
    }

    public static List<String> getReadableProjects(final UserI user) {
        return getReadableProjects(user.getUsername());
    }

    public static List<String> getReadableProjects(final String username) {
        return getPermissionsService().getUserReadableProjects(username);
    }

    private static NamedParameterJdbcTemplate getTemplate(final NamedParameterJdbcTemplate template) {
        return ObjectUtils.defaultIfNull(template, getTemplate());
    }

    private static NamedParameterJdbcTemplate getTemplate() {
        if (_template == null) {
            _template = XDAT.getContextService().getBean(NamedParameterJdbcTemplate.class);
        }
        return _template;
    }

    public static String getProjectAccessByQuery(final NamedParameterJdbcTemplate template, final String projectId) {
        if (!verifyProjectExists(template, projectId)) {
            return null;
        }
        switch (checkProjectAccess(template, projectId)) {
            case 1:
                return "public";
            case 0:
                return "protected";
            default:
                return "private";
        }
    }

    public static boolean verifyProjectExists(final NamedParameterJdbcTemplate template, final String projectId) {
        return template.queryForObject(QUERY_PROJECT_EXISTS, new MapSqlParameterSource("projectId", projectId), Boolean.class);
    }

    public static boolean verifySubjectExists(final NamedParameterJdbcTemplate template, final String subjectId) {
        return template.queryForObject(QUERY_SUBJECT_EXISTS, new MapSqlParameterSource("subjectId", subjectId), Boolean.class);
    }

    public static ArrayListMultimap<String, String> verifyAccessToSessions(final NamedParameterJdbcTemplate template, final UserI user, final List<String> sessionIds) throws InsufficientPrivilegesException {
        return verifyAccessToSessions(template, user, new HashSet<>(sessionIds), null);
    }

    @SuppressWarnings("unused")
    public static ArrayListMultimap<String, String> verifyAccessToSessions(final NamedParameterJdbcTemplate template, final UserI user, final Set<String> sessionIds) throws InsufficientPrivilegesException {
        return verifyAccessToSessions(template, user, sessionIds, null);
    }

    public static ArrayListMultimap<String, String> verifyAccessToSessions(final NamedParameterJdbcTemplate template, final UserI user, final List<String> sessionIds, final String scopedProjectId) throws InsufficientPrivilegesException {
        return verifyAccessToSessions(template, user, new HashSet<>(sessionIds), scopedProjectId);
    }

    @Nonnull
    public static ArrayListMultimap<String, String> verifyAccessToSessions(final NamedParameterJdbcTemplate template, final UserI user, final Set<String> sessionIds, final String scopedProjectId) throws InsufficientPrivilegesException {
        final List<Map<String, Object>> locatedTypes        = template.queryForList(QUERY_GET_XSI_TYPES_FROM_EXPTS, new MapSqlParameterSource("sessionIds", sessionIds));
        final Set<String>               sessionsUserCanRead = new HashSet<>();
        for (final Map<String, Object> session : locatedTypes) {
            try {
                final String sessionId = session.get("id").toString();
                if (StringUtils.isBlank(scopedProjectId)) {
                    final Set<String> sessionSet = new HashSet<>();
                    sessionSet.add(sessionId);
                    final Multimap<String, String> projMap             = getProjectsForSessions(template, sessionSet);
                    final Set<String>              projectsSessionIsIn = projMap.keySet();
                    for (final String projectId : projectsSessionIsIn) {
                        if (canRead(user, session.get("xsi").toString() + "/project", projectId)) {
                            sessionsUserCanRead.add(sessionId);
                            break;
                        }
                    }
                } else {
                    if (canRead(user, session.get("xsi").toString() + "/project", scopedProjectId)) {
                        sessionsUserCanRead.add(sessionId);
                    }
                }
            } catch (Exception e) {
                throw new InsufficientPrivilegesException(user.getUsername(), scopedProjectId, sessionIds);
            }
        }

        // Get all projects, primary and shared, that contain the specified session IDs.
        final ArrayListMultimap<String, String> projectSessionMap = getProjectsForSessions(template, sessionsUserCanRead);

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

    public static ArrayListMultimap<String, String> getProjectsForSessions(final NamedParameterJdbcTemplate template, final Set<String> sessions) {
        final ArrayListMultimap<String, String> projectSessionMap = ArrayListMultimap.create();
        if (sessions.isEmpty()) {
            return projectSessionMap;
        }
        List<Map<String, Object>> located;
        if (sessions.size() < QUERY_GROUP_SIZE) {
            located = template.queryForList(QUERY_GET_PROJECTS_FROM_EXPTS, new MapSqlParameterSource("sessionIds", sessions));
        } else {
            located = new ArrayList<>();
            for (int i = 0; i < sessions.size(); i += QUERY_GROUP_SIZE) {
                Set<String> inGroup = sessions.stream().skip(i).limit(QUERY_GROUP_SIZE).collect(Collectors.toSet());
                located.addAll(template.queryForList(QUERY_GET_PROJECTS_FROM_EXPTS, new MapSqlParameterSource("sessionIds", inGroup)));
            }
        }
        if (located.isEmpty()) {
            throw new InvalidSearchException("The submitted sessions are not associated with any projects:\n * Sessions: " + Joiner.on(", ").join(sessions));
        }

        for (final Map<String, Object> session : located) {
            projectSessionMap.put(session.get("project").toString(), session.get("experiment").toString());
        }
        return projectSessionMap;
    }

    @SuppressWarnings("unused")
    public static Map<String, Map<String, String>> getAllAccessibleExperimentsOfType(final NamedParameterJdbcTemplate template, final UserI user, final String dataType, final String action) {
        final String query;
        switch (action) {
            case SecurityManager.READ:
                query = QUERY_ALL_READABLE_EXPTS_OF_TYPE;
                break;
            case SecurityManager.EDIT:
                query = QUERY_ALL_EDITABLE_EXPTS_OF_TYPE;
                break;
            case SecurityManager.DELETE:
                query = QUERY_ALL_DELETABLE_EXPTS_OF_TYPE;
                break;
            default:
                throw new RuntimeException("The action " + action + " isn't something I can check.");
        }
        final Map<String, Map<String, String>> experiments = new HashMap<>();
        template.query(query, new MapSqlParameterSource("username", user.getUsername()).addValue("dataType", dataType), new RowCallbackHandler() {
            @Override
            public void processRow(final ResultSet resultSet) throws SQLException {
                final String project = resultSet.getString("project");
                if (!experiments.containsKey(project)) {
                    experiments.put(project, new HashMap<>());
                }
                experiments.get(project).put(resultSet.getString("id"), resultSet.getString("label"));
            }
        });
        return experiments;
    }

    @SuppressWarnings("unused")
    public static Set<String> getInvalidProjectIds(final JdbcTemplate template, final Set<String> projectIds) {
        return Sets.difference(projectIds, getAllProjectIds(template));
    }

    @SuppressWarnings("unused")
    public static Set<String> getInvalidProjectIds(final NamedParameterJdbcTemplate template, final Set<String> projectIds) {
        return Sets.difference(projectIds, getAllProjectIds(template));
    }

    public static Set<String> getAllProjectIds(final JdbcTemplate template) {
        return new HashSet<>(template.queryForList("SELECT DISTINCT id from xnat_projectData", String.class));
    }

    public static Set<String> getAllProjectIds(final NamedParameterJdbcTemplate template) {
        return new HashSet<>(template.queryForList("SELECT DISTINCT id from xnat_projectData", EmptySqlParameterSource.INSTANCE, String.class));
    }

    /**
     * Returns a query template that can be used with a parameterized JDBC template to query whether an instance of a particular data type exists.
     *
     * @param table      The table in which the object may be persisted.
     * @param primaryKey The primary key to check.
     * @param parameter  The name of the parameter to be supplied to the parameterized template.
     *
     * @return A query that can be run to determine whether an object of the specified data type and ID exists.
     */
    public static String getObjectExistsQuery(final String table, final String primaryKey, final String parameter) {
        return StringSubstitutor.replace(QUERY_OBJECT_EXISTS, ImmutableMap.<String, Object>of("table", table, "pk", primaryKey, "parameter", parameter));
    }

    /**
     * Queries for project access for guest user to determine project accessibility. This method catches an exception that can occur when the guest user has been added to project groups.
     * It logs an error message informing administrators that the guest user has been added to those groups and performs the access query again without joining to user groups.
     *
     * @param template  The JDBC template for performing the query.
     * @param projectId The project to test for access level.
     *
     * @return Returns 1 if the project is public, 0 if the project is protected, and -1 if the project is private.
     */
    public static Integer checkProjectAccess(final NamedParameterJdbcTemplate template, final String projectId) {
        try {
            return template.queryForObject(QUERY_IS_PROJECT_PUBLIC_OR_PROTECTED, new MapSqlParameterSource("projectId", projectId), Integer.class);
        } catch (BadSqlGrammarException e) {
            final List<String> groupIds = _template.queryForList(GroupsAndPermissionsCache.QUERY_GET_GROUPS_FOR_USER, GUEST_QUERY_PARAMETERS, String.class);
            log.warn("Got a bad SQL grammar exception trying to find the access level for the project '{}', which usually indicates that the guest user has been added to groups. Checking for guest user access directly.\nGroups found for guest user: {}\nError code and message: [{}] {}\nSQL in error: {}", projectId, StringUtils.join(groupIds, ", "), e.getSQLException().getErrorCode(), e.getSQLException().getMessage(), e.getSql());
            return template.queryForObject(QUERY_IS_PROJECT_PUBLIC_OR_PROTECTED_GUEST_ONLY, new MapSqlParameterSource("projectId", projectId), Integer.class);
        }
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

    /**
     * Gets all protected project IDs from the database.
     */
    private static final String QUERY_GET_PROTECTED_PROJECTS = "SELECT "
                                                               + "  DISTINCT xfm.field_value AS project "
                                                               + "FROM xdat_field_mapping xfm "
                                                               + "  LEFT JOIN xdat_field_mapping_set xfms "
                                                               + "    ON xfm.xdat_field_mapping_set_xdat_field_mapping_set_id = xfms.xdat_field_mapping_set_id "
                                                               + "  LEFT JOIN xdat_element_access xea "
                                                               + "    ON xfms.permissions_allow_set_xdat_elem_xdat_element_access_id = xea.xdat_element_access_id "
                                                               + "  LEFT JOIN xdat_user xu ON xea.xdat_user_xdat_user_id = xu.xdat_user_id "
                                                               + "  LEFT JOIN xdat_user_groupid xugid ON xu.xdat_user_id = xugid.groups_groupid_xdat_user_xdat_user_id "
                                                               + "  LEFT JOIN xdat_usergroup xug ON xugid.groupid = xug.id "
                                                               + "WHERE xu.login = 'guest' "
                                                               + "      AND xea.element_name = 'xnat:projectData' "
                                                               + "      AND xfm.create_element = 0 "
                                                               + "      AND xfm.read_element = 1 "
                                                               + "      AND xfm.edit_element = 0 "
                                                               + "      AND xfm.delete_element = 0 "
                                                               + "      AND xfm.active_element = 0 "
                                                               + "      AND xfm.comparison_type = 'equals' "
                                                               + "ORDER BY project";

    /**
     * Gets all public project IDs from the database.
     */
    private static final String QUERY_GET_PUBLIC_PROJECTS = "SELECT "
                                                            + "  DISTINCT xfm.field_value AS project "
                                                            + "FROM xdat_field_mapping xfm "
                                                            + "  LEFT JOIN xdat_field_mapping_set xfms "
                                                            + "    ON xfm.xdat_field_mapping_set_xdat_field_mapping_set_id = xfms.xdat_field_mapping_set_id "
                                                            + "  LEFT JOIN xdat_element_access xea "
                                                            + "    ON xfms.permissions_allow_set_xdat_elem_xdat_element_access_id = xea.xdat_element_access_id "
                                                            + "  LEFT JOIN xdat_user xu ON xea.xdat_user_xdat_user_id = xu.xdat_user_id "
                                                            + "  LEFT JOIN xdat_user_groupid xugid ON xu.xdat_user_id = xugid.groups_groupid_xdat_user_xdat_user_id "
                                                            + "  LEFT JOIN xdat_usergroup xug ON xugid.groupid = xug.id "
                                                            + "WHERE xu.login = 'guest' "
                                                            + "      AND xea.element_name = 'xnat:projectData' "
                                                            + "      AND xfm.create_element = 0 "
                                                            + "      AND xfm.read_element = 1 "
                                                            + "      AND xfm.edit_element = 0 "
                                                            + "      AND xfm.delete_element = 0 "
                                                            + "      AND xfm.active_element = 1 "
                                                            + "      AND xfm.comparison_type = 'equals' "
                                                            + "ORDER BY project";

    private static final String QUERY_IS_PROJECT_PUBLIC_OR_PROTECTED            = "SELECT coalesce((SELECT xfm.active_element "
                                                                                  + "FROM xdat_field_mapping xfm "
                                                                                  + "  LEFT JOIN xdat_field_mapping_set xfms "
                                                                                  + "    ON xfm.xdat_field_mapping_set_xdat_field_mapping_set_id = xfms.xdat_field_mapping_set_id "
                                                                                  + "  LEFT JOIN xdat_element_access xea "
                                                                                  + "    ON xfms.permissions_allow_set_xdat_elem_xdat_element_access_id = xea.xdat_element_access_id "
                                                                                  + "  LEFT JOIN xdat_user xu ON xea.xdat_user_xdat_user_id = xu.xdat_user_id "
                                                                                  + "  LEFT JOIN xdat_user_groupid xugid ON xu.xdat_user_id = xugid.groups_groupid_xdat_user_xdat_user_id "
                                                                                  + "  LEFT JOIN xdat_usergroup xug ON xugid.groupid = xug.id "
                                                                                  + "WHERE xu.login = 'guest' "
                                                                                  + "      AND xea.element_name = 'xnat:projectData' "
                                                                                  + "      AND xfm.create_element = 0 "
                                                                                  + "      AND xfm.read_element = 1 "
                                                                                  + "      AND xfm.edit_element = 0 "
                                                                                  + "      AND xfm.delete_element = 0 "
                                                                                  + "      AND xfm.comparison_type = 'equals' "
                                                                                  + "      AND xfm.field_value = :projectId), -1)";
    private static final String QUERY_IS_PROJECT_PUBLIC_OR_PROTECTED_GUEST_ONLY = "SELECT coalesce((SELECT xfm.active_element "
                                                                                  + "FROM xdat_field_mapping xfm "
                                                                                  + "  LEFT JOIN xdat_field_mapping_set xfms "
                                                                                  + "    ON xfm.xdat_field_mapping_set_xdat_field_mapping_set_id = xfms.xdat_field_mapping_set_id "
                                                                                  + "  LEFT JOIN xdat_element_access xea "
                                                                                  + "    ON xfms.permissions_allow_set_xdat_elem_xdat_element_access_id = xea.xdat_element_access_id "
                                                                                  + "  LEFT JOIN xdat_user xu ON xea.xdat_user_xdat_user_id = xu.xdat_user_id "
                                                                                  + "WHERE xu.login = 'guest' "
                                                                                  + "      AND xea.element_name = 'xnat:projectData' "
                                                                                  + "      AND xfm.create_element = 0 "
                                                                                  + "      AND xfm.read_element = 1 "
                                                                                  + "      AND xfm.edit_element = 0 "
                                                                                  + "      AND xfm.delete_element = 0 "
                                                                                  + "      AND xfm.comparison_type = 'equals' "
                                                                                  + "      AND xfm.field_value = :projectId), -1)";
    private static final String QUERY_OBJECT_EXISTS                             = "SELECT EXISTS(SELECT " +
                                                                                  "  TRUE " +
                                                                                  "FROM ${table} " +
                                                                                  "WHERE ${pk} = :${parameter}) AS exists";
    private static final String QUERY_PROJECT_EXISTS                            = getObjectExistsQuery("xnat_projectdata", "id", "projectId");
    private static final String QUERY_SUBJECT_EXISTS                            = getObjectExistsQuery("xnat_subjectdata", "id", "subjectId");

    private static final List<String>       PROJECT_GROUPS         = Arrays.asList(AccessLevel.Collaborator.code(), AccessLevel.Member.code(), AccessLevel.Owner.code());
    private static final int                PROJECT_GROUP_COUNT    = PROJECT_GROUPS.size();
    private static final SqlParameterSource GUEST_QUERY_PARAMETERS = new MapSqlParameterSource(Users.USERNAME_PROPERTY, Users.DEFAULT_GUEST_USERNAME);

    private static final String QUERY_ALL_ACCESSIBLE_EXPTS_OF_TYPE = "SELECT id, label, project FROM data_type_fns_get_all_accessible_expts_of_type(:username, :dataType) WHERE can_${action} = TRUE";
    private static final String QUERY_ALL_READABLE_EXPTS_OF_TYPE   = StringSubstitutor.replace(QUERY_ALL_ACCESSIBLE_EXPTS_OF_TYPE, ImmutableMap.of("action", SecurityManager.READ));
    private static final String QUERY_ALL_EDITABLE_EXPTS_OF_TYPE   = StringSubstitutor.replace(QUERY_ALL_ACCESSIBLE_EXPTS_OF_TYPE, ImmutableMap.of("action", SecurityManager.EDIT));
    private static final String QUERY_ALL_DELETABLE_EXPTS_OF_TYPE  = StringSubstitutor.replace(QUERY_ALL_ACCESSIBLE_EXPTS_OF_TYPE, ImmutableMap.of("action", SecurityManager.DELETE));

    private static PermissionsServiceI        _service;
    private static NamedParameterJdbcTemplate _template;
}
