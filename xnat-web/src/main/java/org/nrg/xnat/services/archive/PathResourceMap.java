/*
 * web: org.nrg.xnat.services.archive.PathResourceMapper
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.archive;

import java.util.Iterator;

/**
 * Provides the interface for returning result sets that map paths to resources. The generic version of this allows the
 * path and resource to be in any form useful for a particular scenario.
 *
 * @param <P> The data type to be used for the path.
 * @param <R> The data type to be used for the resource.
 */
public interface PathResourceMap<P, R> extends Iterator<PathResourceMap.Mapping<P, R>> {
    /**
     * Indicates how many resources have been processed.
     *
     * @return The total number of resources that have been processed.
     */
    long getProcessedCount();

    /**
     * Provides the duple interface for the path-resource association.
     */
    interface Mapping<P, R> {
        P getPath();
        R getResource();
    }
}
