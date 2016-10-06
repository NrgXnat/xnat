/*
 * web: org.nrg.xnat.services.archive.impl.legacy.DefaultCatalogService
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *  
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.archive.impl.legacy;

import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnat.turbine.utils.ArchivableItem;
import org.nrg.xnat.utils.CatalogUtils;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * {@inheritDoc}
 */
@Service
public class DefaultCatalogService implements CatalogService {
    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshResourceCatalog(final UserI user, final String resource, final Operation... operations) throws ActionException {
        _refreshCatalog(user, resource, Arrays.asList(operations));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshResourceCatalog(final UserI user, final String resource, final Collection<Operation> operations) throws ActionException {
        _refreshCatalog(user, resource, operations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshResourceCatalogs(final UserI user, final List<String> resources, final Operation... operations) throws ActionException {
        for (final String resource : resources) {
            _refreshCatalog(user, resource, Arrays.asList(operations));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshResourceCatalogs(final UserI user, final List<String> resources, final Collection<Operation> operations) throws ActionException {
        for (final String resource : resources) {
            _refreshCatalog(user, resource, operations);
        }
    }

    /**
     * Performs the actual work of refreshing a single catalog.
     *
     * @param user          The user requesting the refresh operation.
     * @param resource      The archive path for the resource to refresh.
     * @param operations    The operations to be performed.
     *
     * @throws ActionException When an error occurs during the refresh operation.
     */
    private void _refreshCatalog(final UserI user, final String resource, final Collection<Operation> operations) throws ActionException {
        try {
            //parse passed URI parameter
            final URIManager.DataURIA uri = UriParserUtils.parseURI(resource);

            if (!(uri instanceof URIManager.ArchiveItemURI)) {
                throw new ClientException("Invalid Resource URI:" + resource);
            }

            URIManager.ArchiveItemURI resourceURI = (URIManager.ArchiveItemURI) uri;

            ArchivableItem existenceCheck = resourceURI.getSecurityItem();

            if (existenceCheck != null) {
                final EventDetails event = new EventDetails(EventUtils.CATEGORY.DATA, EventUtils.TYPE.PROCESS, "Catalog(s) Refreshed", "Refreshed catalog for resource " + resourceURI.getUri(), "");

                final Collection<Operation> list = getOperations(operations);

                final boolean append = list.contains(Operation.Append);
                final boolean checksum = list.contains(Operation.Checksum);
                final boolean delete = list.contains(Operation.Delete);
                final boolean populateStats = list.contains(Operation.PopulateStats);

                try {
                    if (resourceURI instanceof ResourceURII) {//if we are referencing a specific catalog, make sure it doesn't actually reference an individual file.
                        if (StringUtils.isNotEmpty(((ResourceURII) resourceURI).getResourceFilePath()) && !((ResourceURII) resourceURI).getResourceFilePath().equals("/")) {
                            throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, new Exception("This operation cannot be performed directly on a file URL"));
                        }
                    }

                    final ArchivableItem security = resourceURI.getSecurityItem();
                    try {
                        if (!Permissions.canEdit(user, security)) {
                            throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN, new Exception("Unauthorized attempt to add a file to " + resourceURI.getUri()));
                        }
                    } catch (ClientException e) {
                        throw e;
                    } catch (Exception e) {
                        _log.error("An error occurred trying to check the edit permissions for user " + user.getUsername(), e);
                    }

                    final PersistentWorkflowI wrk = PersistentWorkflowUtils.getOrCreateWorkflowData(null, user, resourceURI.getSecurityItem().getItem(), event);

                    for (XnatAbstractresourceI res : resourceURI.getResources(true)) {
                        final String archiveRootPath = resourceURI.getSecurityItem().getArchiveRootPath();
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

    private boolean refreshResourceCatalog(final XnatAbstractresource resource,
                                           final String projectPath,
                                           final boolean populateStats,
                                           final boolean checksums,
                                           final boolean removeMissingFiles,
                                           final boolean addUnreferencedFiles,
                                           final UserI user, final EventMetaI now) throws ServerException {
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
        if (operations.size() == 0) {
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

    private static final Logger _log = LoggerFactory.getLogger(DefaultCatalogService.class);
}
