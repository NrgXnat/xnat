package org.nrg.xdat.security.helpers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.UserGroupServiceI;
import org.nrg.xdat.security.group.exceptions.GroupFieldMappingException;
import org.nrg.xdat.security.user.exceptions.UserFieldMappingException;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.security.UserI;

public class Groups {
    static Logger logger = Logger.getLogger(Groups.class);


    private static UserGroupServiceI singleton = null;

    /**
     * Returns the currently configured permissions service
     * <p/>
     * You can customize the implementation returned by adding a new implementation to the org.nrg.xdat.security.user.custom package (or a diffently configured package).
     * <p/>
     * You can change the default implementation returned via the security.userManagementService.default configuration parameter
     *
     * @return
     */
    public static UserGroupServiceI getUserGroupService() {
        if (singleton == null) {
            try {
                List<Class<?>> classes = Reflection.getClassesForPackage(XDAT.safeSiteConfigProperty("security.userGroupService.package", "org.nrg.xdat.groups.custom"));

                if (classes != null && classes.size() > 0) {
                    for (Class<?> clazz : classes) {
                        if (UserGroupServiceI.class.isAssignableFrom(clazz)) {
                            singleton = (UserGroupServiceI) clazz.newInstance();
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                logger.error("", e);
            } catch (InstantiationException e) {
                logger.error("", e);
            } catch (IllegalAccessException e) {
                logger.error("", e);
            } catch (IOException e) {
                logger.error("", e);
            }

            //default to PermissionsServiceImpl implementation (unless a different default is configured)
            if (singleton == null) {
                try {
                    String className = XDAT.safeSiteConfigProperty("security.userGroupService.default", "org.nrg.xdat.security.UserGroupManager");
                    singleton = (UserGroupServiceI) Class.forName(className).newInstance();
                } catch (ClassNotFoundException e) {
                    logger.error("", e);
                } catch (InstantiationException e) {
                    logger.error("", e);
                } catch (IllegalAccessException e) {
                    logger.error("", e);
                }
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
     * @param id
     * @return
     */
    public static UserGroupI getGroup(String id) {
        return getUserGroupService().getGroup(id);
    }

    /**
     * Get the UserGroup that are currently assigned to a user.  Loads current groups from database.
     *
     * @param user
     * @return
     */
    public static Map<String, UserGroupI> getGroupsForUser(UserI user) {
        return getUserGroupService().getGroupsForUser(user);
    }

    /**
     * Get the group IDs currently assigned to a user.  Only reviews local object, groups may not be saved yet.
     *
     * @param user
     * @return
     */
    public static List<String> getGroupIdsForUser(UserI user) {
        return getUserGroupService().getGroupIdsForUser(user);
    }

    /**
     * Get the group IDs currently assigned to a user.
     *
     * @param user
     * @return
     */
    public static boolean isMember(UserI user, String grp) {
        return getUserGroupService().isMember(user, grp);
    }

    /**
     * add this group for the specified user (locally).   This will not update the database.  It will add the user to this group in local memory.
     * <p/>
     * Sometimes, a user group is associated with a user, before the user group is physically created in the database.  This method can be used to do that.
     * <p/>
     * To specifically add the user to the group permanently, use the AddUserToGroup method
     *
     * @param user
     * @param groupId
     * @param group
     */
    public static void updateUserForGroup(UserI user, String groupId, UserGroupI group) {
        getUserGroupService().updateUserForGroup(user, groupId, group);
    }

    /**
     * Remove user from the group (including updating database if necessary)
     *
     * @param user
     * @param groupId
     * @param ci
     * @throws SQLException
     * @throws Exception
     */
    public static void removeUserFromGroup(UserI user, String groupId, EventMetaI ci) throws SQLException, Exception {
        getUserGroupService().removeUserFromGroup(user, groupId, ci);
    }

    /**
     * Refresh the user group for this user (this updates any local copies of the group for this user).  This should be eliminated by a more clear caching mechanism.
     *
     * @param user
     * @param groupId
     */
    public static void reloadGroupForUser(UserI user, String groupId) {
        getUserGroupService().reloadGroupForUser(user, groupId);
    }

    /**
     * Refresh all of the user groups for this user.
     *
     * @param user
     */
    public static void reloadGroupsForUser(UserI user) {
        getUserGroupService().reloadGroupsForUser(user);
    }

    /**
     * Get groups that have the specified tag.
     *
     * @param tag
     * @return
     * @throws Exception
     */
    public static List<UserGroupI> getGroupsByTag(String tag) throws Exception {
        return getUserGroupService().getGroupsByTag(tag);
    }

    /**
     * Create user group using the defined permissions.
     *
     * @param id                : String ID to use for the group ID
     * @param displayName
     * @param create            : true if members should be able to create the data types in the List<ElementSecurity> ess, else false
     * @param read              : true if members should be able to read the data types in the List<ElementSecurity> ess, else false
     * @param delete            : true if members should be able to delete the data types in the List<ElementSecurity> ess, else false
     * @param edit              : true if members should be able to edit the data types in the List<ElementSecurity> ess, else false
     * @param activate          : true if members should be able to activate the data types in the List<ElementSecurity> ess, else false
     * @param activateChanges   : Should the permissions be activated upon creation (or wait for approval later)
     * @param ess               : List of data types that this group should have permissions for
     * @param tag               : Tag for permissions to key of off (typically the project ID)
     * @param authenticatedUser
     * @return
     */
    public static UserGroupI createGroup(final String id, final String displayName, Boolean create, Boolean read, Boolean delete, Boolean edit, Boolean activate, boolean activateChanges, List<ElementSecurity> ess, String value, UserI authenticatedUser) {
        return getUserGroupService().createGroup(id, displayName, create, read, delete, edit, activate, activateChanges, ess, value, authenticatedUser);
    }

    /**
     * Create user group using the defined permissions.
     *
     * @param id                : String ID to use for the group ID
     * @param displayName
     * @param create            : true if members should be able to create the data types in the List<ElementSecurity> ess, else false
     * @param read              : true if members should be able to read the data types in the List<ElementSecurity> ess, else false
     * @param delete            : true if members should be able to delete the data types in the List<ElementSecurity> ess, else false
     * @param edit              : true if members should be able to edit the data types in the List<ElementSecurity> ess, else false
     * @param activate          : true if members should be able to activate the data types in the List<ElementSecurity> ess, else false
     * @param activateChanges   : Should the permissions be activated upon creation (or wait for approval later)
     * @param ess               : List of data types that this group should have permissions for
     * @param tag               : Tag for permissions to key of off (typically the project ID)
     * @param authenticatedUser
     * @return
     */
    public static UserGroupI createOrUpdateGroup(final String id, final String displayName, Boolean create, Boolean read, Boolean delete, Boolean edit, Boolean activate, boolean activateChanges, List<ElementSecurity> ess, String value, UserI authenticatedUser) throws Exception {
        return getUserGroupService().createOrUpdateGroup(id, displayName, create, read, delete, edit, activate, activateChanges, ess, value, authenticatedUser);
    }

    /**
     * Add user to the group (includes potential modification to the database).
     *
     * @param group_id
     * @param newUser
     * @param authenticatedUser
     * @param ci
     * @return
     * @throws Exception
     */
    public static UserGroupI addUserToGroup(String group_id, UserI user, UserI authenticatedUser, EventMetaI ci) throws Exception {
        return getUserGroupService().addUserToGroup(group_id, user, authenticatedUser, ci);
    }

    /**
     * return all UserGroups on this server.  (this may be an expensive operation on larger servers)  We might want to get rid of this.
     *
     * @return
     */
    public static List<UserGroupI> getAllGroups() {
        return getUserGroupService().getAllGroups();
    }

    /**
     * The data type or classification identifier used to identify the group data type.
     * <p/>
     * This will be used in workflow events that are created to track modifications to user groups.
     *
     * @return
     */
    public static String getGroupDatatype() {
        return getUserGroupService().getGroupDatatype();
    }

    /**
     * Delete the groups (and user-group mappings) associated with this tag.
     *
     * @param tag
     * @param user
     * @param ci
     * @throws Exception
     */
    public static void deleteGroupsByTag(String tag, UserI user, EventMetaI ci) throws Exception {
        getUserGroupService().deleteGroupsByTag(tag, user, ci);
    }

    /**
     * Delete the groups (and user-group mappings) associated with this tag.
     *
     * @param tag
     * @param user
     * @param eventDetails
     * @throws Exception
     */
    public static void deleteGroup(UserGroupI g, UserI user, EventMetaI ci) throws Exception {
        getUserGroupService().deleteGroup(g, user, ci);
    }

    /**
     * Return a freshly created group object populated with the passed parameters.
     * <p/>
     * Object may or may not already exist in the database.
     *
     * @return
     * @throws GroupFieldMappingException
     */
    public static UserGroupI createGroup(Map<String, ? extends Object> params) throws UserFieldMappingException, UserInitException, GroupFieldMappingException {
        return getUserGroupService().createGroup(params);
    }

    public static void save(UserGroupI tempGroup, UserI user, EventMetaI meta) throws org.nrg.xdat.security.UserGroupServiceI.InvalidValueException, Exception {
        getUserGroupService().save(tempGroup, user, meta);
    }

    public static UserGroupI getGroupByPK(Object gID) {
        return getUserGroupService().getGroupByPK(gID);
    }

    public static UserGroupI getGroupByTagAndName(String pID, String gID) {
        return getUserGroupService().getGroupByTagAndName(pID, gID);
    }
}
