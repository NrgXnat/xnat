package org.nrg.xdat.services.cache;

import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.List;

public interface UserItemCache<T extends ItemI> extends XnatCache {
    /**
     * Indicates whether the specified item ID or alias is already cached.
     *
     * @param idOrAlias The ID or alias of the item to check.
     *
     * @return Returns true if the ID or alias is mapped to an item cache entry, false otherwise.
     */
    boolean has(final String idOrAlias);

    /**
     * Indicates whether permissions for the specified user in the specified item ID or alias is already cached.
     *
     * @param user      The user object for the user requesting the item.
     * @param idOrAlias The ID or alias of the item to check.
     *
     * @return Returns true if the user is mapped to a cache entry for the cached item ID or alias, false otherwise.
     */
    boolean has(final UserI user, final String idOrAlias);

    /**
     * Indicates whether the specified user can delete the item identified by the specified ID or alias. Note that this returns false if
     * the item can't be found by the specified ID or alias or the username can't be located.
     *
     * @param userId    The username of the user to test.
     * @param idOrAlias The ID or an alias of the item to be tested.
     *
     * @return Returns true if the user can delete the specified item or false otherwise.
     */
    boolean canDelete(final String userId, final String idOrAlias);

    /**
     * Indicates whether the specified user can write to the item identified by the specified ID or alias. Note that this returns false if
     * the item can't be found by the specified ID or alias or the username can't be located.
     *
     * @param userId    The username of the user to test.
     * @param idOrAlias The ID or an alias of the item to be tested.
     *
     * @return Returns true if the user can write to the specified item or false otherwise.
     */
    @SuppressWarnings("unused")
    boolean canWrite(final String userId, final String idOrAlias);

    /**
     * Indicates whether the specified user can read from the item identified by the specified ID or alias. Note that this returns false if
     * the item can't be found by the specified ID or alias or the username can't be located.
     *
     * @param userId    The username of the user to test.
     * @param idOrAlias The ID or an alias of the item to be tested.
     *
     * @return Returns true if the user can read from the specified item or false otherwise.
     */
    boolean canRead(final String userId, final String idOrAlias);

    /**
     * Gets the specified item if the user has any access to it. Returns null otherwise.
     *
     * @param user      The user trying to access the item.
     * @param idOrAlias The ID or alias of the item to retrieve.
     *
     * @return The item object if the user can access it, null otherwise.
     */
    T get(final UserI user, final String idOrAlias);

    /**
     * Gets all items of the parameterized type to which the user has access.
     *
     * @param user The user trying to access the items.
     *
     * @return All list of all items of the parameterized type to which the user has access.
     */
    List<T> getAll(final UserI user);

    /**
     * Gets all items where the value of the specified field matches the value submitted to this method.
     *
     * @param user  The user trying to retrieve items.
     * @param field The field to be queried.
     * @param value The value to match for the specified field.
     *
     * @return One or more items that match the specified value.
     */
    @SuppressWarnings("unused")
    List<T> getByField(final UserI user, final String field, final String value);
}
