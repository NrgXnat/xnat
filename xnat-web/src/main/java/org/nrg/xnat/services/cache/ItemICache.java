package org.nrg.xnat.services.cache;

import org.nrg.xdat.services.cache.XnatCache;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.List;
import java.util.Optional;

/**
 * A simple cache of {@link ItemI} objects stored by a composite key comprising the XSI type and ID. This cache doesn't
 * check user permissions and
 */
public interface ItemICache extends XnatCache {
    /**
     * Gets the specified item if an item of the specified type and ID exists.
     *
     * @param xsiType The type of the item to retrieve
     * @param itemId  The ID of the item to retrieve
     *
     * @return The specified item if it exists, empty otherwise.
     */
    Optional<ItemI> get(final String xsiType, final String itemId);

    /**
     * Gets all items of the specified type to which the user has access.
     *
     * @param user    The user trying to retrieve objects
     * @param xsiType The type of the items to retrieve
     *
     * @return A list of all items of the specified type.
     */
    List<ItemI> getAll(final String xsiType);

    /**
     * Clears the cache entry for the project with the specified ID or alias. This is necessary in some cases
     * when operations may cause a race condition before a cache entry can be updated.
     *
     * @param xsiType The type of item to clear
     * @param itemId  The ID of the item to clear
     */
    void clear(final String xsiType, final String itemId);
}
