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
import org.nrg.xdat.model.CatCatalogI;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xft.XFTItem;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.ResourceData;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
     * created catalog. You can retrieve the catalog itself by calling {@link #getCachedCatalog(UserI, String)}.
     *
     * @param user       The user requesting the resources.
     * @param resources  The resources to be included in the catalog.
     * @param withSize   Whether to include the total size of the files that will be included
     * @return The ID of the newly created catalog containing the requested resources.
     *
     * @throws InsufficientPrivilegesException When the user doesn't have access to one or more requested resources.
     */
    Map<String,String> buildCatalogForResources(final UserI user, final Map<String, List<String>> resources, final boolean withSize) throws InsufficientPrivilegesException;

    /**
     * Retrieves the catalog with the submitted ID.
     *
     * @param user      The user requesting the catalog.
     * @param catalogId The ID of the catalog to be retrieved.
     * @return The specified catalog.
     *
     * @throws InsufficientPrivilegesException When the user doesn't have access to one or more requested resources.
     */
    CatCatalogI getCachedCatalog(final UserI user, final String catalogId) throws InsufficientPrivilegesException;

    /**
     * Returns the size of the requested catalog. This is useful when making the catalog available for download.
     *
     * @param user      The user requesting the catalog.
     * @param catalogId The ID of the catalog to be retrieved.
     *
     * @return The size of the specified catalog.
     *
     * @throws InsufficientPrivilegesException When the user doesn't have access to one or more requested resources.
     * @throws IOException                     When an error occurs accessing the catalog.
     */
    long getCatalogSize(UserI user, String catalogId) throws InsufficientPrivilegesException, IOException;

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
    @SuppressWarnings("unused")
    XnatResourcecatalog insertResources(final UserI user, final String parentUri, final File resource, final String label, final String description, final String format, final String content, final String... tags) throws Exception;

    /**
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
     * Creates a catalog and resources for a specified XNAT data object. The resource folder is created in the archive
     * space of the parent data object and have the same name as the catalog. The contents of the locations specified by
     * the sources parameters are copied into the resource folder.
     * * If the source is a file, that file is copied into the resource folder.
     * * If the source is a directory, and preserveDirectories=true, the directory and its contents are copied in.
     *      If preserveDirectories=false, only that directory's contents&emdash;that is,
     *      not the directory itself&emdash;are copied into the resource folder.
     *
     * @param user                  The user creating the catalog.
     * @param parentUri             The URI of the resource parent.
     * @param resources             The files and/or folders to copy into the resource folder.
     * @param parentEventId         Nullable parent event id to prevent new workflow entry from being created
     * @param preserveDirectories   Whether to copy a subdirectory along with its contents (true), or just the directory itself (false).
     * @param label                 The label for the new resource catalog.
     * @param description           The description of the resource catalog.
     * @param format                The format of the data in the resource catalog.
     * @param content               The content of the data in the resource catalog.
     * @param tags                  Tags for categorizing the data in the resource catalog.
     *
     * @return The newly created {@link XnatResourcecatalog} object representing the new resource.
     *
     * @throws Exception When something goes wrong.
     */
    XnatResourcecatalog insertResources(final UserI user, final String parentUri, final Collection<File> resources,
                                        @Nullable Integer parentEventId, final boolean preserveDirectories,
                                        final String label, final String description, final String format,
                                        final String content, final String... tags) throws Exception;

    /**
     * Creates a catalog and resources for a specified XNAT data object. The resource folder is created in the archive
     * space of the parent data object and have the same name as the catalog. The contents of the locations specified by
     * the sources parameters are copied into the resource folder.
     * * If the source is a file, that file is copied into the resource folder.
     * * If the source is a directory, and preserveDirectories=true, the directory and its contents are copied in.
     *      If preserveDirectories=false, only that directory's contents&emdash;that is,
     *      not the directory itself&emdash;are copied into the resource folder.
     * * If uploadToRemote=true, files will be uploaded to a remote filesystem and added to the catalog by remote URL
     *
     * @param user                  The user creating the catalog.
     * @param parentUri             The URI of the resource parent.
     * @param resources             The files and/or folders to copy into the resource folder.
     * @param parentEventId         Nullable parent event id to prevent new workflow entry from being created
     * @param preserveDirectories   Whether to copy a subdirectory along with its contents (true), or just the directory itself (false).
     * @param uploadToRemote        True if upload to remote filesystem ought to be attempted (will fallback on local)
     * @param label                 The label for the new resource catalog.
     * @param description           The description of the resource catalog.
     * @param format                The format of the data in the resource catalog.
     * @param content               The content of the data in the resource catalog.
     * @param tags                  Tags for categorizing the data in the resource catalog.
     *
     * @return The newly created {@link XnatResourcecatalog} object representing the new resource.
     *
     * @throws Exception When something goes wrong.
     */
    XnatResourcecatalog insertResources(final UserI user, final String parentUri, final Collection<File> resources,
                                        @Nullable Integer parentEventId, final boolean preserveDirectories,
                                        final boolean uploadToRemote, final String label, final String description,
                                        final String format, final String content, final String... tags)
            throws Exception;

    /**
     * Creates a new resource catalog with the indicated attributes. The new resource catalog is not associated with any
     * particular resource or entity on the system, is not persisted to the database, and doesn't have any related files
     * in the archive. To store the catalog to the system, you can use the {@link #insertResourceCatalog(UserI, String,
     * XnatResourcecatalog, Integer, Map)} or {@link #insertResourceCatalog(UserI, String, XnatResourcecatalog, Integer)} methods.
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
     * XnatResourcecatalog, Integer, Map)} version of this method.
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
     * Inserts the resource catalog into the resource specified by the parent URI parameter. If you need to pass
     * parameters into the insert function, you should use the {@link #insertResourceCatalog(UserI, String,
     * XnatResourcecatalog, Integer, Map)} version of this method.
     *
     * @param user      The user creating the resource catalog.
     * @param parentUri The URI for the resource parent.
     * @param catalog   The catalog object to insert.
     * @param parentEventId EventId from parent workflow if it exists
     *
     * @return The newly inserted resource catalog.
     *
     * @throws Exception Thrown when an error occurs at some stage of creating the resource catalog.
     */
    XnatResourcecatalog insertResourceCatalog(final UserI user, final String parentUri, final XnatResourcecatalog catalog,
                                              @Nullable Integer parentEventId) throws Exception;

    /**
     * Inserts the resource catalog into the resource specified by the parent URI parameter.
     *
     * @param user       The user creating the resource catalog.
     * @param parentUri  The URI for the resource parent.
     * @param parameters One or more parameters to be passed into the create method.
     * @param catalog    The catalog object to insert.
     * @param parentEventId EventId from parent workflow if it exists
     *
     * @return The newly inserted resource catalog.
     *
     * @throws Exception Thrown when an error occurs at some stage of creating the resource catalog.
     */
    XnatResourcecatalog insertResourceCatalog(final UserI user, final String parentUri, final XnatResourcecatalog catalog,
                                              @Nullable Integer parentEventId, final Map<String, String> parameters) throws Exception;

    /**
     * Inserts the resource catalog into the resource specified by the parent URI parameter. If you need to pass
     * parameters into the insert function, you should use the {@link #insertResourceCatalog(UserI, String,
     * XnatResourcecatalog, Integer, Map)} version of this method.
     *
     * @param user    The user creating the resource catalog.
     * @param parent  The resource parent.
     * @param catalog The catalog object to insert.
     *
     * @return The newly inserted resource catalog.
     *
     * @throws Exception Thrown when an error occurs at some stage of creating the resource catalog.
     */
    @SuppressWarnings("unused")
    XnatResourcecatalog insertResourceCatalog(final UserI user, final BaseElement parent, final XnatResourcecatalog catalog) throws Exception;

    /**
     * Inserts the resource catalog into the resource specified by the parent URI parameter. If you need to pass
     * parameters into the insert function, you should use the {@link #insertResourceCatalog(UserI, String,
     * XnatResourcecatalog, Integer, Map)} version of this method.
     *
     * @param user    The user creating the resource catalog.
     * @param parent  The resource parent.
     * @param catalog The catalog object to insert.
     * @param parentEventId EventId from parent workflow if it exists
     *
     * @return The newly inserted resource catalog.
     *
     * @throws Exception Thrown when an error occurs at some stage of creating the resource catalog.
     */
    @SuppressWarnings("unused")
    XnatResourcecatalog insertResourceCatalog(final UserI user, final BaseElement parent, final XnatResourcecatalog catalog,
                                              @Nullable Integer parentEventId) throws Exception;

    /**
     * Inserts the resource catalog into the resource specified by the parent URI parameter.
     *
     * @param user       The user creating the resource catalog.
     * @param parent     The resource parent.
     * @param parameters One or more parameters to be passed into the create method.
     * @param catalog    The catalog object to insert.
     * @param parentEventId EventId from parent workflow if it exists
     *
     * @return The newly inserted resource catalog.
     *
     * @throws Exception Thrown when an error occurs at some stage of creating the resource catalog.
     */
    XnatResourcecatalog insertResourceCatalog(final UserI user, final BaseElement parent, final XnatResourcecatalog catalog,
                                              @Nullable Integer parentEventId, final Map<String, String> parameters) throws Exception;


    /**
     * Creates a new resource catalog with the indicated attributes and inserts it into the resource specified by the
     * parent URI parameter.
     *
     * @param user              The user creating the resource catalog.
     * @param parentUri         The URI for the resource parent.
     * @param parentEventId     EventId from parent workflow if it exists
     * @param label             The label for the new resource.
     * @param description       The description of the new resource.
     * @param format            The format of the new resource.
     * @param content           The content type of the new resource.
     * @param tags              One or more tags for the new resource.
     * @return The newly created, inserted resource catalog.
     * @throws Exception Thrown when an error occurs at some stage of creating or inserting the resource catalog.
     */
    XnatResourcecatalog createAndInsertResourceCatalog(final UserI user, final String parentUri, @Nullable Integer parentEventId,
                                                       final String label, final String description, final String format,
                                                       final String content, final String... tags) throws Exception;

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
    @SuppressWarnings("unused")
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

    /**
     * Inserts the XML object into the XNAT data store. The submitted XML is validated and inserted (created or updated as appropriate). The contents of the parameters map
     * can contain the following parameters:
     *
     * <ul>
     *     <li>event_reason</li>
     * 	   <li>event_type</li>
     * 	   <li>event_action</li>
     * 	   <li>event_comment</li>
     * </ul>
     *
     * These values are used when creating the audit entries in XNAT for the object creation or update operation. If values aren't provided for these parameters, default
     * values are set in their place. In addition, the parameter map can include property values to set on the resulting XFTItem, with the map key corresponding to the
     * property's XML path and map value to the property value. Generally however, property values should just be set directly in the XML.
     *
     * @param user              The user inserting the XML object.
     * @param input             An input stream from which the XML object can be read.
     * @param allowDataDeletion Indicates whether values on an existing XML object should be overwritten by values in the inserted XML.
     * @param parameters        A map of parameters.
     *
     * @return The resulting {@link XFTItem} object.
     *
     * @throws Exception When an error occurs during object creation or update.
     */
    XFTItem insertXmlObject(final UserI user, final InputStream input, final boolean allowDataDeletion,
                            final Map<String, ?> parameters) throws Exception;

    /**
     * Inserts the XML object into the XNAT data store. The submitted XML is validated and inserted (created or updated as appropriate). The contents of the parameters map
     * can contain the following parameters:
     *
     * <ul>
     *     <li>event_reason</li>
     * 	   <li>event_type</li>
     * 	   <li>event_action</li>
     * 	   <li>event_comment</li>
     * </ul>
     *
     * These values are used when creating the audit entries in XNAT for the object creation or update operation. If values aren't provided for these parameters, default
     * values are set in their place. In addition, the parameter map can include property values to set on the resulting XFTItem, with the map key corresponding to the
     * property's XML path and map value to the property value. Generally however, property values should just be set directly in the XML.
     *
     * @param user              The user inserting the XML object.
     * @param input             An input stream from which the XML object can be read.
     * @param allowDataDeletion Indicates whether values on an existing XML object should be overwritten by values in the inserted XML.
     * @param parameters        A map of parameters.
     * @param parentEventId     EventId from parent workflow if it exists
     *
     * @return The resulting {@link XFTItem} object.
     *
     * @throws Exception When an error occurs during object creation or update.
     */
    XFTItem insertXmlObject(final UserI user, final InputStream input, final boolean allowDataDeletion,
                            final Map<String, ?> parameters, @Nullable Integer parentEventId) throws Exception;

    /**
     * Inserts the XML object into the XNAT data store. The submitted XML is validated and inserted (created or updated as appropriate). The contents of the parameters map
     * can contain the following parameters:
     *
     * <ul>
     *     <li>event_reason</li>
     * 	   <li>event_type</li>
     * 	   <li>event_action</li>
     * 	   <li>event_comment</li>
     * </ul>
     *
     * These values are used when creating the audit entries in XNAT for the object creation or update operation. If values aren't provided for these parameters, default
     * values are set in their place. In addition, the parameter map can include property values to set on the resulting XFTItem, with the map key corresponding to the
     * property's XML path and map value to the property value. Generally however, property values should just be set directly in the XML.
     *
     * @param user              The user inserting the XML object.
     * @param inputFile         File from which the XML object can be read.
     * @param allowDataDeletion Indicates whether values on an existing XML object should be overwritten by values in the inserted XML.
     * @param parameters        A map of parameters.
     * @param parentEventId     EventId from parent workflow if it exists
     *
     * @return The resulting {@link XFTItem} object.
     *
     * @throws Exception When an error occurs during object creation or update.
     */
    XFTItem insertXmlObject(final UserI user, final File inputFile, final boolean allowDataDeletion,
                            final Map<String, ?> parameters, @Nullable Integer parentEventId) throws Exception;


    /**
     * Get ResourceData object based on a URI string
     * @param uriString     the uri string, identified by standard archive-relative paths, such as
     *                      /archive/experiments/XNAT_E0001
     *                      or /archive/projects/XNAT_01/subjects/XNAT_01_01/resources/RESID
     * @return ResourceData
     * @throws ClientException for invalid URI or file URI
     */
    ResourceData getResourceDataFromUri(String uriString) throws ClientException;

    /**
     * Get ResourceData object based on a URI string
     * @param uriString     the uri string, identified by standard archive-relative paths, such as
     *                      /archive/experiments/XNAT_E0001
     *                      or /archive/projects/XNAT_01/subjects/XNAT_01_01/resources/RESID
     * @param acceptFileUri true if URI can be a file path
     * @return ResourceData
     * @throws ClientException for invalid URI
     */
    ResourceData getResourceDataFromUri(String uriString, boolean acceptFileUri) throws ClientException;

    /**
     * Check that user can edit item, throw exception if not
     *
     * @param user the user
     * @param item the item
     * @param accessType the access type
     * @param resourceName the resource name, just used for logging currently
     */
    void checkPermissionsOnItem(final UserI user, final ArchivableItem item, @Nonnull final String accessType,
                                final String resourceName)
            throws ServerException, ClientException;

    /**
     * Pulls all files for specified resource <strong>into an arbitrary location</strong>. It will first copy files that
     * exist in the XNAT archive, then it will attempt to pull any missing files from remote filesystems
     *
     * The resource should be identified by standard archive-relative paths, e.g.:
     *
     * <pre>
     * {@code
     * /archive/experiments/XNAT_E0001
     * /archive/projects/XNAT_01/subjects/XNAT_01_01
     * }
     * </pre>
     *
     * @param user                  The user performing the operation.
     * @param uriString             The uri
     * @param archiveRelativeDir    The archive-relative directory for the resource corresponding to uriString
     * @param destinationDir        The path to the destination or null to pull to archive. The destinationDir will be
     *                              populated with subdirectories as appropriate. For example, if uriString is a session,
     *                              the destinationDir would be the equivalent of archiveRelativeDir, and would be
     *                              populated to contain e.g., RESOURCES/ and SCANS/.
     *                              <strong>Conflicting files in the destinationDir WILL BE OVERWRITTEN</strong>
     *
     * @throws ClientException When an error occurs that is caused somehow by the requested operation.
     * @throws ServerException When an error occurs in the system during the pull operation.
     */
    void pullResourceCatalogsToDestination(final UserI user, final String uriString, final String archiveRelativeDir,
                                           @Nullable String destinationDir)
            throws ServerException, ClientException;

    /**
     * Does the resource indicated by the uriString have remote files
     * @param user      The user performing the operation.
     * @param uriString     the uri string, identified by standard archive-relative paths, such as
     *                      /archive/experiments/XNAT_E0001
     *                      or /archive/projects/XNAT_01/subjects/XNAT_01_01/resources/RESID
     * @return T/F
     */
    boolean hasRemoteFiles(final UserI user, final String uriString) throws ClientException, ServerException;
}
