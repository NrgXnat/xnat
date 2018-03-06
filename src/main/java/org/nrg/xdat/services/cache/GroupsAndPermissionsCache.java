package org.nrg.xdat.services.cache;

import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.security.UserI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GroupsAndPermissionsCache extends XnatCache {
    String CACHE_NAME = "GroupsAndPermissionsCache";

    Map<String, ElementDisplay> getBrowseableElementDisplays(final String username);

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
         * Accessor for time/date cache initialization was completed.
         *
         * @return The completion time/date.
         */
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
     * Indicates whether a group with the specified ID is already cached. Note that this doesn't check whether the
     * group exists on the system, just whether it exists in the cache. Even calls for group IDs that don't exist
     * on the system may return true: if the non-existent group has already been requested and found to not exist,
     * a string is cached that indicates that the group doesn't exist.
     *
     * @param groupId The ID of the group to check.
     *
     * @return Returns true if the group exists in the cache, false otherwise.
     */
    boolean has(final String groupId);

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
     * Returns the group with the submitted tag for the user.
     *
     * @param username The name of the user.
     * @param tag      The tag to retrieve.
     *
     * @return The group with the associated tag and user.
     */
    UserGroupI getGroupForUserAndTag(final String username, final String tag) throws UserNotFoundException;

    /**
     * Returns the timestamp indicating when the specified group was last updated. If the group was only
     * inserted and not updated, the insert time is returned.
     *
     * @param groupId The ID of the group to check.
     *
     * @return The date and time of the latest update to the specified group.
     */
    Date getLastUpdateTime(final String groupId);

    /**
     * Returns the timestamp for the most recently updated group associated with the indicated user.
     *
     * @param user The user to test.
     *
     * @return The date and time of the latest update to any groups associated with the specified user.
     */
    Date getLastUpdateTime(final UserI user);

    /**
     * This method retrieves the group with the specified group ID and puts it in the cache. This method
     * does <i>not</i> check to see if the group is already in the cache! This is primarily for use during
     * cache initialization and shouldn't be used for routine access.
     *
     * @param groupId The ID or alias of the group to retrieve.
     *
     * @return The group object for the specified ID if it exists, null otherwise.
     */
    UserGroupI cacheGroup(String groupId);
}
