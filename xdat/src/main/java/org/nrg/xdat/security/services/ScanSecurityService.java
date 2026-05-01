package org.nrg.xdat.security.services;

import org.nrg.xft.ItemI;
import org.nrg.xft.exception.XftItemException;

import javax.annotation.Nullable;

/**
 * This entire interface will not be needed when we fully support scan security (XXX-187)
 */
public interface ScanSecurityService {
    /**
     * Determine parent session of scan in order to check access to it. If item is not a scan, return null.
     *
     * @param item scan item
     * @return the parent session or null if item is not a scan
     * @throws XftItemException if parent session could not be determined
     */
    @Nullable
    ItemI determineSession(ItemI item) throws XftItemException;
}
