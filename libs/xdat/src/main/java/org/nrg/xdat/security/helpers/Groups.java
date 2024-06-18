/*
 * core: org.nrg.xdat.security.helpers.Groups
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.framework.services.ContextService;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.UserGroupServiceI;
import org.nrg.xdat.security.group.exceptions.GroupFieldMappingException;
import org.nrg.xdat.security.user.exceptions.UserFieldMappingException;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Groups {
    public static final String       ALL_DATA_ADMIN_GROUP   = "ALL_DATA_ADMIN";
    public static final String       ALL_DATA_ACCESS_GROUP  = "ALL_DATA_ACCESS";
    public static final List<String> ALL_DATA_GROUPS        = Arrays.asList(Groups.ALL_DATA_ADMIN_GROUP, Groups.ALL_DATA_ACCESS_GROUP);
    public static final String       OWNER_GROUP            = "owner";
    public static final String       OWNER_NAME             = "Owners";
    public static final String       MEMBER_GROUP           = "member";
    public static final String       MEMBER_NAME            = "Members";
    public static final String       COLLABORATOR_GROUP     = "collaborator";
    public static final String       COLLABORATOR_NAME      = "Collaborators";
    public static final String       USERS                  = "users";
    public static final String       OPERATION_ADD_USERS    = "addUsers";
    public static final String       OPERATION_REMOVE_USERS = "removeUsers";
    public static final String       REMOVED                = "removedGroups";
    public static final Pattern      REGEX_PROJECT_GROUP    = Pattern.compile("(?<project>.*)_(?<access>owner|member|collaborator)");

    /**
     * Returns the currently configured permissions service. You can customize the implementation returned by adding a
     * new implementation to the org.nrg.xdat.security.user.custom package (or a differently configured package). Change
     * the default implementation returned via the security.userManagementService.default configuration parameter.
     *
     * @return An instance of the {@link UserGroupServiceI user group service}.
     */
    public static UserGroupServiceI getUserGroupService() {
        // MIGRATION: All of these services need to switch from having the implementation in the prefs service to autowiring from the context.
        if (_singleton == null) {
            // First find out if it exists in the application context.
            final ContextService contextService = XDAT.getContextService();
            if (contextService != null) {
                try {
                    return _singleton = contextService.getBean(UserGroupServiceI.class);
                } catch (NoSuchBeanDefinitionException ignored) {
                    // This is OK, we'll just create it from the indicated class.
                }
            }
            try {
                List<Class<?>> classes = Reflection.getClassesForPackage(XDAT.safeSiteConfigProperty("security.userGroupService.package", "org.nrg.xdat.groups.custom"));

                if (classes != null && classes.size() > 0) {
                    for (Class<?> clazz : classes) {
                        if (UserGroupServiceI.class.isAssignableFrom(clazz)) {
                            return _singleton = (UserGroupServiceI) clazz.newInstance();
                        }
                    }
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException e) {
                log.error("", e);
            }

            //default to PermissionsServiceImpl implementation (unless a different default is configured)
            try {
                final String                             className = XDAT.safeSiteConfigProperty("security.userGroupService.default", "org.nrg.xdat.security.UserGroupManager");
                final Class<? extends UserGroupServiceI> aClass    = Class.forName(className).asSubclass(UserGroupServiceI.class);
                try {
                    final Constructor<? extends UserGroupServiceI> constructor = aClass.getConstructor(GroupsAndPermissionsCache.class);
                    return _singleton = constructor.newInstance(getGroupsAndPermissionsCache());
                } catch (NoSuchMethodException | InvocationTargetException e) {
                    return _singleton = aClass.newInstance();
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                log.error("", e);
            }
        }
        return _singleton;
    }

    public static GroupsAndPermissionsCache getGroupsAndPermissionsCache() {
        // MIGRATION: All of these services need to switch from having the implementation in the prefs service to autowiring from the context.
        if (_cache == null) {
            // First find out if it exists in the application context.
            final ContextService contextService = XDAT.getContextService();
            if (contextService != null) {
                try {
                    return _cache = contextService.getBean(GroupsAndPermissionsCache.class);
                } catch (NoSuchBeanDefinitionException ignored) {
                    log.warn("Unable to find an instance of the GroupsAndPermissionsCache class.");
                }
            }
        }
        return _cache;
    }

    /**
     * Uses the {@link #REGEX_PROJECT_GROUP} regular expression to determine if the submitted group ID is a project-related
     * group in the form <b><i>PROJECT_ID</i>_<i>access</i></b>, where <i>access</i> is one of <b>owner</b>, <b>member</b>,
     * or <b>collaborator</b>.
     *
     * You can get the project ID and access level for the group ID by calling {@link #getProjectIdAndAccessFromGroupId(String)}.
     *
     * @param groupId The group ID to evaluate.
     *
     * @return Returns true if the ID matches a project group, false otherwise.
     */
    public static boolean isProjectGroup(final String groupId) {
        return REGEX_PROJECT_GROUP.matcher(groupId).matches();
    }

    /**
     * Extracts the project ID and access level from a {@link #isProjectGroup(String) project-related group ID}. The
     * left value of the returned pair contains the project ID while the right value contains the access level. If
     * the submitted group ID is blank or doesn't match the project-related group format, this returns a null pair.
     *
     * @param groupId The group ID from which the project ID and access level should be extracted.
     *
     * @return A pair containing the project ID in the left value and access level in the right.
     */
    public static Pair<String, String> getProjectIdAndAccessFromGroupId(final String groupId) {
        final Matcher matcher = REGEX_PROJECT_GROUP.matcher(groupId);
        return matcher.find() ? ImmutablePair.of(matcher.group("project"), matcher.group("access")) : ImmutablePair.nullPair();
    }

    /**
     * Convenience method to determine whether the user is a system data administrator.
     *
     * @param user The user to test for system data administrator access.
     *
     * @return Returns true if the user is a system data administrator, false otherwise.
     */
    public static boolean isSiteAdmin(final UserI user) {
        return Roles.isSiteAdmin(user);
    }

    public static boolean isDataAdmin(final UserI user) {
        return Groups.isMember(user, ALL_DATA_ADMIN_GROUP);
    }
    public static boolean isDataAdmin(final String user) {
        try {
            return Groups.isMember(user, ALL_DATA_ADMIN_GROUP);
        } catch (UserNotFoundException e) {
            return false;
        }
    }

    public static boolean isDataAccess(final UserI user) {
        return Groups.isMember(user, ALL_DATA_ACCESS_GROUP);
    }

    public static boolean isDataAccess(final String user) {
        try {
            return Groups.isMember(user, ALL_DATA_ACCESS_GROUP);
        } catch (UserNotFoundException e) {
            return false;
        }
    }

    public static boolean hasAllDataAccess(final UserI user) {
        return isSiteAdmin(user) || isDataAdmin(user) || isDataAccess(user);
    }

    public static boolean hasAllDataAdmin(final UserI user) {
        return isSiteAdmin(user) || isDataAdmin(user);
    }

    /**
     * Get a UserGroupI by the group ID.
     *
     * @param groupId The ID of the group to which the user should be added.
     *
     * @return The group with the indicated ID.
     */
    public static UserGroupI getGroup(String groupId) {
        return getUserGroupService().getGroup(groupId);
    }

    /**
     * Get the UserGroup that are currently assigned to a user.  Loads current groups from database.
     *
     * @param user The user on which to search.
     *
     * @return A map of groups to which the user is assigned.
     */
    public static Map<String, UserGroupI> getGroupsForUser(UserI user) {
        return getUserGroupService().getGroupsForUser(user);
    }

    /**
     * Get the UserGroup that are currently assigned to a user.  Loads current groups from database.
     *
     * @param username The user on which to search.
     *
     * @return A map of groups to which the user is assigned.
     */
    public static Map<String, UserGroupI> getGroupsForUser(final String username) throws UserNotFoundException {
        return getUserGroupService().getGroupsForUser(username);
    }

    /**
     * Get the group IDs currently assigned to a user.  Only reviews local object, groups may not be saved yet.
     *
     * @param user The user on which to search.
     *
     * @return A list of the IDs of the groups to which the user is assigned.
     */
    public static List<String> getGroupIdsForUser(UserI user) {
        return getUserGroupService().getGroupIdsForUser(user);
    }

    /**
     * Tests whether the user is assigned to the group with the indicated ID.
     *
     * @param user    The user to test for group assignment.
     * @param groupId The group ID on which to search.
     *
     * @return Returns true if the user is a member of the indicated group, false otherwise.
     */
    public static boolean isMember(UserI user, String groupId) {
        return getUserGroupService().isMember(user, groupId);
    }

    /**
     * Tests whether the user is assigned to the group with the indicated ID.
     *
     * @param user    The username to test for group assignment.
     * @param groupId The group ID on which to search.
     *
     * @return Returns true if the user is a member of the indicated group, false otherwise.
     */
    public static boolean isMember(String user, String groupId) throws UserNotFoundException {
        return getUserGroupService().isMember(user, groupId);
    }

    /**
     * Add this group for the specified user (locally).   This will not update the database.  It will add the user to this group in local memory.
     * <p>
     * Sometimes, a user group is associated with a user, before the user group is physically created in the database.  This method can be used to do that.
     * <p>
     * To specifically add the user to the group permanently, use the AddUserToGroup method
     *
     * @param user    The user on which to search.
     * @param groupId The group ID on which to search.
     * @param group   The group to be updated.
     */
    public static void updateUserForGroup(UserI user, String groupId, UserGroupI group) {
        getUserGroupService().updateUserForGroup(user, groupId, group);
    }

    /**
     * Remove user from the group (including updating database if necessary)
     *
     * @param user              The user to remove from the group.
     * @param authenticatedUser The user requesting the removal.
     * @param groupId           The ID of the group from which the user should be removed.
     * @param ci                The event metadata.
     *
     * @throws Exception When an error occurs.
     */
    public static void removeUserFromGroup(UserI user, UserI authenticatedUser, String groupId, EventMetaI ci) throws Exception {
        if (isMember(user, groupId)) {
            getUserGroupService().removeUserFromGroup(user, authenticatedUser, groupId, ci);
        }
    }

    /**
     * Remove users from the group (including updating database if necessary)
     *
     * @param groupId           The ID of the group from which the user should be removed.
     * @param authenticatedUser The user requesting the removal.
     * @param users             The users to remove from the group.
     * @param eventMeta         The event metadata.
     *
     * @throws Exception When an error occurs.
     */
    public static void removeUsersFromGroup(final String groupId, final UserI authenticatedUser, final List<UserI> users, final EventMetaI eventMeta) throws Exception {
        getUserGroupService().removeUsersFromGroup(groupId, authenticatedUser, users, eventMeta);
    }

    /**
     * Refresh the user group for this user (this updates any local copies of the group for this user).  This should be eliminated by a more clear caching mechanism.
     *
     * @param user    The user to be refreshed.
     * @param groupId The group ID on which to search.
     */
    public static void reloadGroupForUser(UserI user, String groupId) {
        getUserGroupService().reloadGroupForUser(user, groupId);
    }

    /**
     * Refresh all of the user groups for this user.
     *
     * @param user The user to be refreshed.
     */
    public static void reloadGroupsForUser(UserI user) {
        getUserGroupService().reloadGroupsForUser(user);
    }

    /**
     * Get groups that have the specified tag.
     *
     * @param tag The tag to search on.
     *
     * @return A list of the groups with the indicated tag.
     *
     * @throws Exception When an error occurs.
     */
    public static List<UserGroupI> getGroupsByTag(String tag) throws Exception {
        return getUserGroupService().getGroupsByTag(tag);
    }

    /**
     * Retrieves a group by its primary key.
     *
     * @param gID The group's primary key.
     *
     * @return Returns the requested group if found, null otherwise.
     */
    public static UserGroupI getGroupByPK(Object gID) {
        return getUserGroupService().getGroupByPK(gID);
    }

    /**
     * Gets the group by user and tag. The value for tag generally maps to project ID so, although a tag may be associated with more than one
     * groups, users are generally associated with just one of those groups.
     *
     * @param user The user on which to search.
     * @param tag  The tag to search on.
     *
     * @return The group with the submitted tag that is also associated with the specified user.
     */
    public static UserGroupI getGroupForUserAndTag(final UserI user, final String tag) {
        return getUserGroupService().getGroupForUserAndTag(user, tag);
    }

    /**
     * Searches for a group based on the combination of tag and display name.
     *
     * @param tag         The tag on which to search.
     * @param displayName The display name on which to search.
     *
     * @return The group if found.
     */
    public static UserGroupI getGroupByTagAndName(final String tag, final String displayName) {
        return getUserGroupService().getGroupByTagAndName(tag, displayName);
    }

    /**
     * Create user group using the defined permissions.
     *
     * @param id                : String ID to use for the group ID
     * @param displayName       The display name for the group.
     * @param create            : true if members should be able to create the data types in the List&lt;ElementSecurity&gt; ess, else false
     * @param read              : true if members should be able to read the data types in the List&lt;ElementSecurity&gt; ess, else false
     * @param delete            : true if members should be able to delete the data types in the List&lt;ElementSecurity&gt; ess, else false
     * @param edit              : true if members should be able to edit the data types in the List&lt;ElementSecurity&gt; ess, else false
     * @param activate          : true if members should be able to activate the data types in the List&lt;ElementSecurity&gt; ess, else false
     * @param activateChanges   : Should the permissions be activated upon creation (or wait for approval later)
     * @param ess               : List of data types that this group should have permissions for
     * @param authenticatedUser The user creating the group.
     *
     * @return The newly created group.
     */
    @SuppressWarnings("unused")
    public static UserGroupI createGroup(final String id, final String displayName, Boolean create, Boolean read, Boolean delete, Boolean edit, Boolean activate, boolean activateChanges, List<ElementSecurity> ess, String value, UserI authenticatedUser) {
        return getUserGroupService().createGroup(id, displayName, create, read, delete, edit, activate, activateChanges, ess, value, authenticatedUser);
    }

    /**
     * Create user group using the defined permissions.
     *
     * @param id                : String ID to use for the group ID
     * @param displayName       The display name for the group.
     * @param create            : true if members should be able to create the data types in the List&lt;ElementSecurity&gt; ess, else false
     * @param read              : true if members should be able to read the data types in the List&lt;ElementSecurity&gt; ess, else false
     * @param delete            : true if members should be able to delete the data types in the List&lt;ElementSecurity&gt; ess, else false
     * @param edit              : true if members should be able to edit the data types in the List&lt;ElementSecurity&gt; ess, else false
     * @param activate          : true if members should be able to activate the data types in the List&lt;ElementSecurity&gt; ess, else false
     * @param activateChanges   : Should the permissions be activated upon creation (or wait for approval later)
     * @param ess               : List of data types that this group should have permissions for
     * @param tag               Tag for permissions to key of off (typically the project ID)
     * @param authenticatedUser The user creating the group.
     *
     * @return The new or updated group.
     */
    @SuppressWarnings("unused")
    public static UserGroupI createOrUpdateGroup(final String id, final String displayName, Boolean create, Boolean read, Boolean delete, Boolean edit, Boolean activate, boolean activateChanges, List<ElementSecurity> ess, String tag, UserI authenticatedUser) throws Exception {
        return getUserGroupService().createOrUpdateGroup(id, displayName, create, read, delete, edit, activate, activateChanges, ess, tag, authenticatedUser);
    }

    /**
     * Create user group using the defined permissions.
     *
     * @param id                : String ID to use for the group ID
     * @param displayName       The display name for the group.
     * @param create            : true if members should be able to create the data types in the List&lt;ElementSecurity&gt; ess, else false
     * @param read              : true if members should be able to read the data types in the List&lt;ElementSecurity&gt; ess, else false
     * @param delete            : true if members should be able to delete the data types in the List&lt;ElementSecurity&gt; ess, else false
     * @param edit              : true if members should be able to edit the data types in the List&lt;ElementSecurity&gt; ess, else false
     * @param activate          : true if members should be able to activate the data types in the List&lt;ElementSecurity&gt; ess, else false
     * @param activateChanges   : Should the permissions be activated upon creation (or wait for approval later)
     * @param ess               : List of data types that this group should have permissions for
     * @param tag               Tag for permissions to key of off (typically the project ID)
     * @param authenticatedUser The user creating the group.
     * @param users             Users to add to the group on creation.
     *
     * @return The new or updated group.
     */
    @SuppressWarnings("unused")
    public static UserGroupI createOrUpdateGroup(final String id, final String displayName, Boolean create, Boolean read, Boolean delete, Boolean edit, Boolean activate, boolean activateChanges, List<ElementSecurity> ess, String tag, UserI authenticatedUser, final List<UserI> users) throws Exception {
        return getUserGroupService().createOrUpdateGroup(id, displayName, create, read, delete, edit, activate, activateChanges, ess, tag, authenticatedUser, users);
    }

    /**
     * Convenience method to create the standard owner, member, and collaborator groups for the specified project. Uses the project ID
     * to create the group IDs and sets the permissions and tags appropriately.
     *
     * @param projectId       The project for which the group should be created.
     * @param user            The user creating the project and groups.
     *
     * @return The newly created groups.
     *
     */
    @SuppressWarnings("unused")
    public static List<UserGroupI> createOrUpdateProjectGroups(final String projectId, final UserI user) {
        return getUserGroupService().createOrUpdateProjectGroups(projectId, user);
    }

    /**
     * Add user to the group (includes potential modification to the database).
     *
     * @param groupId           The ID of the group to which the user should be added.
     * @param user              The user to add to the group.
     * @param authenticatedUser The user adding the user to the group.
     * @param ci                The event metadata.
     *
     * @return The group with the newly added user.
     *
     * @throws Exception When an error occurs.
     */
    public static UserGroupI addUserToGroup(String groupId, UserI user, UserI authenticatedUser, EventMetaI ci) throws Exception {
        if (!isMember(user, groupId)) {
            return getUserGroupService().addUserToGroup(groupId, user, authenticatedUser, ci);
        } else {
            return getGroup(groupId);
        }
    }

    /**
     * Add a list of users to the group (includes potential modification to the database).
     *
     * @param groupId           The ID of the group to which the user should be added.
     * @param authenticatedUser The user adding the user to the group.
     * @param users             The list of users to add to the group.
     * @param eventMeta         The event metadata.
     *
     * @return The group with the newly added users.
     *
     * @throws Exception When an error occurs.
     */
    @SuppressWarnings("UnusedReturnValue")
    public static UserGroupI addUsersToGroup(final String groupId, final UserI authenticatedUser, final List<UserI> users, final EventMetaI eventMeta) throws Exception {
        return getUserGroupService().addUsersToGroup(groupId, authenticatedUser, users, eventMeta);
    }

    /**
     * return all UserGroups on this server.  (this may be an expensive operation on larger servers)  We might want to get rid of this.
     *
     * @return A list of all user groups.
     */
    public static List<UserGroupI> getAllGroups() {
        return getUserGroupService().getAllGroups();
    }

    /**
     * The data type or classification identifier used to identify the group data type.
     * <p>
     * This will be used in workflow events that are created to track modifications to user groups.
     *
     * @return The data type for user group objects.
     */
    public static String getGroupDatatype() {
        return getUserGroupService().getGroupDatatype();
    }

    /**
     * Delete the groups (and user-group mappings) associated with this tag.
     *
     * @param tag  The tag to search on.
     * @param user The user performing the delete.
     * @param ci   The event metadata.
     *
     * @throws Exception When an error occurs.
     */
    public static void deleteGroupsByTag(String tag, UserI user, EventMetaI ci) throws Exception {
        getUserGroupService().deleteGroupsByTag(tag, user, ci);
    }

    /**
     * Delete the groups (and user-group mappings) associated with this tag.
     *
     * @param group The group to be deleted.
     * @param user  The user performing the delete.
     * @param ci    The event metadata.
     *
     * @throws Exception When an error occurs.
     */
    @SuppressWarnings("RedundantThrows")
    public static void deleteGroup(UserGroupI group, UserI user, EventMetaI ci) throws Exception {
        getUserGroupService().deleteGroup(group, user, ci);
    }

    /**
     * Return a freshly created group object populated with the passed parameters. Object may or may not already exist
     * in the database.
     *
     * @param params The parameters for group creation.
     *
     * @return The newly created group.
     *
     * @throws GroupFieldMappingException When an error occurs creating the group.
     * @throws UserFieldMappingException  When an error occurs adding a user to the group.
     * @throws UserInitException          When an error occurs with the user management system.
     */
    @SuppressWarnings("RedundantThrows")
    public static UserGroupI createGroup(Map<String, ?> params) throws UserFieldMappingException, UserInitException, GroupFieldMappingException {
        return getUserGroupService().createGroup(params);
    }

    public static void save(UserGroupI tempGroup, UserI user, EventMetaI meta) throws Exception {
        getUserGroupService().save(tempGroup, user, meta);
    }

    private static UserGroupServiceI         _singleton;
    private static GroupsAndPermissionsCache _cache;
}
