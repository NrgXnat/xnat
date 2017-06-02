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
import com.google.common.collect.*;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
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
import org.nrg.xnat.turbine.utils.ArchivableItem;
import org.nrg.xnat.utils.CatalogUtils;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    public DefaultCatalogService(final NamedParameterJdbcTemplate parameterized, final CacheManager cacheManager) {
        _parameterized = parameterized;
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
    }

    @Override
    public String buildCatalogForResources(final UserI user, final Map<String, List<String>> resourceMap) throws InsufficientPrivilegesException {
        final CatCatalogBean catalog = new CatCatalogBean();
        catalog.setId(String.format(CATALOG_FORMAT, user.getLogin(), getPrearchiveTimestamp()));

        final DownloadArchiveOptions options = DownloadArchiveOptions.getOptions(resourceMap.get("options"));
        catalog.setDescription(options.getDescription());

        final List<String> sessions = resourceMap.get("sessions");
        final List<String> scanTypes = resourceMap.get("scan_types");
        final List<String> scanFormats = resourceMap.get("scan_formats");
        final List<String> resources = resourceMap.get("resources");
        final List<String> reconstructions = resourceMap.get("reconstructions");
        final List<String> assessors = resourceMap.get("assessors");

        //Unescape scan types and formats so that special characters will not lead to 403s (for example if there is a degree sign in a scan type)
        ArrayList<String> unescapedScanTypes = new ArrayList<>();
        ArrayList<String> unescapedScanFormats = new ArrayList<>();

        if(scanTypes==null) {
            unescapedScanTypes.add("");
        }
        else{
            for(String type: scanTypes){
                if(type!=null){
                    unescapedScanTypes.add(StringEscapeUtils.unescapeHtml4(type));
                }
            }
        }
        if(scanFormats==null) {
            unescapedScanFormats.add("");
        }
        else{
            for(String format: scanFormats){
                if(format!=null){
                    unescapedScanFormats.add(StringEscapeUtils.unescapeHtml4(format));
                }
            }
        }

        final Map<String, Map<String, Map<String, String>>> projects = parseAndVerifySessions(user, sessions, unescapedScanTypes, unescapedScanFormats);

        for (final String project : projects.keySet()) {
            final Map<String, Map<String, String>> subjects = projects.get(project);
            for (final String subject : subjects.keySet()) {
                final Map<String, String> sessionMap = subjects.get(subject);
                final Set<String> sessionIds = sessionMap.keySet();
                for (final String sessionId : sessionIds) {
                    final String label = sessionMap.get(sessionId);

                    final CatCatalogBean sessionCatalog = new CatCatalogBean();
                    sessionCatalog.setId(sessionId);
                    sessionCatalog.setDescription(subject + " " + label);

                    final CatCatalogI sessionsByScanTypesAndFormats = getSessionScans(project, subject, label, sessionId, unescapedScanTypes, unescapedScanFormats, options);
                    if (sessionsByScanTypesAndFormats != null) {
                        addSafeEntrySet(sessionCatalog, sessionsByScanTypesAndFormats);
                    }

                    final CatCatalogI resourcesCatalog = getRelatedData(project, subject, label, sessionId, resources, "resources", options);
                    if (resourcesCatalog != null) {
                        addSafeEntrySet(sessionCatalog, resourcesCatalog);
                    }

                    final CatCatalogI reconstructionsCatalog = getRelatedData(project, subject, subject, sessionId, reconstructions, "reconstructions", options);
                    if (reconstructionsCatalog != null) {
                        addSafeEntrySet(sessionCatalog, reconstructionsCatalog);
                    }

                    final CatCatalogI assessorsCatalog = getSessionAssessors(project, subject, sessionId, assessors, options, user);
                    if (assessorsCatalog != null) {
                        addSafeEntrySet(sessionCatalog, assessorsCatalog);
                    }

                    catalog.addSets_entryset(sessionCatalog);
                }
            }
        }

        storeToCache(user, catalog);

        return catalog.getId();
    }

    @Override
    public CatCatalogI getCachedCatalog(final UserI user, final String catalogId) throws InsufficientPrivilegesException {
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
        final Element cached = _cache.get(String.format(CATALOG_CACHE_KEY_FORMAT, user.getUsername(), catalogId));
        if (cached == null) {
            final File cacheFile = Users.getUserCacheFile(user, "catalogs", catalogId + ".xml");
            if (cacheFile.exists()) {
                final CatCatalogBean catalog = CatalogUtils.getCatalog(cacheFile);
                storeToCache(user, catalog);
                return catalog;
            }
        } else {
            final File file = (File) cached.getObjectValue();
            if (file.exists()) {
                return CatalogUtils.getCatalog(file);
            }
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
            _cache.put(new Element(String.format(CATALOG_CACHE_KEY_FORMAT, user.getUsername(), catalogId), file));
        } catch (IOException e) {
            _log.error("An error occurred writing the catalog " + catalogId + " for user " + user.getLogin(), e);
        }

    }

    private Map<String, Map<String, Map<String, String>>> parseAndVerifySessions(final UserI user, final List<String> sessions, final List<String> scanTypes, final List<String> scanFormats) throws InsufficientPrivilegesException {
        final Multimap<String, String> matchingSessions = ArrayListMultimap.create();
        final Map<String, String[]> subjectLabelMap = Maps.newHashMap();
        final Map<String, Map<String, Map<String, String>>> sessionMap = Maps.newHashMap();
        for (final String sessionInfo : sessions) {
            final String[] atoms = sessionInfo.split(":");
            final String projectId = atoms[0];
            final String subject = atoms[1];
            final String label = atoms[2];
            final String sessionId = atoms[3];

            matchingSessions.put(projectId, sessionId);
            subjectLabelMap.put(projectId + ":" + sessionId, new String[]{subject, label});
        }

        for (final String projectId : matchingSessions.keySet()) {
            // First, can the user access the project they specified for the session at all?
            try {
                if (!Permissions.canReadProject(user, projectId)) {
                    throw new InsufficientPrivilegesException(user.getUsername(), projectId);
                }
            } catch (InsufficientPrivilegesException e) {
                throw e;
            } catch (Exception e) {
                _log.error("An unexpected error occurred while trying to resolve read access for user " + user.getUsername() + " on project " + projectId);
                throw new NrgServiceRuntimeException(NrgServiceError.Unknown, e);
            }

            final Set<String> sessionIds = new HashSet<>(matchingSessions.get(projectId));

            // Now verify that the experiment is either in or shared into the specified project.
            final MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("sessionIds", sessionIds);
            parameters.addValue("projectId", projectId);



            parameters.addValue("scanTypes", scanTypes);
            parameters.addValue("scanFormats", scanFormats);

            final Set<String> matching = new HashSet<>(_parameterized.queryForList(QUERY_FIND_SESSIONS_BY_TYPE_AND_FORMAT, parameters, String.class));
            final Set<String> difference = Sets.difference(sessionIds, matching);
            if (difference.size() > 0) {
                //Check whether the mismatch was merely due to the lack of scans for those sessions.
                final MapSqlParameterSource scanParameters = new MapSqlParameterSource();
                scanParameters.addValue("sessionIds", difference);
                scanParameters.addValue("scanTypes", scanTypes);
                scanParameters.addValue("scanFormats", scanFormats);
                final Set<String> matchingScans = new HashSet<>(_parameterized.queryForList(QUERY_FIND_SCANS_BY_SESSION, scanParameters, String.class));
                if(matchingScans.size()>0) {
                    //The mismatch was not entirely due to sessions not having scans of those types/formats.
                    throw new InsufficientPrivilegesException(user.getUsername(), difference);
                }
            }

            for (final String sessionId : sessionIds) {
                if (matchingSessions.get(projectId).contains(sessionId)) {
                    final Map<String, Map<String, String>> projectMap;
                    if (sessionMap.containsKey(projectId)) {
                        projectMap = sessionMap.get(projectId);
                    } else {
                        projectMap = new HashMap<>();
                        sessionMap.put(projectId, projectMap);
                    }
                    final String[] subjectLabel = subjectLabelMap.get(projectId + ":" + sessionId);
                    final String subject = subjectLabel[0];
                    final String label = subjectLabel[1];
                    final Map<String, String> subjectMap;
                    if (projectMap.containsKey(subject)) {
                        subjectMap = projectMap.get(subject);
                    } else {
                        subjectMap = new HashMap<>();
                        projectMap.put(subject, subjectMap);
                    }
                    subjectMap.put(sessionId, label);
                }
            }
        }

        return sessionMap;
    }

    private CatCatalogI getSessionScans(final String project, final String subject, final String label, final String session, final List<String> scanTypes, final List<String> scanFormats, final DownloadArchiveOptions options) {
        if (StringUtils.isBlank(session)) {
            throw new NrgServiceRuntimeException(NrgServiceError.Uninitialized, "Got a blank session to retrieve, that shouldn't happen.");
        }
        final CatCatalogBean catalog = new CatCatalogBean();
        catalog.setId("RAW");
        try {
            if((scanFormats == null || scanFormats.size() <= 0)||(scanTypes == null || scanTypes.size() <= 0)){
                return null;
            }

            final MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("sessionId", session);
            parameters.addValue("scanTypes", scanTypes);
            parameters.addValue("scanFormats", scanFormats);
            final String query = QUERY_FIND_SCANS_BY_TYPE_AND_FORMAT;

            final List<Map<String, Object>> scans = _parameterized.queryForList(query, parameters);
            if (scans.isEmpty()) {
                return null;
            }

            for (final Map<String, Object> scan : scans) {
                final CatEntryBean entry = new CatEntryBean();
                final String scanId = (String) scan.get("scan_id");
                final String resource = URLEncoder.encode((String) scan.get("resource"), "UTF-8");
                entry.setName(getPath(options, project, subject, label, "scans", scanId, "resources", resource));
                entry.setUri("/archive/experiments/" + session + "/scans/" + scanId + "/resources/" + resource + "/files");
                _log.debug("Created session scan entry for project {} session {} scan {} ({}) with name {}: {}", project, session, scanId, resource, entry.getName(), entry.getUri());
                catalog.addEntries_entry(entry);
            }
        } catch (UnsupportedEncodingException ignored) {
            //
        }

        return catalog.getEntries_entry().size() > 0 ? catalog : null;
    }

    private CatCatalogI getRelatedData(final String project, final String subject, final String label, final String session, final List<String> resources, final String type, final DownloadArchiveOptions options) {
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
                entry.setName(getPath(options, project, subject, label, "resources", resourceId));
                entry.setUri("/archive/experiments/" + session + "/resources/" + resourceId + "/files");
                _log.debug("Created resource entry for project {} session {} resource {} of type {} with name {}: {}", project, session, resource, type, entry.getName(), entry.getUri());
                catalog.addEntries_entry(entry);
            }
        } catch (UnsupportedEncodingException ignored) {
            //
        }

        return catalog.getEntries_entry().size() > 0 ? catalog : null;
    }

    private CatCatalogI getSessionAssessors(final String project, final String subject, final String sessionId, final List<String> assessorTypes, final DownloadArchiveOptions options, final UserI user) {
        if (assessorTypes == null || assessorTypes.size() == 0) {
            return null;
        }

        final CatCatalogBean catalog = new CatCatalogBean();
        catalog.setId(StringUtils.upperCase("assessors"));

        // Limit the resource URIs to those associated with the current session.
        final MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("sessionId", sessionId);
        parameters.addValue("assessorTypes", assessorTypes);
        final List<Map<String, Object>> resources = _parameterized.queryForList(QUERY_SESSION_ASSESSORS, parameters);

        try {
            for (final Map<String, Object> resource : resources) {
                final CatEntryBean entry = new CatEntryBean();
                final String resourceLabel = URLEncoder.encode(resource.get("resource_label").toString(), "UTF-8");
                final String assessorLabel = URLEncoder.encode(resource.get("assessor_label").toString(), "UTF-8");
                final String sessionLabel = URLEncoder.encode(resource.get("session_label").toString(), "UTF-8");
                final String proj = URLEncoder.encode(resource.get("project").toString(), "UTF-8");
                try{
                    if(Permissions.canReadProject(user,proj)) {
                        entry.setName(getPath(options, project, subject, sessionLabel, "assessors", assessorLabel, "resources", resourceLabel));
                        entry.setUri(StrSubstitutor.replace("/archive/experiments/${session_id}/assessors/${assessor_id}/out/resources/${resource_label}/files", resource));
                        _log.debug("Created session assessor entry for project {} session {} assessor {} resource {} with name {}: {}", project, sessionId, assessorLabel, resourceLabel, entry.getName(), entry.getUri());
                        catalog.addEntries_entry(entry);
                    }
                }
                catch(Exception e){
                    _log.warn("An error occurred trying to get session assessors for a project.", e);
                }
            }
        } catch (UnsupportedEncodingException ignored) {
            //
        }

        return catalog.getEntries_entry().size() > 0 ? catalog : null;
    }

    private String getPath(final DownloadArchiveOptions options, final String project, final String subject, final String element, final String... elements) {
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
            return Paths.get(project, subject).resolve(base).toString();
        } else if (options.isSubjectIncludedInPath()) {
            return Paths.get(subject).resolve(base).toString();
        } else {
            return base.toString();
        }
    }

    private void addSafeEntrySet(final CatCatalogI catalog, final CatCatalogI innerCatalog) {
        try {
            innerCatalog.setDescription(catalog.getDescription() + " " + innerCatalog.getId());
            catalog.addSets_entryset(innerCatalog);
        } catch (Exception e) {
            _log.warn("An error occurred trying to add a catalog entry to another catalog.", e);
        }
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

    private static final String CATALOG_FORMAT                         = "%s-%s";
    private static final String CATALOG_SERVICE_CACHE                  = DefaultCatalogService.class.getSimpleName() + "Cache";
    private static final String CATALOG_CACHE_KEY_FORMAT               = DefaultCatalogService.class.getSimpleName() + ".%s.%s";
    private static final String QUERY_FIND_SCANS_BY_SESSION           = "SELECT DISTINCT image_session_id FROM xnat_imagescandata scan " +
                                                                            "LEFT JOIN xnat_abstractResource res ON scan.xnat_imagescandata_id = res.xnat_imagescandata_xnat_imagescandata_id " +
                                                                            "WHERE image_session_id IN (:sessionIds) AND scan.type IN (:scanTypes) AND res.label IN (:scanFormats);";
    private static final String QUERY_FIND_SESSIONS_BY_TYPE_AND_FORMAT = "SELECT DISTINCT scan.image_session_id AS session_id "
                                                                         + "FROM xnat_imagescandata scan "
                                                                         + "  LEFT JOIN xnat_abstractResource res ON scan.xnat_imagescandata_id = res.xnat_imagescandata_xnat_imagescandata_id "
                                                                         + "  LEFT JOIN xnat_imagesessiondata session ON scan.image_session_id = session.id "
                                                                         + "  LEFT JOIN xnat_experimentdata expt ON session.id = expt.id "
                                                                         + "  LEFT JOIN xnat_experimentdata_share share ON share.sharing_share_xnat_experimentda_id = expt.id "
                                                                         + "WHERE expt.id IN (:sessionIds) AND "
                                                                         + "      (share.project = :projectId OR expt.project = :projectId) AND "
                                                                         + "      scan.type IN (:scanTypes) "
                                                                         + "      AND res.label IN (:scanFormats);";
    private static final String QUERY_FIND_SCANS_BY_TYPE               = "SELECT "
                                                                         + "  scan.id   AS scan_id, "
                                                                         + "  res.label AS resource "
                                                                         + "FROM xnat_imagescandata scan "
                                                                         + "  JOIN xnat_abstractResource res ON scan.xnat_imagescandata_id = res.xnat_imagescandata_xnat_imagescandata_id "
                                                                         + "WHERE scan.image_session_id = :sessionId AND scan.type IN (:scanTypes) "
                                                                         + "ORDER BY scan";
    private static final String QUERY_FIND_SCANS_BY_TYPE_AND_FORMAT    = "SELECT "
                                                                         + "  scan.id   AS scan_id, "
                                                                         + "  res.label AS resource "
                                                                         + "FROM xnat_imagescandata scan "
                                                                         + "  JOIN xnat_abstractResource res ON scan.xnat_imagescandata_id = res.xnat_imagescandata_xnat_imagescandata_id "
                                                                         + "WHERE scan.image_session_id = :sessionId AND scan.type IN (:scanTypes) AND res.label IN (:scanFormats) "
                                                                         + "ORDER BY scan";
    private static final String QUERY_SESSION_RESOURCES                = "SELECT res.label resource "
                                                                         + "FROM xnat_abstractresource res  "
                                                                         + "  LEFT JOIN xnat_experimentdata_resource exptRes "
                                                                         + "    ON exptRes.xnat_abstractresource_xnat_abstractresource_id = res.xnat_abstractresource_id "
                                                                         + "  LEFT JOIN xnat_experimentdata expt ON expt.id = exptRes.xnat_experimentdata_id "
                                                                         + "WHERE expt.ID = :sessionId AND res.label IN (:resourceIds);";
    private static final String QUERY_SESSION_ASSESSORS                = "SELECT "
                                                                         + "  abstract.xnat_abstractresource_id AS resource_id, "
                                                                         + "  abstract.label AS resource_label, "
                                                                         + "  assessor.id AS assessor_id, "
                                                                         + "  assessor.label AS assessor_label, "
                                                                         + "  session.id AS session_id, "
                                                                         + "  session.label AS session_label, "
                                                                         + "  assessor.project AS project "
                                                                         + "FROM xnat_abstractresource abstract "
                                                                         + "  LEFT JOIN img_assessor_out_resource imgOut "
                                                                         + "    ON imgOut.xnat_abstractresource_xnat_abstractresource_id = abstract.xnat_abstractresource_id "
                                                                         + "  LEFT JOIN xnat_imageassessordata imgAssessor ON imgOut.xnat_imageassessordata_id = imgAssessor.id "
                                                                         + "  LEFT JOIN xnat_experimentdata assessor ON assessor.id = imgAssessor.id "
                                                                         + "  LEFT JOIN xnat_experimentdata session ON session.id = imgAssessor.imagesession_id "
                                                                         + "  LEFT JOIN xdat_meta_element xme ON assessor.extension = xme.xdat_meta_element_id "
                                                                         + "WHERE xme.element_name IN (:assessorTypes) AND "
                                                                         + "      session.id = :sessionId";

    private static final Logger              _log      = LoggerFactory.getLogger(DefaultCatalogService.class);
    private static final Map<String, String> EMPTY_MAP = ImmutableMap.of();

    private final NamedParameterJdbcTemplate _parameterized;
    private final Cache                      _cache;
}
