/*
 * web: org.nrg.xnat.services.archive.CatalogService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.archive;

import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xft.security.UserI;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
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
     * Creates a catalog containing references for the objects specified in the submitted resource map. The map can
     * contain the following keys:
     *
     * <ul>
     * <li>sessions</li>
     * <li>scan_type</li>
     * <li>scan_format</li>
     * <li>recon</li>
     * <li>assessors</li>
     * <li>resources</li>
     * </ul>
     *
     * Each key can reference a list containing one or more data object IDs. This function returns the ID of the newly
     * created catalog. You can retrieve the catalog itself by calling {@link #getResourcesForCatalog(UserI, String)}.
     *
     * @param user      The user requesting the resources.
     * @param resources The resources to be included in the catalog.
     *
     * @return The ID of the newly created catalog containing the requested resources.
     *
     * @throws InsufficientPrivilegesException When the user doesn't have access to one or more requested resources.
     */
    String buildCatalogForResources(final UserI user, final Map<String, List<String>> resources) throws InsufficientPrivilegesException;

    /**
     * Retrieves the catalog with the submitted ID.
     *
     * @param user      The user requesting the catalog.
     * @param catalogId The ID of the catalog to be retrieved.
     *
     * @return The specified catalog.
     *
     * @throws InsufficientPrivilegesException When the user doesn't have access to one or more requested resources.
     */
    CatCatalogBean getCatalogForResources(final UserI user, final String catalogId) throws InsufficientPrivilegesException;

    /**
     * Returns the size of the requested catalog. This is useful when making the catalog available for download.
     *
     * @param user      The user requesting the catalog.
     * @param catalogId The ID of the catalog to be retrieved.
     *
     * @return The size of the specified catalog.
     *
     * @throws InsufficientPrivilegesException When the user doesn't have access to one or more requested resources.
     * @throws IOException When an error occurs accessing the catalog.
     */
    long getCatalogSize(UserI user, String catalogId) throws InsufficientPrivilegesException, IOException;

    /**
     * Retrieves the catalog with the specified ID, locates the resources specified in the catalog, and maps the
     * resources to keys based on relative location to parent.
     *
     * @param user      The user retrieving the catalog.
     * @param catalogId The catalog of resource to retrieve.
     *
     * @return A map of relative file locations and resources.
     *
     * @throws InsufficientPrivilegesException When the user doesn't have access to one or more requested resources.
     */
    Map<String, Resource> getResourcesForCatalog(final UserI user, final String catalogId) throws InsufficientPrivilegesException, IOException;

    /**
     * Creates a catalog and resources for a specified XNAT data object. The resource folder is created in the archive
     * space of the parent data object and have the same name as the catalog. The contents of the location specified by
     * the source parameter are copied into the resource folder: if source is a directory, only its contents&emdash;that
     * is, not the source directory itself&emdash;are copied into the resource folder, but if source is a file, that
     * file is copied into the resource folder.
     *
     * @param user        The user creating the catalog.
     * @param parentUri   The URI of the resource parent.
     * @param resource    The file or folder to copy into the resource folder.
     * @param label       The label for the new resource catalog.
     * @param description The description of the resource catalog.
     * @param format      The format of the data in the resource catalog.
     * @param content     The content of the data in the resource catalog.
     * @param tags        Tags for categorizing the data in the resource catalog.
     *
     * @return The newly created {@link XnatResourcecatalog} object representing the new resource.
     *
     * @throws Exception When something goes wrong.
     */
    XnatResourcecatalog insertResources(final UserI user, final String parentUri, final File resource, final String label, final String description, final String format, final String content, final String... tags) throws Exception;

    /**
     * /**
     * Creates a catalog and resources for a specified XNAT data object. The resource folder is created in the archive
     * space of the parent data object and have the same name as the catalog. The contents of the locations specified by
     * the sources parameters are copied into the resource folder: if each source is a directory, only that directory's
     * contents&emdash;that is, not the directory itself&emdash;are copied into the resource folder, but if the source
     * is a file, that file is copied into the resource folder.
     *
     * @param user        The user creating the catalog.
     * @param parentUri   The URI of the resource parent.
     * @param resources   The files and/or folders to copy into the resource folder.
     * @param label       The label for the new resource catalog.
     * @param description The description of the resource catalog.
     * @param format      The format of the data in the resource catalog.
     * @param content     The content of the data in the resource catalog.
     * @param tags        Tags for categorizing the data in the resource catalog.
     *
     * @return The newly created {@link XnatResourcecatalog} object representing the new resource.
     *
     * @throws Exception When something goes wrong.
     */
    XnatResourcecatalog insertResources(final UserI user, final String parentUri, final Collection<File> resources, final String label, final String description, final String format, final String content, final String... tags) throws Exception;

    /**
     * Creates a new resource catalog with the indicated attributes. The new resource catalog is not associated with any
     * particular resource or entity on the system, is not persisted to the database, and doesn't have any related files
     * in the archive. To store the catalog to the system, you can use the {@link #insertResourceCatalog(UserI, String,
     * XnatResourcecatalog, Map)} or {@link #insertResourceCatalog(UserI, String, XnatResourcecatalog)} methods.
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

    XnatResourcecatalog createAndInsertResourceCatalog(final UserI user, final String parentUri, final String label, final String description, final String format, final String content, final String... tags) throws Exception;

    /**
     * Inserts the resource catalog into the resource specified by the parent URI parameter. If you need to pass
     * parameters into the insert function, you should use the {@link #insertResourceCatalog(UserI, String,
     * XnatResourcecatalog, Map)} version of this method.
     *
     * @param user      The user creating the resource catalog.
     * @param parentUri The URI for the resource parent.
     * @param catalog   The catalog object to insert.
     *
     * @return The newly inserted resource catalog.
     *
     * @throws Exception Thrown when an error occurs at some stage of creating the resource catalog.
     */
    XnatResourcecatalog insertResourceCatalog(final UserI user, final String parentUri, final XnatResourcecatalog catalog) throws Exception;

    /**
     * Inserts the resource catalog into the resource specified by the parent URI parameter.
     *
     * @param user       The user creating the resource catalog.
     * @param parentUri  The URI for the resource parent.
     * @param parameters One or more parameters to be passed into the create method.
     * @param catalog    The catalog object to insert.
     *
     * @return The newly inserted resource catalog.
     *
     * @throws Exception Thrown when an error occurs at some stage of creating the resource catalog.
     */
    XnatResourcecatalog insertResourceCatalog(final UserI user, final String parentUri, final XnatResourcecatalog catalog, final Map<String, String> parameters) throws Exception;

    /**
     * Inserts the resource catalog into the resource specified by the parent URI parameter. If you need to pass
     * parameters into the insert function, you should use the {@link #insertResourceCatalog(UserI, String,
     * XnatResourcecatalog, Map)} version of this method.
     *
     * @param user    The user creating the resource catalog.
     * @param parent  The resource parent.
     * @param catalog The catalog object to insert.
     *
     * @return The newly inserted resource catalog.
     *
     * @throws Exception Thrown when an error occurs at some stage of creating the resource catalog.
     */
    XnatResourcecatalog insertResourceCatalog(final UserI user, final BaseElement parent, final XnatResourcecatalog catalog) throws Exception;

    /**
     * Inserts the resource catalog into the resource specified by the parent URI parameter.
     *
     * @param user       The user creating the resource catalog.
     * @param parent     The resource parent.
     * @param parameters One or more parameters to be passed into the create method.
     * @param catalog    The catalog object to insert.
     *
     * @return The newly inserted resource catalog.
     *
     * @throws Exception Thrown when an error occurs at some stage of creating the resource catalog.
     */
    XnatResourcecatalog insertResourceCatalog(final UserI user, final BaseElement parent, final XnatResourcecatalog catalog, final Map<String, String> parameters) throws Exception;

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
