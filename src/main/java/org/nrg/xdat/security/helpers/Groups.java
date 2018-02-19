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
import org.apache.ecs.xhtml.meta;
import org.nrg.framework.services.ContextService;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.UserGroupServiceI;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.group.exceptions.GroupFieldMappingException;
import org.nrg.xdat.security.user.exceptions.UserFieldMappingException;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import reactor.bus.Event;
import reactor.fn.Predicate;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
public class Groups {
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
                final String className = XDAT.safeSiteConfigProperty("security.userGroupService.default", "org.nrg.xdat.security.UserGroupManager");
                return _singleton = (UserGroupServiceI) Class.forName(className).newInstance();
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
     * Tests whether an {@link XftItemEvent} is related to an {@link XdatUsergroup} object. Note that this doesn't test
     * for a <i>particular</i> group, just whether the event was related to a group.
     */
    public static final Predicate<Object> IS_GROUP_XFTITEM_EVENT = new Predicate<Object>() {
        @Override
        public boolean test(final Object object) {
            return object instanceof XftItemEvent && isXdatUsergroupEvent((XftItemEvent) object);
        }
    };

    /**
     * Indicates whether the event is related to an {@link XdatUsergroup} object. This method returns true if the
     * {@link Event#getData()} event item} is an instance of {@link XftItemEvent} and that event is related to an
     * {@link XdatUsergroup} object, as determined by calling {@link #isXdatUsergroupEvent(XftItemEvent)}.
     *
     * @param event The event to be evaluated.
     *
     * @return Returns true if the event is related to an {@link XdatUsergroup} object.
     *
     * @see #isXdatUsergroupEvent(XftItemEvent)
     * @see #IS_GROUP_XFTITEM_EVENT
     */
    public static boolean isXdatUsergroupEvent(@Nonnull final Event event) {
        final Object data = event.getData();
        return !(data instanceof XftItemEvent) || isXdatUsergroupEvent((XftItemEvent) data);
    }

    /**
     * Indicates whether the event is related to an {@link XdatUsergroup} object. This method returns true if the
     * {@link XftItemEvent} {@link XftItemEvent#getItem() is related to an} {@link XdatUsergroup} object.
     *
     * @param event The event to be evaluated.
     *
     * @return Returns true if the event is related to an {@link XdatUsergroup} object.
     *
     */
    public static boolean isXdatUsergroupEvent(@Nonnull final XftItemEvent event) {
        final Object item = event.getItem();
        try {
            return (item instanceof XFTItem && ((XFTItem) item).instanceOf(XdatUsergroup.SCHEMA_ELEMENT_NAME)) || XdatUsergroup.class.isAssignableFrom(item.getClass());
        } catch (ElementNotFoundException ignored) {
            return false;
        }
    }

    /**
     * Convenience method to determine whether the user is a system data administrator.
     *
     * @param user The user to test for system data administrator access.
     * @return Returns true if the user is a system data administrator, false otherwise.
     */
    public static boolean isDataAdmin(final UserI user) {
        return Groups.isMember(user, "ALL_DATA_ADMIN");
    }

    public static boolean hasAllDataAccess(final UserI user) {
        return isDataAdmin(user) || Groups.isMember(user, "ALL_DATA_ACCESS");
    }

    /**
     * Get a UserGroupI by the group ID.
     *
     * @param groupId    The ID of the group to which the user should be added.
     *
     * @return The group with the indicated ID.
     */
    public static UserGroupI getGroup(String groupId) {
        return getUserGroupService().getGroup(groupId);
    }

    /**
     * Get the UserGroup that are currently assigned to a user.  Loads current groups from database.
     *
     * @param user    The user on which to search.
     *
     * @return A map of groups to which the user is assigned.
     */
    public static Map<String, UserGroupI> getGroupsForUser(UserI user) {
        return getUserGroupService().getGroupsForUser(user);
    }

    /**
     * Get the group IDs currently assigned to a user.  Only reviews local object, groups may not be saved yet.
     *
     * @param user    The user on which to search.
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
     * @return Returns true if the user is a member of the indicated group, false otherwise.
     */
    public static boolean isMember(UserI user, String groupId) {
        return getUserGroupService().isMember(user, groupId);
    }

