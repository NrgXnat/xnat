package org.nrg.xdat.services.cache;

import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.security.PermissionCriteriaI;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.ItemNotFoundException;
import org.nrg.xft.security.UserI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static org.nrg.xdat.security.SecurityManager.*;

public interface GroupsAndPermissionsCache extends XnatCache {
    String       CACHE_NAME                = "GroupsAndPermissionsCache";
    List<String> ACTIONS                   = Arrays.asList(READ, EDIT, CREATE);
    String       QUERY_GET_GROUPS_FOR_USER = "SELECT " +
                                             "    g.groupid " +
                                             "FROM " +
                                             "    xdat_user_groupid g " +
                                             "    LEFT JOIN xdat_user u ON g.groups_groupid_xdat_user_xdat_user_id = u.xdat_user_id " +
                                             "WHERE " +
                                             "    g.groupid IS NOT NULL AND " +
                                             "    u.login = :username " +
                                             "ORDER BY " +
                                             "    g.groupid";

    interface Listener {
        /**
         * Returns a set containing the IDs of the groups that have not yet been cached.
         *
         * @return A set of group IDs for groups that have not yet been cached.
         */
        Set<String> getUnprocessed();

        /**
         * Returns a set containing the IDs of the groups that have been cached.
         *
         * @return A set of group IDs for groups that have been cached.
         */
        Set<String> getProcessed();

        /**
         * Accessor for time/date cache initialization was started.
         *
         * @return The start time/date.
         */
        Date getStart();

        /**
         * Accessor for time/date cache initialization was completed. This returns null
         * if initialization has not yet completed.
         *
         * @return The completion time/date.
         */
        @Nullable
        Date getCompleted();

        /**
         * Sets the list of group IDs to be processed. This should only be called by the cache
         * with which the listener is registered.
         *
         * @param groupIds The list of group IDs to be processed.
         */
        void setGroupIds(final List<String> groupIds);
    }

    interface Provider {
        void registerListener(final Listener listener);

        Listener getListener();
    }

    /**
     * Gets the specified group if it exists. If the group is stored in the cache, the cached group is returned. If
     * not, this tries to retrieve the group from the system. If the group exists, it's cached and returned. Otherwise
     * null is returned.
     *
     * @param groupId The ID of the group to retrieve.
     *
     * @return The group object if it exists, null otherwise.
     */
    @Nullable
    UserGroupI get(final String groupId);

    /**
     * Get the readable counts for the indicated user.
     *
     * @param user The user to retrieve readable counts for.
     *
     * @return The readable counts for the indicated user.
     */
    Map<String, Long> getReadableCounts(final UserI user);

    /**
     * Get the browseable element displays for the indicated user.
     *
     * @param user The user to retrieve browseable element displays for.
     *
     * @return The browseable element displays for the indicated user.
     */
    Map<String, ElementDisplay> getBrowseableElementDisplays(final UserI user);

    /**
     * Get the searchable element displays for the indicated user.
     *
     * @param user The user for which to retrieve action element displays.
     *
     * @return The searchable element displays for the indicated user.
     */
    List<ElementDisplay> getSearchableElementDisplays(final UserI user) throws Exception;

    /**
     * Get the action element displays for the indicated user.
     *
     * @param user   The user for which to retrieve action element displays.
     * @param action The action for which to retrieve action element displays.
     *
     * @return The action element displays for the indicated user.
     */
    List<ElementDisplay> getActionElementDisplays(final UserI user, final String action) throws Exception;

    List<ElementDisplay> getActionElementDisplays(String username, String action);

    /**
     * Get the permission criteria for the indicated user.
     *
     * @param user     The user for which to retrieve permission criteria.
     * @param dataType The data type for which to retrieve permission criteria.
     *
     * @return The permission criteria for the indicated user.
     */
    List<PermissionCriteriaI> getPermissionCriteria(final UserI user, final String dataType);

    /**
     * Get the permission criteria for the indicated user.
     *
     * @param username The user for which to retrieve permission criteria.
     * @param dataType The data type for which to retrieve permission criteria.
     *
     * @return The permission criteria for the indicated user.
     */
    List<PermissionCriteriaI> getPermissionCriteria(final String username, String dataType);

    /**
     * Gets the total count of instances of data types in the system.
     *
     * @return A map of data types with the number of instances of each data type.
     */
    Map<String, Long> getTotalCounts();

    /**
     * Gets the projects for which the user has the indicated permission. Returns an empty list if the user doesn't
     * exist on the system.
     *
     * @param username The name of the user.
     * @param access   The required access level.
     *
     * @return The IDs of the projects for which the user has the indicated access level.
     */
    @Nonnull
    List<String> getProjectsForUser(final String username, final String access);

    /**
     * Gets the groups for the specified tag (usually maps directly to the project ID). Returns an empty list if the
     * tag doesn't exist on the system.
     *
     * @param tag The tag to retrieve.
     *
     * @return The groups for the tag, empty list otherwise.
     */
    @Nonnull
    List<UserGroupI> getGroupsForTag(final String tag);

    /**
     * Gets the groups for the user with the specified username. Returns an empty map if no groups are found for the
     * user. Throws {@link UserNotFoundException} if the user doesn't exist on the system.
     *
     * @param username The username to retrieve groups for.
     *
     * @return The groups for the user, empty list otherwise.
     *
     * @throws UserNotFoundException When a user with the indicated username doesn't exist on the system.
     */
    @Nonnull
    Map<String, UserGroupI> getGroupsForUser(final String username) throws UserNotFoundException;

    /**
     * Refreshes the user's group list.
     *
     * @param username The user whose group list should be refreshed.
     */
    void refreshGroupsForUser(final String username) throws UserNotFoundException;

    /**
     * Returns the group with the submitted tag for the user.
     *
     * @param username The name of the user.
     * @param tag      The tag to retrieve.
     *
     * @return The group with the associated tag and user.
     */
    UserGroupI getGroupForUserAndTag(final String username, final String tag) throws UserNotFoundException;

    /**
     * Gets a list of all of the user IDs associated with the specified group.
     *
     * @param groupId The ID of the group to retrieve.
     *
     * @return A list of all user IDs associated with the group.
     */
    List<String> getUserIdsForGroup(String groupId);

    /**
     * Refreshes the user's group list.
     *
     * @param groupId The group whose list of users should be refreshed.
     */
    void refreshGroup(final String groupId) throws ItemNotFoundException;


    /**
     * Returns the timestamp for the most recently updated group associated with the indicated user.
     *
     * @param user The user to test.
     *
     * @return The date and time of the latest update to any groups associated with the specified user.
     */
    Date getUserLastUpdateTime(final UserI user);

    /**
     * Returns the timestamp for the most recently updated group associated with the indicated user.
     *
     * @param username The name of the user to test.
     *
     * @return The date and time of the latest update to any groups associated with the specified user.
     */
    Date getUserLastUpdateTime(final String username);

    /**
     * Clears all cache entries for the indicated user.
     *
     * @param username The name of the user whose cache should be cleared.
     */
    void clearUserCache(final String username);

    /**
     * Resets the overall system counts (expensive on uber-large servers)
     */
    void resetTotalCounts();

    /**
     * Get the projects (id and secondary_id) that this user has permissions to modify data of the given type
     * @param user
     * @param dataType
     * @return
     * @throws ElementNotFoundException
     */
    XFTTable getProjectsForDatatypeAction(final UserI user, final String dataType, final String action) throws Exception;
}
