package org.nrg.xdat.services.cache;

import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

public interface UserProjectCache<T extends ItemI> extends XnatCache {
    /**
     * Indicates whether the specified project ID or alias is already cached.
     *
     * @param idOrAlias The ID or alias of the project to check.
     *
     * @return Returns true if the ID or alias is mapped to a project cache entry, false otherwise.
     */
    boolean has(final String idOrAlias);

    /**
     * Indicates whether permissions for the specified user in the specified project ID or alias is already cached.
     *
     * @param user      The user object for the user requesting the project.
     * @param idOrAlias The ID or alias of the project to check.
     *
     * @return Returns true if the user is mapped to a cache entry for the cached project ID or alias, false otherwise.
     */
    boolean has(final UserI user, final String idOrAlias);

    /**
     * Indicates whether the specified user can delete the project identified by the specified ID or alias. Note that this returns false if
     * the project can't be found by the specified ID or alias or the username can't be located.
     *
     * @param userId    The username of the user to test.
     * @param idOrAlias The ID or an alias of the project to be tested.
     *
     * @return Returns true if the user can delete the specified project or false otherwise.
     */
    boolean canDelete(final String userId, final String idOrAlias);

    /**
     * Indicates whether the specified user can write to the project identified by the specified ID or alias. Note that this returns false if
     * the project can't be found by the specified ID or alias or the username can't be located.
     *
     * @param userId    The username of the user to test.
     * @param idOrAlias The ID or an alias of the project to be tested.
     *
     * @return Returns true if the user can write to the specified project or false otherwise.
     */
    @SuppressWarnings("unused")
    boolean canWrite(final String userId, final String idOrAlias);

    /**
     * Indicates whether the specified user can read from the project identified by the specified ID or alias. Note that this returns false if
     * the project can't be found by the specified ID or alias or the username can't be located.
     *
     * @param userId    The username of the user to test.
     * @param idOrAlias The ID or an alias of the project to be tested.
     *
     * @return Returns true if the user can read from the specified project or false otherwise.
     */
    boolean canRead(final String userId, final String idOrAlias);

    /**
     * Gets the specified project if the user has any access to it. Returns null otherwise.
     *
     * @param user      The user trying to access the project.
     * @param idOrAlias The ID or alias of the project to retrieve.
     *
     * @return The project object if the user can access it, null otherwise.
     */
    T get(final UserI user, final String idOrAlias);
}
