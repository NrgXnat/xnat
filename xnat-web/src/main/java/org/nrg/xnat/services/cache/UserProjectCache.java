package org.nrg.xnat.services.cache;

import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.services.cache.UserItemCache;
import org.nrg.xft.security.UserI;

import java.util.List;

/**
 * Extends the item cache interface to define it specifically for the project class.
 */
public interface UserProjectCache extends UserItemCache<XnatProjectdata> {
    /**
     * Indicates whether the specified project ID or alias is already cached.
     *
     * @param idOrAlias The ID or alias of the project to check.
     *
     * @return Returns true if the ID or alias is mapped to an project cache entry, false otherwise.
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
    XnatProjectdata get(final UserI user, final String idOrAlias);

    /**
     * Gets all projects of the parameterized type to which the user has access.
     *
     * @param user The user trying to access the projects.
     *
     * @return All list of all projects of the parameterized type to which the user has access.
     */
    List<XnatProjectdata> getAll(final UserI user);

    /**
     * Gets all projects where the value of the specified field matches the value submitted to this method.
     *
     * @param user  The user trying to retrieve projects.
     * @param field The field to be queried.
     * @param value The value to match for the specified field.
     *
     * @return One or more projects that match the specified value.
     */
    List<XnatProjectdata> getByField(final UserI user, final String field, final String value);
}
