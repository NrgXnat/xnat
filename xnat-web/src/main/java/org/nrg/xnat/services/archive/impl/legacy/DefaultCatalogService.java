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
import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xdat.security.helpers.Permissions;
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
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.nrg.xnat.restlet.util.XNATRestConstants.getPrearchiveTimestamp;

/**
 * {@inheritDoc}
 */
@Service
public class DefaultCatalogService implements CatalogService {
    @Override
    public String buildCatalogForResources(final UserI user, final Map<String, List<String>> resources) throws InsufficientPrivilegesException {
        final String catalogId = String.format(CATALOG_FORMAT, user.getLogin(), getPrearchiveTimestamp());
        final CatCatalogBean catalog = new CatCatalogBean();
        catalog.setId(catalogId);
        final List<String> sessions = resources.get("sessions");
        for (final String session : sessions) {
            final CatCatalogBean sessionCatalog = new CatCatalogBean();
            sessionCatalog.setId(session);
            catalog.addSets_entryset(sessionCatalog);
        }
        _catalogs.put(catalogId, catalog);
        return catalogId;
    }

    @Override
    public CatCatalogBean getCatalogForResources(final UserI user, final String catalogId) throws InsufficientPrivilegesException {
        return _catalogs.get(catalogId);
    }

    @Override
    public long getCatalogSize(final UserI user, final String catalogId) throws InsufficientPrivilegesException, IOException {
        final CatCatalogBean bean = _catalogs.get(catalogId);
        final StringWriter writer = new StringWriter();
        bean.toXML(writer, true);
        return writer.toString().length();
    }

    @Override
    public Map<String, Resource> getResourcesForCatalog(final UserI user, final String catalogId) throws InsufficientPrivilegesException, IOException {
        final Path parent = BasicXnatResourceLocator.getResource("classpath:testdata").getFile().toPath();
        final Map<String, Resource> resources = Maps.newHashMap();
        for (final Resource resource : BasicXnatResourceLocator.getResources("classpath*:testdata/**/*.dcm")) {
            resources.put(parent.relativize(resource.getFile().toPath()).toString(), resource);
        }
        return resources;
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

    private boolean refreshResourceCatalog(final XnatAbstractresource resource, final String projectPath, final boolean populateStats, final boolean checksums, final boolean removeMissingFiles, final boolean addUnreferencedFiles, final UserI user, final EventMetaI now) throws ServerException {
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
                            if (resource.save(user, false, false, now)) {
                                modified = true;
                            }
                        } catch (Exception e) {
                            throw new ServerException("An error occurred saving the resource " + resource.getFullPath(projectPath), e);
                        }
                    }
                }

                return modified;
            }
        } else if (populateStats) {
            if (CatalogUtils.populateStats(resource, projectPath)) {
                try {
                    return resource.save(user, false, false, now);
                } catch (Exception e) {
                    throw new ServerException("An error occurred saving the resource " + resource.getFullPath(projectPath), e);
                }
            }
        }

        return false;
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

    private static final String CATALOG_FORMAT = "%s-%s";

    private static final Logger              _log      = LoggerFactory.getLogger(DefaultCatalogService.class);
    private static final Map<String, String> EMPTY_MAP = ImmutableMap.of();

    private final Map<String, CatCatalogBean> _catalogs = Maps.newHashMap();
}
