package org.nrg.xdat.services.cache;

import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public interface GroupsAndPermissionsCache extends XnatCache {
    String CACHE_NAME = "GroupsAndPermissionsCache";

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
}
