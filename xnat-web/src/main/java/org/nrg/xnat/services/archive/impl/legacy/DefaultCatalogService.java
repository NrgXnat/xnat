/*
 * web: org.nrg.xnat.services.archive.impl.legacy.DefaultCatalogService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *  
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.archive.impl.legacy;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.bean.CatEntryBean;
import org.nrg.xdat.model.CatCatalogI;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.helpers.uri.archive.*;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnat.services.archive.PathResourceMap;
import org.nrg.xnat.turbine.utils.ArchivableItem;
import org.nrg.xnat.utils.CatalogUtils;
import org.nrg.xnat.utils.WorkflowUtils;
import org.nrg.xnat.web.http.CatalogPathResourceMap;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.nrg.xnat.restlet.util.XNATRestConstants.getPrearchiveTimestamp;

/**
 * {@inheritDoc}
 */
@Service
public class DefaultCatalogService implements CatalogService {
    @Autowired
    public DefaultCatalogService(final JdbcTemplate template, final CacheManager cacheManager, final SiteConfigPreferences preferences) {
        _template = template;
        _parameterized = new NamedParameterJdbcTemplate(_template);
        if (!cacheManager.cacheExists(CATALOG_SERVICE_CACHE)) {
            final CacheConfiguration config = new CacheConfiguration(CATALOG_SERVICE_CACHE, 0)
                    .copyOnRead(false).copyOnWrite(false)
                    .eternal(false)
                    .persistence(new PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.NONE))
                    .timeToLiveSeconds(3600)
                    .maxEntriesLocalHeap(5000);
            _cache = new Cache(config);
            cacheManager.addCache(_cache);
        } else {
            _cache = cacheManager.getCache(CATALOG_SERVICE_CACHE);
        }
        _archiveRoot = preferences.getArchivePath();
    }

    @Override
    public String buildCatalogForResources(final UserI user, final Map<String, List<String>> resourceMap) throws InsufficientPrivilegesException {
        final CatCatalogBean catalog = new CatCatalogBean();
        catalog.setId(String.format(CATALOG_FORMAT, user.getLogin(), getPrearchiveTimestamp()));

        final DownloadArchiveOptions options = new DownloadArchiveOptions(resourceMap.get("options"));
        catalog.setDescription(options.getDescription());

        final List<String> sessions = resourceMap.get("sessions");
        final List<String> scanTypes = resourceMap.get("scan_types");
        final List<String> scanFormats = resourceMap.get("scan_formats");
        final List<String> resources = resourceMap.get("resources");
        final List<String> reconstructions = resourceMap.get("reconstructions");
        final List<String> assessors = resourceMap.get("assessors");

        for (final String subjectSession : sessions) {
            final String[] atoms = subjectSession.split(":");
            final String subject = atoms[0];
            final String label = atoms[1];
            final String session = atoms[2];

            final CatCatalogBean sessionCatalog = new CatCatalogBean();
            sessionCatalog.setId(session);
            sessionCatalog.setDescription(subject);

            final CatCatalogI sessionsByScanTypesAndFormats = getSessionsByScanTypesAndFormats(subject, label, session, scanTypes, scanFormats, options);
            if (sessionsByScanTypesAndFormats != null) {
                addSafeEntrySet(sessionCatalog, sessionsByScanTypesAndFormats);
            }

            final CatCatalogI resourcesCatalog = getRelatedData(subject, label, session, resources, "resources", options);
            if (resourcesCatalog != null) {
                addSafeEntrySet(sessionCatalog, resourcesCatalog);
            }

            final CatCatalogI reconstructionsCatalog = getRelatedData(subject, subject, session, reconstructions, "reconstructions", options);
            if (reconstructionsCatalog != null) {
                addSafeEntrySet(sessionCatalog, reconstructionsCatalog);
            }

            final CatCatalogI assessorsCatalog = getRelatedData(subject, subject, session, assessors, "assessors", options);
            if (assessorsCatalog != null) {
                addSafeEntrySet(sessionCatalog, assessorsCatalog);
            }

            catalog.addSets_entryset(sessionCatalog);
        }

        storeToCache(user, catalog);

        return catalog.getId();
    }

    @Override
    public CatCatalogI getCatalogForResources(final UserI user, final String catalogId) throws InsufficientPrivilegesException {
        final CatCatalogI catalog = getFromCache(user, catalogId);
        if (catalog == null) {
            throw new InsufficientPrivilegesException(user.getUsername());
        }
        return catalog;
    }

    @Override
    public long getCatalogSize(final UserI user, final String catalogId) throws InsufficientPrivilegesException, IOException {
        final CatCatalogI catalog = getFromCache(user, catalogId);
        if (catalog == null) {
            throw new InsufficientPrivilegesException(user.getUsername());
        }
        final StringWriter writer = new StringWriter();
        if (catalog instanceof CatCatalogBean) {
            ((CatCatalogBean) catalog).toXML(writer, true);
        } else {
            try {
                catalog.toXML(writer);
            } catch (Exception e) {
                throw new IOException("An error occurred trying to access the catalog " + catalogId + " for the user " + user.getLogin());
            }
        }
        return writer.toString().length();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathResourceMap<String, Resource> getResourcesForCatalog(final UserI user, final String catalogId) throws InsufficientPrivilegesException, IOException {
        final CatCatalogI catalog = getFromCache(user, catalogId);
        if (catalog == null) {
            throw new InsufficientPrivilegesException(user.getUsername());
        }
        return new CatalogPathResourceMap(catalog, _archiveRoot);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatResourcecatalog insertResources(final UserI user, final String parentUri, final File resource, final String label, final String description, final String format, final String content, final String... tags) throws Exception {
        return insertResources(user, parentUri, Collections.singletonList(resource), label, description, format, content, tags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatResourcecatalog insertResources(final UserI user, final String parentUri, final Collection<File> resources, final String label, final String description, final String format, final String content, final String... tags) throws Exception {
        final Collection<File> missing = Lists.newArrayList();
        for (final File source : resources) {
            if (!source.exists()) {
                missing.add(source);
            }
        }
        if (missing.size() > 0) {
            throw new FileNotFoundException("Unable to find the following source files/folders: " + Joiner.on(", ").join(missing));
        }

        final XnatResourcecatalog catalog = createAndInsertResourceCatalog(user, parentUri, label, description, format, content, tags);

        final File destination = new File(catalog.getUri()).getParentFile();
        for (final File source : resources) {
            if (source.isDirectory()) {
                FileUtils.copyDirectory(source, destination);
            } else {
                FileUtils.copyFileToDirectory(source, destination);
            }
        }

        refreshResourceCatalog(user, parentUri);

        return catalog;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("Duplicates")
    public XnatResourcecatalog createResourceCatalog(final UserI user, final String label, final String description, final String format, final String content, final String... tags) throws Exception {
        final XFTItem item = XFTItem.NewItem("xnat:resourceCatalog", user);
        final XnatResourcecatalog catalog = (XnatResourcecatalog) BaseElement.GetGeneratedItem(item);
        catalog.setLabel(label);

        if (StringUtils.isNotBlank(description)) {
            catalog.setDescription(description);
        }
        if (StringUtils.isNotBlank(format)) {
            catalog.setFormat(format);
        }
        if (StringUtils.isNotBlank(content)) {
            catalog.setContent(content);
        }
        for (final String tag : tags) {
            if (StringUtils.isNotBlank(tag)) {
                for (final String subtag : tag.split("\\s*,\\s*")) {
                    final XnatAbstractresourceTag resourceTag = new XnatAbstractresourceTag(user);
                    if (subtag.contains("=")) {
                        final String[] atoms = subtag.split("=", 2);
                        resourceTag.setName(atoms[0]);
                        resourceTag.setTag(atoms[1]);
                    } else if (subtag.contains(":")) {
                        final String[] atoms = subtag.split(":", 2);
                        resourceTag.setName(atoms[0]);
                        resourceTag.setTag(atoms[1]);
                    } else {
                        resourceTag.setTag(subtag);
                    }
                    catalog.setTags_tag(resourceTag);
                }
            }
        }
        return catalog;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatResourcecatalog createAndInsertResourceCatalog(final UserI user, final String parentUri, final String label, final String description, final String format, final String content, final String... tags) throws Exception {
        final XnatResourcecatalog catalog = createResourceCatalog(user, label, description, format, content, tags);
        insertResourceCatalog(user, parentUri, catalog);
        return catalog;
    }

    /**
     * {@inheritDoc}
     */

    public XnatResourcecatalog insertResourceCatalog(final UserI user, final String parentUri, final XnatResourcecatalog catalog) throws Exception {
        return insertResourceCatalog(user, parentUri, catalog, null);
    }

    /**
     * {@inheritDoc}
     */
    public XnatResourcecatalog insertResourceCatalog(final UserI user, final String parentUri, final XnatResourcecatalog catalog, final Map<String, String> parameters) throws Exception {
        final URIManager.DataURIA uri = UriParserUtils.parseURI(parentUri);

        if (!(uri instanceof URIManager.ArchiveItemURI)) {
            throw new ClientException("Invalid Resource URI:" + parentUri);
        }

        final URIManager.ArchiveItemURI resourceURI = (URIManager.ArchiveItemURI) uri;

        final Class<? extends URIManager.ArchiveItemURI> parentClass = resourceURI.getClass();
        final BaseElement parent;
        try {
            if (AssessorURII.class.isAssignableFrom(parentClass)) {
                parent = ((AssessorURII) resourceURI).getAssessor();
            } else if (ExperimentURII.class.isAssignableFrom(parentClass)) {
                parent = ((ExperimentURII) resourceURI).getExperiment();
            } else if (ScanURII.class.isAssignableFrom(parentClass)) {
                parent = ((ScanURII) resourceURI).getScan();
            } else if (ProjectURII.class.isAssignableFrom(parentClass)) {
                parent = ((ProjectURII) resourceURI).getProject();
            } else if (SubjectURII.class.isAssignableFrom(parentClass)) {
                parent = ((SubjectURII) resourceURI).getSubject();
            } else if (ReconURII.class.isAssignableFrom(parentClass)) {
                parent = ((ReconURII) resourceURI).getRecon();
            } else {
                _log.error("The URI is of an unknown type: " + resourceURI.getClass().getName());
                return null;
            }
        } catch (Exception e) {
            _log.error("An error occurred creating the catalog with label {} for resource {}, please check the server logs.", catalog.getLabel(), parentUri);
            return null;
        }

        return insertResourceCatalog(user, parent, catalog, parameters);
    }

    /**
     * {@inheritDoc}
     */
    public XnatResourcecatalog insertResourceCatalog(final UserI user, final BaseElement item, final XnatResourcecatalog catalog) throws Exception {
        return insertResourceCatalog(user, item, catalog, null);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("ConstantConditions")
    public XnatResourcecatalog insertResourceCatalog(final UserI user, final BaseElement parent, final XnatResourcecatalog catalog, final Map<String, String> parameters) throws Exception {
        final XFTItem item = parent.getItem();
        final boolean isScan = item.instanceOf(XnatImagescandata.SCHEMA_ELEMENT_NAME);
        final boolean isReconstruction = item.instanceOf(XnatReconstructedimagedata.SCHEMA_ELEMENT_NAME);
        final boolean isExperiment = item.instanceOf(XnatExperimentdata.SCHEMA_ELEMENT_NAME);
        final boolean isProject = item.instanceOf(XnatProjectdata.SCHEMA_ELEMENT_NAME);
        final boolean isSubject = item.instanceOf(XnatSubjectdata.SCHEMA_ELEMENT_NAME);

        final boolean useParentForUploadId = isScan || isReconstruction;

        final String uploadId;
        if (useParentForUploadId) {
            final Object id = item.getProperty("ID");
            uploadId = StringUtils.isNotBlank((String) id) ? (String) id : getPrearchiveTimestamp();
        } else {
            uploadId = StringUtils.isNotBlank(catalog.getLabel()) ? catalog.getLabel() : getPrearchiveTimestamp();
        }

        final EventDetails event = new EventDetails(EventUtils.CATEGORY.DATA, EventUtils.TYPE.PROCESS, EventUtils.CREATE_RESOURCE, "Catalog service invoked", "");
        try {
            if (isExperiment) {
                final XnatExperimentdata experiment = (XnatExperimentdata) parent;
                event.setComment("Created experiment resource " + uploadId + " for " + experiment.getId() + " at " + catalog.getUri());
                insertExperimentResourceCatalog(user, experiment, catalog, uploadId, event, parameters == null ? EMPTY_MAP : parameters);
            } else if (isScan) {
                final XnatImagescandata scan = (XnatImagescandata) parent;
                event.setComment("Created scan resource " + uploadId + " for image session " + scan.getImageSessionId() + " scan " + scan.getId() + " at " + catalog.getUri());
                insertScanResourceCatalog(user, scan, catalog, uploadId, event);
            } else if (isProject) {
                final XnatProjectdata project = (XnatProjectdata) parent;
                event.setComment("Created project resource " + uploadId + " for project " + project.getId() + " at " + catalog.getUri());
                insertProjectResourceCatalog(user, project, catalog, uploadId, event);
            } else if (isSubject) {
                final XnatSubjectdata subject = (XnatSubjectdata) parent;
                event.setComment("Created subject resource " + uploadId + " for " + subject.getId() + " at " + catalog.getUri());
                insertSubjectResourceCatalog(user, subject, catalog, uploadId, event, parameters == null ? EMPTY_MAP : parameters);
            } else if (isReconstruction) {
                final XnatReconstructedimagedata reconstruction = (XnatReconstructedimagedata) parent;
                event.setComment("Created reconstruction resource " + uploadId + " for image session " + reconstruction.getImageSessionId() + " reconstruction " + reconstruction.getId() + " at " + catalog.getUri());
                insertReconstructionResourceCatalog(user, reconstruction, catalog, uploadId, event, parameters == null ? EMPTY_MAP : parameters);
            }
            return catalog;
        } catch (Exception e) {
            _log.error("An error occurred creating the catalog with label {} for resource {}, please check the server logs.", catalog.getLabel(), parent.getItem().getIDValue());
            return null;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshResourceCatalog(final UserI user, final String resource, final Operation... operations) throws ServerException, ClientException {
        _refreshCatalog(user, resource, Arrays.asList(operations));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshResourceCatalog(final UserI user, final String resource, final Collection<Operation> operations) throws ServerException, ClientException {
        _refreshCatalog(user, resource, operations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshResourceCatalogs(final UserI user, final List<String> resources, final Operation... operations) throws ServerException, ClientException {
        for (final String resource : resources) {
            _refreshCatalog(user, resource, Arrays.asList(operations));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshResourceCatalogs(final UserI user, final List<String> resources, final Collection<Operation> operations) throws ServerException, ClientException {
        for (final String resource : resources) {
            _refreshCatalog(user, resource, operations);
        }
    }

    /**
     * Performs the actual work of refreshing a single catalog.
     *
     * @param user       The user requesting the refresh operation.
     * @param resource   The archive path for the resource to refresh.
     * @param operations The operations to be performed.
     *
     * @throws ClientException When an error occurs that is caused somehow by the requested operation.
     * @throws ServerException When an error occurs in the system during the refresh operation.
     */
    private void _refreshCatalog(final UserI user, final String resource, final Collection<Operation> operations) throws ServerException, ClientException {
        try {
            final URIManager.DataURIA uri = UriParserUtils.parseURI(resource);

            if (!(uri instanceof URIManager.ArchiveItemURI)) {
                throw new ClientException("Invalid Resource URI:" + resource);
            }

            final URIManager.ArchiveItemURI resourceURI = (URIManager.ArchiveItemURI) uri;
            final ArchivableItem item = resourceURI.getSecurityItem();

            try {
                if (!Permissions.canEdit(user, item)) {
                    throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN, "The user " + user.getLogin() + " does not have permission to access the resource " + resource);
                }
            } catch (Exception e) {
                throw new ServerException(Status.SERVER_ERROR_INTERNAL, "An error occurred try to check the user " + user.getLogin() + " permissions for resource " + resource);
            }

            if (item != null) {
                final EventDetails event = new EventDetails(EventUtils.CATEGORY.DATA, EventUtils.TYPE.PROCESS, "Catalog(s) Refreshed", "Refreshed catalog for resource " + resourceURI.getUri(), "");

                final Collection<Operation> list = getOperations(operations);

                final boolean append = list.contains(Operation.Append);
                final boolean checksum = list.contains(Operation.Checksum);
                final boolean delete = list.contains(Operation.Delete);
                final boolean populateStats = list.contains(Operation.PopulateStats);

                try {
                    if (resourceURI instanceof ResourceURII) {//if we are referencing a specific catalog, make sure it doesn't actually reference an individual file.
                        final String resourceFilePath = ((ResourceURII) resourceURI).getResourceFilePath();
                        if (StringUtils.isNotEmpty(resourceFilePath) && !resourceFilePath.equals("/")) {
                            throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, new Exception("This operation cannot be performed directly on a file URL"));
                        }
                    }

                    try {
                        if (!Permissions.canEdit(user, item)) {
                            throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN, new Exception("Unauthorized attempt to add a file to " + resourceURI.getUri()));
                        }
                    } catch (ClientException e) {
                        throw e;
                    } catch (Exception e) {
                        _log.error("An error occurred trying to check the edit permissions for user " + user.getUsername(), e);
                    }

                    final PersistentWorkflowI wrk = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, item.getItem(), event);

                    for (XnatAbstractresourceI res : resourceURI.getResources(true)) {
                        final String archiveRootPath = item.getArchiveRootPath();
                        refreshResourceCatalog((XnatAbstractresource) res, archiveRootPath, populateStats, checksum, delete, append, user, wrk.buildEvent());
                    }

                    WorkflowUtils.complete(wrk, wrk.buildEvent());
                } catch (PersistentWorkflowUtils.JustificationAbsent justificationAbsent) {
                    throw new ClientException("No justification was provided for the refresh action, but is required by the system configuration.");
                } catch (PersistentWorkflowUtils.ActionNameAbsent actionNameAbsent) {
                    throw new ClientException("No action name was provided for the refresh action, but is required by the system configuration.");
                } catch (PersistentWorkflowUtils.IDAbsent idAbsent) {
                    throw new ClientException("No workflow ID was provided for the refresh action, but is required by the system configuration.");
                } catch (BaseXnatExperimentdata.UnknownPrimaryProjectException e) {
                    throw new ClientException("Couldn't find the primary project for the specified resource " + resource);
                } catch (Exception e) {
                    throw new ServerException("An error occurred trying to save the workflow for the refresh operation.", e);
                }
            }
        } catch (MalformedURLException e) {
            throw new ClientException("Invalid Resource URI:" + resource);
        }
    }

    private void insertProjectResourceCatalog(final UserI user, final XnatProjectdata project, final XnatResourcecatalog resourceCatalog, final String uploadId, final EventDetails ci) throws Exception {
        final XnatProjectdata working;
        if (project.getUser() == null) {
            working = XnatProjectdata.getProjectByIDorAlias(project.getId(), user, true);
        } else {
            working = project;
        }

        final String resourceFolder = resourceCatalog.getLabel();

        final CatCatalogBean catalog = new CatCatalogBean();
        catalog.setId(uploadId);

        final Path path = Paths.get(working.getRootArchivePath(), "resources");
        final File destination = resourceFolder != null
                                 ? path.resolve(Paths.get(resourceFolder, catalog.getId() + "_catalog.xml")).toFile()
                                 : path.resolve(catalog.getId() + "_catalog.xml").toFile();

        destination.getParentFile().mkdirs();

        try (final FileWriter writer = new FileWriter(destination)) {
            catalog.toXML(writer, true);
        } catch (IOException e) {
            throw new IOException("An error occurred trying to write the catalog to the file " + destination.getAbsolutePath(), e);
        }

        resourceCatalog.setUri(destination.getAbsolutePath());

        try {
            working.setResources_resource(resourceCatalog);
            SaveItemHelper.authorizedSave(working, user, false, false, ci);
        } catch (Exception e) {
            throw new Exception("An error occurred trying to set the in/out status on the project " + project.getId(), e);
        }
    }

    private void insertSubjectResourceCatalog(final UserI user, final XnatSubjectdata subject, final XnatResourcecatalog resourceCatalog, final String uploadId, final EventDetails ci, final Map<String, String> parameters) throws Exception {
        final String resourceFolder = resourceCatalog.getLabel();
        final XnatProjectdata project = parameters.containsKey("project")
                                        ? XnatProjectdata.getProjectByIDorAlias(parameters.get("project"), user, false)
                                        : subject.getPrimaryProject(false);

        CatCatalogBean catalog = new CatCatalogBean();
        catalog.setId(uploadId);

        final Path path = Paths.get(project.getRootArchivePath(), "subjects", subject.getArchiveDirectoryName());
        final File destination = resourceFolder != null
                                 ? path.resolve(Paths.get(resourceFolder, catalog.getId() + "_catalog.xml")).toFile()
                                 : path.resolve(catalog.getId() + "_catalog.xml").toFile();

        destination.getParentFile().mkdirs();

        try (final FileWriter writer = new FileWriter(destination)) {
            catalog.toXML(writer, true);
        } catch (IOException e) {
            throw new Exception("An error occurred trying to write the catalog to the file " + destination.getAbsolutePath(), e);
        }

        resourceCatalog.setUri(destination.getAbsolutePath());

        try {
            final XnatSubjectdata copy = subject.getLightCopy();
            copy.setResources_resource(resourceCatalog);
            SaveItemHelper.authorizedSave(copy, user, false, false, ci);
        } catch (Exception e) {
            throw new Exception("An error occurred trying to set the in/out status on the project " + project.getId(), e);
        }
    }

    private void insertExperimentResourceCatalog(final UserI user, final XnatExperimentdata experiment, final XnatResourcecatalog resourceCatalog, final String uploadId, final EventDetails event, final Map<String, String> parameters) throws Exception {
        final boolean isImageAssessor = experiment.getItem().instanceOf(XnatImageassessordata.SCHEMA_ELEMENT_NAME);
        final String resourceFolder = resourceCatalog.getLabel();
        final String experimentId = experiment.getId();

        final Path path;
        if (isImageAssessor) {
            final XnatImagesessiondata parent = ((XnatImageassessordata) experiment).getImageSessionData();
            path = Paths.get(parent.getCurrentSessionFolder(true), "ASSESSORS", experiment.getArchiveDirectoryName());
        } else {
            path = Paths.get(experiment.getCurrentSessionFolder(true), "RESOURCES");
        }

        final CatCatalogBean catalog = new CatCatalogBean();
        catalog.setId(uploadId);

        final File destination = resourceFolder != null
                                 ? path.resolve(Paths.get(resourceFolder, catalog.getId() + "_catalog.xml")).toFile()
                                 : path.resolve(catalog.getId() + "_catalog.xml").toFile();

        destination.getParentFile().mkdirs();

        try (final FileWriter writer = new FileWriter(destination)) {
            catalog.toXML(writer, true);
        } catch (IOException e) {
            throw new IOException("An error occurred trying to write the catalog to the file " + destination.getAbsolutePath(), e);
        }

        resourceCatalog.setUri(destination.getAbsolutePath());

        if (isImageAssessor) {
            try {
                final XnatImageassessordata assessor = (XnatImageassessordata) experiment.getLightCopy();
                if (parameters.containsKey("type") && StringUtils.equals("in", parameters.get("type"))) {
                    assessor.setIn_file(resourceCatalog);
                } else {
                    assessor.setOut_file(resourceCatalog);
                }
                SaveItemHelper.authorizedSave(assessor, user, false, false, event);
            } catch (Exception e) {
                throw new Exception("An error occurred trying to set the in/out status on the image assessor " + experimentId, e);
            }
        } else {
            try {
                final XnatExperimentdata copy = experiment.getLightCopy();
                copy.setResources_resource(resourceCatalog);
                SaveItemHelper.authorizedSave(copy, user, false, false, event);
            } catch (Exception e) {
                throw new Exception("An error occurred trying to set the in/out status on the experiment " + experimentId, e);
            }
        }
    }

    private void insertScanResourceCatalog(final UserI user, final XnatImagescandata scan, final XnatResourcecatalog resourceCatalog, final String uploadId, final EventDetails ci) throws Exception {
        final String resourceFolder = resourceCatalog.getLabel();

        final CatCatalogBean catalog = new CatCatalogBean();
        catalog.setId(uploadId);

        final XnatImagesessiondata session = scan.getImageSessionData();

        final Path path = Paths.get(session.getCurrentSessionFolder(true), "SCANS", scan.getId());
        final File destination = resourceFolder != null
                                 ? path.resolve(Paths.get(resourceFolder, catalog.getId() + "_catalog.xml")).toFile()
                                 : path.resolve(catalog.getId() + "_catalog.xml").toFile();

        destination.getParentFile().mkdirs();

        try (final FileWriter writer = new FileWriter(destination)) {
            catalog.toXML(writer, true);
        } catch (IOException e) {
            throw new IOException("An error occurred trying to write the catalog to the file " + destination.getAbsolutePath(), e);
        }

        resourceCatalog.setUri(destination.getAbsolutePath());

        if (scan.getFile().size() == 0) {
            if (resourceCatalog.getContent() == null && scan.getType() != null) {
                resourceCatalog.setContent("RAW");
            }
        }

        try {
            scan.setFile(resourceCatalog);
            SaveItemHelper.authorizedSave(scan, user, false, false, ci);
        } catch (Exception e) {
            throw new Exception("An error occurred trying to save the scan " + scan.getId() + " for session " + scan.getImageSessionData().getLabel(), e);
        }
    }

    private void insertReconstructionResourceCatalog(final UserI user, final XnatReconstructedimagedata reconstruction, final XnatResourcecatalog resourceCatalog, final String uploadId, final EventDetails ci, final Map<String, String> parameters) throws Exception {
        final String resourceFolder = resourceCatalog.getLabel();
        final XnatImagesessiondata session = reconstruction.getImageSessionData();
        final Path path = Paths.get(session.getCurrentSessionFolder(true), "ASSESSORS", "PROCESSED", uploadId);

        final CatCatalogBean catalog = new CatCatalogBean();
        catalog.setId(uploadId);

        final File destination = resourceFolder != null
                                 ? path.resolve(Paths.get(resourceFolder, catalog.getId() + "_catalog.xml")).toFile()
                                 : path.resolve(catalog.getId() + "_catalog.xml").toFile();

        destination.getParentFile().mkdirs();

        try (final FileWriter writer = new FileWriter(destination)) {
            catalog.toXML(writer, true);
        } catch (IOException e) {
            throw new IOException("An error occurred trying to write the catalog to the file " + destination.getAbsolutePath(), e);
        }

        resourceCatalog.setUri(destination.getAbsolutePath());

        try {
            if (parameters.containsKey("type") && StringUtils.equals("in", parameters.get("type"))) {
                reconstruction.setIn_file(resourceCatalog);
            } else {
                reconstruction.setOut_file(resourceCatalog);
            }
            SaveItemHelper.authorizedSave(reconstruction, user, false, false, ci);
        } catch (Exception e) {
            throw new Exception("An error occurred trying to set the in/out status on the reconstructed image data " + reconstruction.getId(), e);
        }
    }

    private void refreshResourceCatalog(final XnatAbstractresource resource, final String projectPath, final boolean populateStats, final boolean checksums, final boolean removeMissingFiles, final boolean addUnreferencedFiles, final UserI user, final EventMetaI now) throws ServerException {
        if (resource instanceof XnatResourcecatalog) {
            final XnatResourcecatalog catRes = (XnatResourcecatalog) resource;

            final CatCatalogBean cat = CatalogUtils.getCatalog(projectPath, catRes);
            final File catFile = CatalogUtils.getCatalogFile(projectPath, catRes);

            if (cat != null) {
                boolean modified = false;

                if (addUnreferencedFiles) {//check for files in the proper resource directory, but not referenced from the existing xml
                    if (CatalogUtils.addUnreferencedFiles(catFile, cat, user, now.getEventId())) {
                        modified = true;
                    }
                }

                if (CatalogUtils.formalizeCatalog(cat, catFile.getParent(), user, now, checksums, removeMissingFiles)) {
                    modified = true;
                }

                if (modified) {
                    try {
                        CatalogUtils.writeCatalogToFile(cat, catFile, checksums);
                    } catch (Exception e) {
                        throw new ServerException("An error occurred writing the catalog file " + catFile.getAbsolutePath(), e);
                    }
                }

                // populate (or repopulate) the file stats --- THIS SHOULD BE DONE AFTER modifications to the catalog xml
                if (populateStats || modified) {
                    if (CatalogUtils.populateStats(resource, projectPath) || modified) {
                        try {
                            resource.save(user, false, false, now);
                        } catch (Exception e) {
                            throw new ServerException("An error occurred saving the resource " + resource.getFullPath(projectPath), e);
                        }
                    }
                }

            }
        } else if (populateStats) {
            if (CatalogUtils.populateStats(resource, projectPath)) {
                try {
                    resource.save(user, false, false, now);
                } catch (Exception e) {
                    throw new ServerException("An error occurred saving the resource " + resource.getFullPath(projectPath), e);
                }
            }
        }

    }

    private CatCatalogI getFromCache(final UserI user, final String catalogId) {
        final Element cached = _cache.get(String.format(CATALOG_CACHE_KEY_FORMAT, catalogId));
        if (cached == null) {
            return null;
        }
        final File file = (File) cached.getObjectValue();
        if (file.exists()) {
            return CatalogUtils.getCatalog(file);
        }
        final File cacheFile = Users.getUserCacheFile(user, "catalogs", catalogId + ".xml");
        if (cacheFile.exists()) {
            return CatalogUtils.getCatalog(cacheFile);
        }
        return null;
    }

    private void storeToCache(final UserI user, final CatCatalogBean catalog) {
        final String catalogId = catalog.getId();
        final File file = Users.getUserCacheFile(user, "catalogs", catalogId + ".xml");
        file.getParentFile().mkdirs();

        try {
            try (final FileWriter writer = new FileWriter(file)) {
                catalog.toXML(writer, true);
            }
            _cache.put(new Element(String.format(CATALOG_CACHE_KEY_FORMAT, catalogId), file));
        } catch (IOException e) {
            _log.error("An error occurred writing the catalog " + catalogId + " for user " + user.getLogin(), e);
        }

    }

    private CatCatalogI getSessionsByScanTypesAndFormats(final String subject, final String label, final String session, final List<String> scanTypes, final List<String> scanFormats, final DownloadArchiveOptions options) {
        if (StringUtils.isBlank(session)) {
            throw new NrgServiceRuntimeException(NrgServiceError.Uninitialized, "Got a blank session to retrieve, that shouldn't happen.");
        }
        final CatCatalogBean catalog = new CatCatalogBean();
        try {
            final StringBuilder query = new StringBuilder("SELECT id FROM xnat_imagescandata WHERE image_session_id = '").append(session).append("'");
            if (scanTypes != null && scanTypes.size() > 0) {
                query.append(" AND ").append(getTypeClause(scanTypes));
            }
            final List<String> scans = _template.queryForList(query.toString(), String.class);
            if (scans.size() == 0) {
                return null;
            }

            catalog.setId("RAW");

            final boolean hasFormatLimits = scanFormats != null && scanFormats.size() > 0;
            if (hasFormatLimits) {
                final MapSqlParameterSource parameters = new MapSqlParameterSource();
                parameters.addValue("sessionId", session);
                parameters.addValue("scanFormats", scanFormats);
                final List<Map<String, Object>> existing = _parameterized.queryForList(QUERY_SCAN_FORMATS, parameters);

                for (final Map<String, Object> result : existing) {
                    final CatEntryBean entry = new CatEntryBean();
                    final String scan = (String) result.get("scan");
                    final String resource = URLEncoder.encode((String) result.get("resource"), "UTF-8");
                    entry.setName(getPath(options, subject, label, "scans", scan, "resources", resource));
                    entry.setUri("/archive/experiments/" + session + "/scans/" + scan + "/resources/" + resource + "/files");
                    catalog.addEntries_entry(entry);
                }
            } else {
                for (final String scan : scans) {
                    final CatEntryBean entry = new CatEntryBean();
                    entry.setName(getPath(options, subject, label, "scans", scan));
                    entry.setUri("/archive/experiments/" + session + "/scans/" + scan + "/files");
                    catalog.addEntries_entry(entry);
                }
            }
        } catch (UnsupportedEncodingException ignored) {
            //
        }
        return catalog;
    }

    private CatCatalogI getRelatedData(final String subject, final String label, final String session, final List<String> resources, final String type, final DownloadArchiveOptions options) {
        if (resources == null || resources.size() == 0) {
            return null;
        }

        final CatCatalogBean catalog = new CatCatalogBean();
        catalog.setId(StringUtils.upperCase(type));

        // Limit the resource URIs to those associated with the current session.
        final MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("sessionId", session);
        parameters.addValue("resourceIds", resources);
        final List<String> existing = _parameterized.query(QUERY_SESSION_RESOURCES, parameters, new RowMapper<String>() {
            @Override
            public String mapRow(final ResultSet resultSet, final int rowNum) throws SQLException {
                return resultSet.getString("resource");
            }
        });

        try {
            for (final String resource : existing) {
                final CatEntryBean entry = new CatEntryBean();
                final String resourceId = URLEncoder.encode(resource, "UTF-8");
                entry.setName(getPath(options, subject, label, "resources", resourceId));
                entry.setUri("/archive/experiments/" + session + "/resources/" + resourceId + "/files");
                catalog.addEntries_entry(entry);
            }
        } catch (UnsupportedEncodingException ignored) {
            //
        }
        return catalog;
    }

    private String getPath(final DownloadArchiveOptions options, final String subject, final String element, final String... elements) {
        final Path base;
        if (options.isSimplified()) {
            final String[] reduced = new String[elements.length / 2];
            for (int index = 1; index < elements.length; index += 2) {
                reduced[(index - 1) / 2] = elements[index];
            }
            base = Paths.get(element, reduced);
        } else {
            base = Paths.get(element, elements);
        }
        if (options.isProjectIncludedInPath()) {
            return Paths.get(options.getProject(), subject).resolve(base).toString();
        } else if (options.isSubjectIncludedInPath()) {
            return Paths.get(subject).resolve(base).toString();
        } else {
            return base.toString();
        }
    }

    private void addSafeEntrySet(final CatCatalogI catalog, final CatCatalogI innerCatalog) {
        try {
            catalog.addSets_entryset(innerCatalog);
        } catch (Exception e) {
            _log.warn("An error occurred trying to add a catalog entry to another catalog.", e);
        }
    }

    private String getTypeClause(final List<String> requestScanTypes) {
        final StringBuilder buffer = new StringBuilder();
        boolean foundNull = false;
        for (String item : requestScanTypes) {
            if (StringUtils.isBlank(item) || item.equalsIgnoreCase("NULL")) {
                foundNull = true;
                continue;
            }
            buffer.append("'").append(item).append("', ");
        }
        final int length = buffer.length();
        if (length == 0 && !foundNull) {
            throw new RuntimeException("Bad state found: no scan types specified, not even NULL!");
        }
        if (length > 0) {
            buffer.delete(length - 2, length);
        }
        if (foundNull) {
            if (length > 0) {
                buffer.insert(0, "(type IN (");
                buffer.append(") OR type is NULL)");
            } else {
                buffer.append("type is NULL");
            }
        } else {
            buffer.insert(0, "type IN (");
            buffer.append(")");
        }
        return buffer.toString();
    }

    private static Collection<Operation> getOperations(final Collection<Operation> operations) {
        // The default is All, so if they specified nothing, give them all.
        if (operations == null || operations.size() == 0) {
            return Operation.ALL;
        }

        // If ANY of the operations are All, give them all as well.
        for (final Operation operation : operations) {
            if (operation == Operation.All) {
                return Operation.ALL;
            }
        }

        // If All isn't specified, just return a list of the actual values.
        return operations;
    }

    private static class DownloadArchiveOptions {
        DownloadArchiveOptions(final List<String> options) {
            _project = getProjectFromOptions(options);
            _description = Joiner.on(", ").join(options);
            _projectIncludedInPath = options.contains("projectIncludedInPath");
            _subjectIncludedInPath = options.contains("subjectIncludedInPath");
            _simplified = options.contains("simplified");
        }

        private String getProjectFromOptions(final List<String> options) {
            for (final String option : options) {
                if (option.startsWith("project:")) {
                    return option.split(":")[1];
                }
            }
            return null;
        }

        public String getProject() {
            return _project;
        }

        public String getDescription() {
            return _description;
        }

        public boolean isProjectIncludedInPath() {
            return _projectIncludedInPath;
        }

        public boolean isSubjectIncludedInPath() {
            return _subjectIncludedInPath;
        }

        public boolean isSimplified() {
            return _simplified;
        }

        private final String  _project;
        private final String  _description;
        private final boolean _projectIncludedInPath;
        private final boolean _subjectIncludedInPath;
        private final boolean _simplified;
    }

    private static final String CATALOG_FORMAT           = "%s-%s";
    private static final String CATALOG_SERVICE_CACHE    = DefaultCatalogService.class.getSimpleName() + "Cache";
    private static final String CATALOG_CACHE_KEY_FORMAT = DefaultCatalogService.class.getSimpleName() + ".%s";
    private static final String QUERY_SESSION_RESOURCES  = "SELECT res.label resource FROM xnat_abstractresource res LEFT JOIN xnat_experimentdata_resource exptRes ON exptRes.xnat_abstractresource_xnat_abstractresource_id = res.xnat_abstractresource_id LEFT JOIN xnat_experimentdata expt ON expt.id = exptRes.xnat_experimentdata_id WHERE expt.ID = :sessionId AND res.label in (:resourceIds)";
    private static final String QUERY_SCAN_FORMATS       = "SELECT scan.id scan, res.label resource FROM xnat_imagescandata scan JOIN xnat_abstractResource res ON scan.xnat_imagescandata_id = res.xnat_imagescandata_xnat_imagescandata_id WHERE scan.image_session_id = :sessionId AND res.label IN (:scanFormats) ORDER BY scan";

    private static final Logger              _log      = LoggerFactory.getLogger(DefaultCatalogService.class);
    private static final Map<String, String> EMPTY_MAP = ImmutableMap.of();

    private final JdbcTemplate               _template;
    private final NamedParameterJdbcTemplate _parameterized;
    private final Cache                      _cache;
    private final String                     _archiveRoot;
}
