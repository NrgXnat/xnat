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
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
     * Creates a new resource catalog with the indicated attributes. The new resource catalog is not associated with any
     * particular resource or entity on the system, is not persisted to the database, and doesn't have any related files
     * in the archive. To store the catalog to the system, you can use the {@link #insertResourceCatalog(UserI, String,
     * XnatResourcecatalog, EventMetaI, Map)} or {@link #insertResourceCatalog(UserI, String, XnatResourcecatalog,
     * EventMetaI)} methods.
     *
     * @param user        The user creating the resource catalog.
     * @param label       The label for the new resource.
     * @param description The description of the new resource.
     * @param format      The format of the new resource.
     * @param content     The content type of the new resource.
     * @param tags        One or more tags for the new resource.
     *
     * @return The newly created resource catalog.
     *
     * @throws Exception Thrown when an error occurs at some stage of creating the resource catalog.
     */
    XnatResourcecatalog createResourceCatalog(final UserI user, final String label, final String description, final String format, final String content, final String... tags) throws Exception;

    /**
     * Inserts the resource catalog into the resource specified by the parent URI parameter. If you need to pass
     * parameters into the insert function, you should use the {@link #insertResourceCatalog(UserI, String,
     * XnatResourcecatalog, EventMetaI, Map)} version of this method.
     *
     * @param user        The user creating the resource catalog.
     * @param parentUri   The URI for the resource parent.
     * @param catalog     The catalog object to insert.
     * @param event       The event meta data to process.
     *
     * @return The newly inserted resource catalog.
     *
     * @throws Exception Thrown when an error occurs at some stage of creating the resource catalog.
     */
    XnatResourcecatalog insertResourceCatalog(final UserI user, final String parentUri, final XnatResourcecatalog catalog, final EventMetaI event) throws Exception;

    /**
     * Inserts the resource catalog into the resource specified by the parent URI parameter.
     *
     * @param user        The user creating the resource catalog.
     * @param parentUri   The URI for the resource parent.
     * @param parameters  One or more parameters to be passed into the create method.
     * @param catalog     The catalog object to insert.
     * @param event       The event meta data to process.
     *
     * @return The newly inserted resource catalog.
     *
     * @throws Exception Thrown when an error occurs at some stage of creating the resource catalog.
     */
    XnatResourcecatalog insertResourceCatalog(final UserI user, final String parentUri, final XnatResourcecatalog catalog, final EventMetaI event, final Map<String, String> parameters) throws Exception;

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