    /**
     * Add this group for the specified user (locally).   This will not update the database.  It will add the user to this group in local memory.
     *
     * Sometimes, a user group is associated with a user, before the user group is physically created in the database.  This method can be used to do that.
     *
     * To specifically add the user to the group permanently, use the AddUserToGroup method
     *
     * @param user       The user on which to search.
     * @param groupId    The group ID on which to search.
     * @param group      The group to be updated.
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
     * @throws Exception
     */
    public static void removeUserFromGroup(UserI user, UserI authenticatedUser, String groupId, EventMetaI ci) throws Exception {
        getUserGroupService().removeUserFromGroup(user, authenticatedUser, groupId, ci);
    }

    /**
     * Refresh the user group for this user (this updates any local copies of the group for this user).  This should be eliminated by a more clear caching mechanism.
     *
     * @param user       The user to be refreshed.
     * @param groupId    The group ID on which to search.
     */
    public static void reloadGroupForUser(UserI user, String groupId) {
        getUserGroupService().reloadGroupForUser(user, groupId);
    }

    /**
     * Refresh all of the user groups for this user.
     *
     * @param user    The user to be refreshed.
     */
    public static void reloadGroupsForUser(UserI user) {
        getUserGroupService().reloadGroupsForUser(user);
    }

    /**
     * Get groups that have the specified tag.
     *
     * @param tag    The tag to search on.
     *
     * @return A list of the groups with the indicated tag.
     *
     * @throws Exception
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
    public static UserGroupI getGroupsByUserAndTag(final UserI user, final String tag) {
        return null;
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
     * @param authenticatedUser The user creating the group.
     *
     * @return The new or updated group.
     */
    public static UserGroupI createOrUpdateGroup(final String id, final String displayName, Boolean create, Boolean read, Boolean delete, Boolean edit, Boolean activate, boolean activateChanges, List<ElementSecurity> ess, String value, UserI authenticatedUser) throws Exception {
        return getUserGroupService().createOrUpdateGroup(id, displayName, create, read, delete, edit, activate, activateChanges, ess, value, authenticatedUser);
    }

    /**
     * Add user to the group (includes potential modification to the database).
     *
     * @param groupId              The ID of the group to which the user should be added.
     * @param user                 The user to add to the group.
     * @param authenticatedUser    The user adding the user to the group.
     * @param ci                   The event metadata.
     *
     * @return The group with the newly added user.
     * @throws Exception
     */
    public static UserGroupI addUserToGroup(String groupId, UserI user, UserI authenticatedUser, EventMetaI ci) throws Exception {
        return getUserGroupService().addUserToGroup(groupId, user, authenticatedUser, ci);
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
     *
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
     * @param tag     The tag to search on.
     * @param user    The user performing the delete.
     * @param ci      The event metadata.
     * @throws Exception
     */
    public static void deleteGroupsByTag(String tag, UserI user, EventMetaI ci) throws Exception {
        getUserGroupService().deleteGroupsByTag(tag, user, ci);
    }

    /**
     * Delete the groups (and user-group mappings) associated with this tag.
     *
     * @param group    The group to be deleted.
     * @param user     The user performing the delete.
     * @param ci       The event metadata.
     *
     * @throws Exception
     */
    public static void deleteGroup(UserGroupI group, UserI user, EventMetaI ci) throws Exception {
        getUserGroupService().deleteGroup(group, user, ci);
    }

    /**
     * Return a freshly created group object populated with the passed parameters. Object may or may not already exist
     * in the database.
     *
     * @param params    The parameters for group creation.
     *
     * @return The newly created group.
     * @throws GroupFieldMappingException
     */
    public static UserGroupI createGroup(Map<String, ?> params) throws UserFieldMappingException, UserInitException, GroupFieldMappingException {
        return getUserGroupService().createGroup(params);
    }

    public static void save(UserGroupI tempGroup, UserI user, EventMetaI meta) throws Exception {
        getUserGroupService().save(tempGroup, user, meta);
    }

    private static UserGroupServiceI         _singleton;
    private static GroupsAndPermissionsCache _cache;
}
