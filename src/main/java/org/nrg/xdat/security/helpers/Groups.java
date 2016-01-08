package org.nrg.xdat.security.helpers;

import org.nrg.framework.services.ContextService;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.UserGroupServiceI;
import org.nrg.xdat.security.group.exceptions.GroupFieldMappingException;
import org.nrg.xdat.security.user.exceptions.UserFieldMappingException;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Groups {
    private static final Logger logger = LoggerFactory.getLogger(Groups.class);


    private static UserGroupServiceI singleton = null;

    /**
     * Returns the currently configured permissions service. You can customize the implementation returned by adding a
     * new implementation to the org.nrg.xdat.security.user.custom package (or a differently configured package). Change
     * the default implementation returned via the security.userManagementService.default configuration parameter.
     *
     * @return An instance of the {@link UserGroupServiceI user group service}.
     */
    public static UserGroupServiceI getUserGroupService() {
        // MIGRATION: All of these services need to switch from having the implementation in the prefs service to autowiring from the context.
        if (singleton == null) {
            // First find out if it exists in the application context.
            final ContextService contextService = XDAT.getContextService();
            if (contextService != null) {
                try {
                    return singleton = contextService.getBean(UserGroupServiceI.class);
                } catch (NoSuchBeanDefinitionException ignored) {
                    // This is OK, we'll just create it from the indicated class.
                }
            }
            try {
                List<Class<?>> classes = Reflection.getClassesForPackage(XDAT.safeSiteConfigProperty("security.userGroupService.package", "org.nrg.xdat.groups.custom"));

                if (classes != null && classes.size() > 0) {
                    for (Class<?> clazz : classes) {
                        if (UserGroupServiceI.class.isAssignableFrom(clazz)) {
                            return singleton = (UserGroupServiceI) clazz.newInstance();
                        }
                    }
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException e) {
                logger.error("", e);
            }

            //default to PermissionsServiceImpl implementation (unless a different default is configured)
            try {
                final String className = XDAT.safeSiteConfigProperty("security.userGroupService.default", "org.nrg.xdat.security.UserGroupManager");
                return singleton = (UserGroupServiceI) Class.forName(className).newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                logger.error("", e);
            }
        }
        return singleton;
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
     * <p/>
     * Sometimes, a user group is associated with a user, before the user group is physically created in the database.  This method can be used to do that.
     * <p/>
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
     * @param user       The user to remove from the group.
     * @param groupId    The ID of the group from which the user should be removed.
     * @param ci         The event metadata.
     * @throws Exception
     */
    public static void removeUserFromGroup(UserI user, String groupId, EventMetaI ci) throws Exception {
        getUserGroupService().removeUserFromGroup(user, groupId, ci);
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
     * Create user group using the defined permissions.
     *
     * @param id                : String ID to use for the group ID
     * @param displayName       The display name for the group.
     * @param create            : true if members should be able to create the data types in the List<ElementSecurity> ess, else false
     * @param read              : true if members should be able to read the data types in the List<ElementSecurity> ess, else false
     * @param delete            : true if members should be able to delete the data types in the List<ElementSecurity> ess, else false
     * @param edit              : true if members should be able to edit the data types in the List<ElementSecurity> ess, else false
     * @param activate          : true if members should be able to activate the data types in the List<ElementSecurity> ess, else false
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
     * @param create            : true if members should be able to create the data types in the List<ElementSecurity> ess, else false
     * @param read              : true if members should be able to read the data types in the List<ElementSecurity> ess, else false
     * @param delete            : true if members should be able to delete the data types in the List<ElementSecurity> ess, else false
     * @param edit              : true if members should be able to edit the data types in the List<ElementSecurity> ess, else false
     * @param activate          : true if members should be able to activate the data types in the List<ElementSecurity> ess, else false
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
     * <p/>
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

    public static UserGroupI getGroupByPK(Object gID) {
        return getUserGroupService().getGroupByPK(gID);
    }

    public static UserGroupI getGroupByTagAndName(String pID, String gID) {
        return getUserGroupService().getGroupByTagAndName(pID, gID);
    }
}
