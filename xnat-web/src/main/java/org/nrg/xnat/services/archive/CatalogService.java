/*
 * web: org.nrg.xnat.services.archive.CatalogService
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.archive;

import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xft.security.UserI;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Defines the service that maintains and manages the XNAT archive catalog.
 */
public interface CatalogService {
    /**
     * Specifies an operation to be performed during a {@link #refreshResourceCatalogs(UserI, List, Operation...) catalog
     * refresh}.
     */
    enum Operation {
        All,
        Append,
        Checksum,
        Delete,
        PopulateStats;

        public static final List<Operation> ALL = Arrays.asList(Operation.values());
    }

    /**
     * Refreshes the catalog for the specified resource. The resource should be identified by standard archive-relative
     * paths, e.g.:
     *
     * <pre>
     * {@code
     * /archive/experiments/XNAT_E0001
     * /archive/projects/XNAT_01/subjects/XNAT_01_01
     * }
     * </pre>
     *
     * @param user       The user performing the refresh operation.
     * @param resource   The path to the resource to be refreshed.
     * @param operations One or more operations to perform. If no operations are specified, {@link Operation#All} is
     *                   presumed.
     *
     * @throws ClientException When an error occurs that is caused somehow by the requested operation.
     * @throws ServerException When an error occurs in the system during the refresh operation.
     */
    void refreshResourceCatalog(final UserI user, final String resource, final Operation... operations) throws ServerException, ClientException;

    /**
     * Refreshes the catalog for the specified resource. The resource should be identified by standard archive-relative
     * paths, e.g.:
     *
     * <pre>
     * {@code
     * /archive/experiments/XNAT_E0001
     * /archive/projects/XNAT_01/subjects/XNAT_01_01
     * }
     * </pre>
     *
     * @param user       The user performing the refresh operation.
     * @param resource   The path to the resource to be refreshed.
     * @param operations One or more operations to perform. If no operations are specified, {@link Operation#All} is
     *                   presumed.
     *
     * @throws ClientException When an error occurs that is caused somehow by the requested operation.
     * @throws ServerException When an error occurs in the system during the refresh operation.
     */
    void refreshResourceCatalog(final UserI user, final String resource, final Collection<Operation> operations) throws ServerException, ClientException;

    /**
     * Refreshes the catalog for the specified resources. The resources should be identified by
     * standard archive-relative paths, e.g.:
     *
     * <pre>
     * {@code
     * /archive/experiments/XNAT_E0001
     * /archive/projects/XNAT_01/subjects/XNAT_01_01
     * }
     * </pre>
     *
     * @param user       The user performing the refresh operation.
     * @param resources  The paths to the resources to be refreshed.
     * @param operations One or more operations to perform. If no operations are specified, {@link Operation#All} is
     *                   presumed.
     *
     * @throws ClientException When an error occurs that is caused somehow by the requested operation.
     * @throws ServerException When an error occurs in the system during the refresh operation.
     */
    void refreshResourceCatalogs(final UserI user, final List<String> resources, final Operation... operations) throws ServerException, ClientException;

    /**
     * Refreshes the catalog for the specified resources. The resources should be identified by
     * standard archive-relative paths, e.g.:
     *
     * <pre>
     * {@code
     * /archive/experiments/XNAT_E0001
     * /archive/projects/XNAT_01/subjects/XNAT_01_01
     * }
     * </pre>
     *
     * @param user       The user performing the refresh operation.
     * @param resources  The paths to the resources to be refreshed.
     * @param operations One or more operations to perform. If no operations are specified, {@link Operation#All} is
     *                   presumed.
     *
     * @throws ClientException When an error occurs that is caused somehow by the requested operation.
     * @throws ServerException When an error occurs in the system during the refresh operation.
     */
    void refreshResourceCatalogs(final UserI user, final List<String> resources, final Collection<Operation> operations) throws ServerException, ClientException;
}
