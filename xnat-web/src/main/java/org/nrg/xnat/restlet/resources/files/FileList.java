/*
 * web: org.nrg.xnat.restlet.resources.files.FileList
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources.files;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.dcm.Dcm2Jpg;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.bean.CatEntryBean;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.file.StoredFile;
import org.nrg.xnat.helpers.resource.direct.ResourceModifierA;
import org.nrg.xnat.helpers.resource.direct.ResourceModifierA.UpdateMeta;
import org.nrg.xnat.presentation.ChangeSummaryBuilderA;
import org.nrg.xnat.restlet.files.utils.RestFileUtils;
import org.nrg.xnat.restlet.representations.BeanRepresentation;
import org.nrg.xnat.restlet.representations.JSONObjectRepresentation;
import org.nrg.xnat.restlet.representations.ZipRepresentation;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.services.cache.UserProjectCache;
import org.nrg.xnat.services.messaging.file.MoveStoredFileRequest;
import org.nrg.xnat.turbine.utils.ArchivableItem;
import org.nrg.xnat.utils.CatalogUtils;
import org.nrg.xnat.utils.CatalogUtils.CatEntryFilterI;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import static org.nrg.xnat.utils.CatalogUtils.*;

/**
 * @author timo
 */
@SuppressWarnings("RegExpRedundantEscape")
@Slf4j
public class FileList extends XNATCatalogTemplate {
    public FileList(Context context, Request request, Response response) throws ClientException {
        super(context, request, response, isQueryVariableTrue("all", request));
        reference = getQueryVariable("reference");
        acceptNotFound = isQueryVariableTrueHelper(getQueryVariable("accept-not-found"));
        delete = isQueryVariableTrue("delete", request);
        async = isQueryVariableTrue("async", request);
        notifyList = isQueryVariableTrue("notify", request) ? getQueryVariable("notify").split(",") : new String[0];
        try {
            final UserI user = getUser();
            if (!getResourceIds().isEmpty()) {
                final List<Integer> alreadyAdded = new ArrayList<>();
                if (hasCatalogs()) {
                    for (final Object[] row : getCatalogs().rows()) {
                        final Integer id    = (Integer) row[0];
                        final String  label = (String) row[1];
                        for (final String resourceId : getResourceIds()) {
                            if (!alreadyAdded.contains(id) && (id.toString().equals(resourceId) || (label != null && label.equals(resourceId)))) {
                                final XnatAbstractresource resource = XnatAbstractresource.getXnatAbstractresourcesByXnatAbstractresourceId(id, user, false);
                                if (row.length == 7) {
                                    resource.setBaseURI((String) row[6]);
                                }
                                if (proj == null || Permissions.canReadProject(user, proj.getId())) {
                                    getResources().add(resource);
                                    alreadyAdded.add(id);
                                }
                            }
                        }
                    }
                }

                // if caller is asking for the files directly by resource ID (e.g. /experiments/{EXPT_ID}/resources/{RESOURCE_ID}/files),
                // the catalog will not be found by the superclass
                // (unless caller passes all=true, which seems clunky to require given that they are passing in the resource PK).
                // So here we provide an alternate path finding the resource
                // added check to make sure it's an number.  You can also reference resource labels here (not just pks).
                for (final String resourceId : getResourceIds()) {
                    try {
                        final Integer id = Integer.parseInt(resourceId);
                        if (!alreadyAdded.contains(id)) {
                            final XnatAbstractresource resource = XnatAbstractresource.getXnatAbstractresourcesByXnatAbstractresourceId(id, user, false);
                            if (resource != null) {
                                final XnatImageassessordata assessor = getAssessor((XnatResourcecatalog) resource);
                                if ((proj == null || Permissions.canReadProject(user, proj.getId())) && (assessor == null || Permissions.canRead(user, assessor))) {
                                    getResources().add(resource);
                                }
                            }
                        }
                    } catch (NumberFormatException e) {
                        // ignore... this is probably a resource label
                    }
                }
            }

            if (!getResources().isEmpty()) {
                resource = getResources().get(0);
            }

            filePath = URLDecoder.decode(StringUtils.substringBefore(StringUtils.removeStart(getRequest().getResourceRef().getRemainingPart(), "/"), "?"), Charset.defaultCharset().name());

            getVariants().addAll(VARIANTS);
        } catch (Exception e) {
            log.error("Error occurred while initializing FileList service", e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e, "Error during service initialization");
        }
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    /**
     * ****************************************
     * if(filePath>"")then returns File
     * else returns table of files
     */
    @Override
    @SuppressWarnings("unchecked")
    public Representation represent(Variant variant) {
        final MediaType mediaType = overrideVariant(variant);
        try {
            if (proj == null) {
                //setting project as primary project, or shared project
                //this only works because the absolute paths are stored in the database for each resource, so the actual project path isn't used.
                if (parent != null && parent.getItem().instanceOf("xnat:experimentData")) {
                    proj = ((XnatExperimentdata) parent).getPrimaryProject(false);
                    // Per FogBugz 4746, prevent NPE when user doesn't have access to resource (MRH)
                    // Check access through shared project when user doesn't have access to primary project
                    if (proj == null) {
                        proj = (XnatProjectdata) ((XnatExperimentdata) parent).getFirstProject();
                    }
                } else if (security != null && security.getItem().instanceOf("xnat:experimentData")) {
                    proj = ((XnatExperimentdata) security).getPrimaryProject(false);
                    // Per FogBugz 4746, ....
                    if (proj == null) {
                        proj = (XnatProjectdata) ((XnatExperimentdata) security).getFirstProject();
                    }
                } else if (security != null && security.getItem().instanceOf("xnat:subjectData")) {
                    proj = ((XnatSubjectdata) security).getPrimaryProject(false);
                    // Per FogBugz 4746, ....
                    if (proj == null) {
                        proj = (XnatProjectdata) ((XnatSubjectdata) security).getFirstProject();
                    }
                } else if (security != null && security.getItem().instanceOf("xnat:projectData")) {
                    proj = (XnatProjectdata) security;
                }
            }

            final List<XnatAbstractresource> resources = getResources();
            if (resources.size() == 1 && !(isZIPRequest(mediaType))) {
                //one catalog
                return handleSingleCatalog(mediaType);
            }
            if (!resources.isEmpty()) {
                //multiple catalogs
                return handleMultipleCatalogs(mediaType);
            }
            try {
                // Check project access before iterating through all of the resources.
                if (proj == null || Permissions.canReadProject(getUser(), proj.getId())) {
                    //all catalogs
                    getCatalogs().resetRowCursor();
                    for (Hashtable<String, Object> rowHash : getCatalogs().rowHashs()) {
                        final XnatAbstractresource resource = XnatAbstractresource.getXnatAbstractresourcesByXnatAbstractresourceId(rowHash.get("xnat_abstractresource_id"), getUser(), false);
                        if (rowHash.containsKey("resource_path")) {
                            resource.setBaseURI((String) rowHash.get("resource_path"));
                        }
                        resources.add(resource);
                    }
                }
            } catch (Exception e) {
                log.error("Exception checking whether user has project access.", e);
            }

            return handleMultipleCatalogs(mediaType);
        } catch (ElementNotFoundException e) {
            if (acceptNotFound) {
                getResponse().setStatus(Status.SUCCESS_NO_CONTENT, "Unable to find file.");
            } else {
                log.error("", e);
                getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unable to find file.");
            }
            return new StringRepresentation("");
        }
    }

    @Override
    public void handlePut() {
        handlePost();
    }

    @Override
    public void handlePost() {
        if (parent != null && security != null) {
            try {
                final UserI user = getUser();
                if (Permissions.canEdit(user, security)) {
                    if (proj == null) {
                        if (parent.getItem().instanceOf("xnat:experimentData")) {
                            proj = ((XnatExperimentdata) parent).getPrimaryProject(false);
                        } else if (security.getItem().instanceOf("xnat:experimentData")) {
                            proj = ((XnatExperimentdata) security).getPrimaryProject(false);
                        } else if (parent.getItem().instanceOf("xnat:subjectData")) {
                            proj = ((XnatSubjectdata) parent).getPrimaryProject(false);
                        } else if (security.getItem().instanceOf("xnat:subjectData")) {
                            proj = ((XnatSubjectdata) security).getPrimaryProject(false);
                        }
                    }

                    final Object resourceIdentifier;

                    if (resource == null) {
                        if (getCatalogs().rows().size() > 0) {
                            resourceIdentifier = getCatalogs().getFirstObject();
                        } else {
                            if (!getResourceIds().isEmpty()) {
                                resourceIdentifier = getResourceIds().get(0);
                            } else {
                                resourceIdentifier = null;
                            }
                        }
                    } else {
                        resourceIdentifier = resource.getXnatAbstractresourceId();
                    }

                    final boolean overwrite = isQueryVariableTrue("overwrite");
                    final boolean extract   = isQueryVariableTrue("extract");

                    PersistentWorkflowI workflow = PersistentWorkflowUtils.getWorkflowByEventId(user, getEventId());
                    if (workflow == null && resource != null && "SNAPSHOTS".equals(resource.getLabel())) {
                        if (getSecurityItem() instanceof XnatExperimentdata) {
                            final Collection<? extends PersistentWorkflowI> workflows = PersistentWorkflowUtils.getOpenWorkflows(user, ((ArchivableItem) security).getId());
                            if (workflows != null && workflows.size() == 1) {
                                workflow = (WrkWorkflowdata) CollectionUtils.get(workflows, 0);
                                if (!"xnat_tools/AutoRun.xml".equals(workflow.getPipelineName())) {
                                    workflow = null;
                                }
                            }
                        }
                    }

                    final boolean skipUpdateStats = isQueryVariableFalse("update-stats");

                    boolean isNew = false;
                    if (workflow == null && !skipUpdateStats) {
                        isNew = true;
                        workflow = PersistentWorkflowUtils.buildOpenWorkflow(user, getSecurityItem().getItem(), newEventInstance(EventUtils.CATEGORY.DATA, (getAction() != null) ? getAction() : EventUtils.UPLOAD_FILE));
                    }

                    final EventMetaI eventMeta;
                    if (workflow == null) {
                        eventMeta = EventUtils.DEFAULT_EVENT(user, null);
                    } else {
                        eventMeta = workflow.buildEvent();
                    }

                    final UpdateMeta updateMeta = new UpdateMeta(eventMeta, !(skipUpdateStats));

                    try {
                        final List<FileWriterWrapperI> writers = getFileWriters();
                        if (writers == null || writers.isEmpty()) {
                            final String method = getRequest().getMethod().toString();
                            final long   size   = getRequest().getEntity().getAvailableSize();
                            if (size == 0) {
                                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "You tried to " + method + " to this service, but didn't provide any data (found request entity size of 0). Please check the format of your service request.");
                            } else {
                                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "You tried to " + method + " a payload of " + CatalogUtils.formatSize(size) + " to this service, but didn't provide any data. If you think you sent data to upload, you can try to " + method + " with the query-string parameter inbody=true or use multipart/form-data encoding.");
                            }
                            return;
                        }

                        final ResourceModifierA resourceModifier = buildResourceModifier(overwrite, updateMeta);
                        final String            projectId        = proj.getId();
                        if (!async || StringUtils.isBlank(reference)) {
                            final List<String> duplicates = resourceModifier.addFile(writers, resourceIdentifier, type, filePath, buildResourceInfo(updateMeta), extract);
                            if (!overwrite && duplicates.size() > 0) {
                                getResponse().setStatus(Status.SUCCESS_OK);
                                getResponse().setEntity(new JSONObjectRepresentation(MediaType.TEXT_HTML, new JSONObject(ImmutableMap.of("duplicates", duplicates))));
                                isNew = false;
                            } else {
                                getResponse().setStatus(Status.SUCCESS_OK);
                                getResponse().setEntity(new StringRepresentation("", MediaType.TEXT_PLAIN));
                            }

                            if (StringUtils.equals(XnatProjectdata.SCHEMA_ELEMENT_NAME, parent.getXSIType())) {
                                final UserProjectCache cache = XDAT.getContextService().getBeanSafely(UserProjectCache.class);
                                if (cache != null) {
                                    cache.clearProjectCacheEntry(projectId);
                                }
                                XDAT.triggerXftItemEvent(proj, XftItemEventI.UPDATE);
                            }
                        } else {
                            if (workflow == null) {
                                throw new Exception("Unexpected null workflow");
                            }

                            workflow.setStatus(PersistentWorkflowUtils.QUEUED);
                            WorkflowUtils.save(workflow, workflow.buildEvent());

                            final MoveStoredFileRequest request;
                            if (StringUtils.equals(XnatProjectdata.SCHEMA_ELEMENT_NAME, parent.getXSIType())) {
                                request = new MoveStoredFileRequest(resourceModifier, resourceIdentifier, writers, user, workflow.getWorkflowId(), delete, notifyList, type, filePath, buildResourceInfo(updateMeta), extract, projectId);
                            } else {
                                request = new MoveStoredFileRequest(resourceModifier, resourceIdentifier, writers, user, workflow.getWorkflowId(), delete, notifyList, type, filePath, buildResourceInfo(updateMeta), extract);
                            }
                            XDAT.sendJmsRequest(request);

                            getResponse().setStatus(Status.SUCCESS_OK);
                            getResponse().setEntity(new JSONObjectRepresentation(MediaType.TEXT_HTML, new JSONObject(ImmutableMap.of("workflowId", workflow.getWorkflowId()))));
                        }
                    } catch (Exception e) {
                        log.error("Error occurred while trying to POST file", e);
                        throw e;
                    }

                    if (StringUtils.isBlank(reference) && workflow != null && isNew) {
                        WorkflowUtils.complete(workflow, eventMeta);
                    }
                }
            } catch (IllegalArgumentException e) { // XNAT-2989
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
                log.error("", e);
            } catch (Exception e) {
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
                log.error("", e);
            }
        }
    }

    @Override
    public void handleDelete() {
        try {
            if (resource == null || parent == null || security == null) {
                throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST,
                        "Unable to determine resource, parent, or security.");
            }

            final UserI user = getUser();
            if (!Permissions.canDelete(user,security)) {
                throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN,
                        "User account doesn't have permission to modify this session.");
            }
            XFTItem item = resource.getItem();
            if (item.isLocked() || !item.isActive() && !item.isQuarantine()) {
                //cannot modify it if it isn't active
                throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN,
                        "Item locked or is not active and not quarantined");
            }

            if (!(resource instanceof XnatResourcecatalog)) {
                throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST,
                        "File is not an instance of XnatResourcecatalog. Delete operation not supported.");
            }

            if (proj == null) {
                if (parent.getItem().instanceOf("xnat:experimentData")) {
                    proj = ((XnatExperimentdata) parent).getPrimaryProject(false);
                } else if (security.getItem().instanceOf("xnat:experimentData")) {
                    proj = ((XnatExperimentdata) security).getPrimaryProject(false);
                }
            }

            final CatalogUtils.CatalogData catalogData = CatalogUtils.CatalogData.getOrCreate(proj.getRootArchivePath(), (XnatResourcecatalog) resource, proj.getId()
            );
            final Collection<CatEntryI> entries = CatalogUtils.findCatEntriesWithinPath(filePath, catalogData);

            if (entries.isEmpty()) {
                getResponse().setStatus(acceptNotFound ? Status.SUCCESS_NO_CONTENT : Status.CLIENT_ERROR_NOT_FOUND,
                        "No matched files");
                return;
            }

            PersistentWorkflowI work = WorkflowUtils.getOrCreateWorkflowData(getEventId(), user,
                    security.getItem(), newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.REMOVE_FILE));
            try {
                long catSize = catalogData.catRes.getFileSize() == null ? 0 : (Long) catalogData.catRes.getFileSize();
                Map<CatEntryI, File> historyMap = new HashMap<>();
                for (CatEntryI entry : entries) {
                    CatalogUtils.CatalogEntryPathInfo info = new CatalogUtils.CatalogEntryPathInfo(entry,
                            catalogData.catPath);
                    historyMap.put(entry, new File(info.entryPathDest));
                    catSize -= CatalogUtils.getCatalogEntrySize(entry);
                }

                int nremoved = entries.size();
                int fileCount = (catalogData.catRes.getFileCount() == null) ? 0 :
                        catalogData.catRes.getFileCount() - nremoved;

                EventMetaI ci = work.buildEvent();
                Map<String, Map<String, Integer>> auditSummary = new HashMap<>();
                CatalogUtils.addAuditEntry(auditSummary, Integer.parseInt(ci.getEventId().toString()),
                        Calendar.getInstance().getTime(), ChangeSummaryBuilderA.REMOVED, nremoved);

                // Perform remove on the catalog bean
                catalogData.catBean.getEntries_entry().removeAll(entries);

                // Write updated bean to the catalog, maintain history if appropriate, and remove files if requested
                CatalogUtils.saveUpdatedCatalog(catalogData, auditSummary, catSize, fileCount, ci, user,
                        historyMap, !isQueryVariableFalse("removeFiles"));

                if (StringUtils.equals(XnatProjectdata.SCHEMA_ELEMENT_NAME, parent.getXSIType())) {
                    XDAT.triggerXftItemEvent(XnatProjectdata.SCHEMA_ELEMENT_NAME, parent.getStringProperty("ID"),
                            XftItemEventI.DELETE);
                }
            } finally {
                WorkflowUtils.complete(work, work.buildEvent());
            }
        } catch (ClientException e) {
            getResponse().setStatus(e.getStatus(), e.getMessage());
        } catch (Exception e) {
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
        }
    }

    public Representation representTable(final XFTTable table, final MediaType mediaType, final Hashtable<String, Object> parameters, final Map<String, Map<String, String>> columnProperties, final Map<String, String> sessionMapping) {
        if (mediaType.equals(SecureResource.APPLICATION_XCAT)) {
            return buildXcatRepresentation(table, mediaType, sessionMapping);
        }
        if (isZIPRequest(mediaType)) {
            return buildZipRepresentation(table, mediaType, sessionMapping);
        }
        return super.representTable(table, mediaType, parameters, columnProperties);
    }

    public List<FileWriterWrapperI> getFileWritersAndLoadParams(final Representation entity, boolean useFileFieldName) throws FileUploadException, ClientException {
        if (StringUtils.isNotEmpty(reference)) {
            return getReferenceWrapper(reference);
        } else {
            return super.getFileWritersAndLoadParams(entity, useFileFieldName);
        }
    }

    private Representation handleMultipleCatalogs(final MediaType mediaType) throws ElementNotFoundException {
        final boolean           isZip    = isZIPRequest(mediaType);
        final Map<String, File> fileList = new HashMap<>();
        final XFTTable          table    = new XFTTable();

        final String[] headers = isZip ? CatalogUtils.FILE_HEADERS_W_FILE.clone() : CatalogUtils.FILE_HEADERS.clone();

        final String locator;
        final String queryVariable = getQueryVariable(LOCATOR);
        // NOTE:  zip representations must have URI
        if (isZip || !StringUtils.equalsAnyIgnoreCase(queryVariable, ABSOLUTE_PATH, PROJECT_PATH)) {
            locator = URI;
        } else {
            headers[ArrayUtils.indexOf(headers, URI)] = locator = StringUtils.equalsIgnoreCase(queryVariable, ABSOLUTE_PATH) ? ABSOLUTE_PATH : PROJECT_PATH;
        }

        table.initTable(headers);

        final String          baseURI     = getBaseURI();
        final CatEntryFilterI entryFilter = buildFilter();
        final Integer         index       = (containsQueryVariable("index")) ? Integer.parseInt(getQueryVariable("index")) : null;
        File                  file        = null;

        final String projectId = proj.getId();
        final String rootArchivePath = proj.getRootArchivePath();
        for (final XnatAbstractresource temp : getResources()) {
            if (temp.getItem().instanceOf("xnat:resourceCatalog")) {
                final boolean             includeRoot = isQueryVariableTrue("includeRootPath");
                final XnatResourcecatalog catResource = (XnatResourcecatalog) temp;
                final CatalogData         catalogData;
                try {
                    catalogData = CatalogData.getOrCreateAndClean(rootArchivePath, catResource, includeRoot, projectId);
                } catch (ServerException e) {
                    throw new ElementNotFoundException("xnat:resourceCatalog " + catResource.getUri());
                }
                final CatCatalogBean      cat         = catalogData.catBean;
                final String              parentPath  = catalogData.catPath;

                if (StringUtils.isBlank(filePath)) {
                    table.insertRows(CatalogUtils.getEntryDetails(cat, parentPath, (catResource.getBaseURI() != null) ? catResource.getBaseURI() + "/files" : baseURI + "/resources/" + catResource.getXnatAbstractresourceId() + "/files", catResource, isZip || (index != null), entryFilter, proj, locator));
                } else {
                    final List<CatEntryI> entries   = new ArrayList<>();
                    final CatEntryBean    entryBean = getCatalogEntry(cat);
                    if (entryBean != null) {
                        entries.add(entryBean);
                    }
                    if (entries.isEmpty() && filePath.endsWith("/")) {
                        entries.addAll(CatalogUtils.getEntriesByFilter(cat, getFolderFilter(entryFilter, parentPath)));
                    }
                    if (entries.size() == 0 && filePath.endsWith("*")) {
                        StringBuilder regex     = new StringBuilder(filePath);
                        int           lastIndex = filePath.lastIndexOf("*");
                        regex.replace(lastIndex, lastIndex + 1, ".*");
                        entries.addAll(CatalogUtils.getEntriesByRegex(cat, regex.toString()));
                    }


                    if (entries.size() == 1) {
                        file = CatalogUtils.getFile(entries.get(0), parentPath, projectId);
                        if (file != null && file.exists()) {
                            break;
                        }
                    } else {
                        for (final CatEntryI entry : entries) {
                            file = CatalogUtils.getFile(entry, parentPath, projectId);

                            if (file != null && file.exists()) {
                                fileList.put(CatalogUtils.getRelativePathForCatalogEntry(entry, parentPath), file);
                            }
                        }
                        break;
                    }
                }
            } else {
                //not catalog
                if (entryFilter == null) {
                    ArrayList<File> files = temp.getCorrespondingFiles(rootArchivePath);
                    if (files != null && files.size() > 0) {
                        final boolean checksums = XDAT.getSiteConfigPreferences().getChecksums();
                        for (final File subFile : files) {
                            final List<Object> row = Lists.newArrayList();
                            row.add(subFile.getName());
                            row.add(subFile.length());
                            if (locator.equalsIgnoreCase(URI)) {
                                row.add(temp.getBaseURI() != null ? temp.getBaseURI() + "/files/" + subFile.getName() : baseURI + "/resources/" + temp.getXnatAbstractresourceId() + "/files/" + subFile.getName());
                            } else if (locator.equalsIgnoreCase(ABSOLUTE_PATH)) {
                                row.add(subFile.getAbsolutePath());
                            } else if (locator.equalsIgnoreCase(PROJECT_PATH)) {
                                row.add(subFile.getAbsolutePath().substring(rootArchivePath.substring(0, rootArchivePath.lastIndexOf(proj.getId())).length()));
                            }
                            row.add(temp.getLabel());
                            row.add(temp.getTagString());
                            row.add(temp.getFormat());
                            row.add(temp.getContent());
                            row.add(temp.getXnatAbstractresourceId());
                            if (isZip) {
                                row.add(subFile);
                            }
                            row.add(checksums ? CatalogUtils.getHash(subFile) : "");
                            table.insertRow(row.toArray());
                        }
                    }
                }
            }
        }

        final String downloadName = security != null ? ((ArchivableItem) security).getArchiveDirectoryName() :
                getSessionMaps().get(Integer.toString(0));

        if (mediaType.equals(MediaType.APPLICATION_ZIP)) {
            setContentDisposition(downloadName + ".zip");
        } else if (mediaType.equals(MediaType.APPLICATION_GNU_TAR)) {
            setContentDisposition(downloadName + ".tar.gz");
        } else if (mediaType.equals(MediaType.APPLICATION_TAR)) {
            setContentDisposition(downloadName + ".tar");
        }

        if (StringUtils.isBlank(filePath) && index == null) {
            final Pair<Hashtable<String, Object>, Map<String, Map<String, String>>> parametersAndProperties = getDefaultParametersAndColumnProperties();
            parametersAndProperties.getRight().get(URI).put("serverRoot", StringUtils.removeEnd(StringUtils.removeEnd(getRequest().getRootRef().getPath(), "/data"), "/REST"));
            return representTable(table, mediaType, parametersAndProperties.getLeft(), parametersAndProperties.getRight(), getSessionMaps());
        }

        if (index != null && table.rows().size() > index) {
            file = (File) table.rows().get(index)[8];
        }

        if (file == null || !file.exists()) {
            getResponse().setStatus(acceptNotFound ? Status.SUCCESS_NO_CONTENT : Status.CLIENT_ERROR_NOT_FOUND, "Unable to find file.");
            return null;
        }

        final String name = file.getName();

        //return file
        final boolean mismatchedExtension = mediaType.equals(MediaType.APPLICATION_ZIP) && !name.toLowerCase().endsWith(".zip") ||
                mediaType.equals(MediaType.APPLICATION_GNU_TAR) && !name.toLowerCase().endsWith(".tar.gz") ||
                mediaType.equals(MediaType.APPLICATION_TAR) && !name.toLowerCase().endsWith(".tar");
        if (fileList.isEmpty()) {
            if (mismatchedExtension) {
                try {
                    final ZipRepresentation representation = new ZipRepresentation(mediaType, ((ArchivableItem) security).getArchiveDirectoryName(), identifyCompression(null));
                    representation.addEntry(name, file);
                    return representation;
                } catch (ActionException e) {
                    log.error("", e);
                    setResponseStatus(e);
                    return null;
                }
            } else {
                return getFileRepresentation(file, mediaType);
            }
        }
        if (mismatchedExtension) {
            try {
                final ZipRepresentation representation = new ZipRepresentation(mediaType, ((ArchivableItem) security).getArchiveDirectoryName(), identifyCompression(null));
                for (final String filename : fileList.keySet()) {
                    representation.addEntry(filename, fileList.get(filename));
                }
                return representation;
            } catch (ActionException e) {
                log.error("", e);
                setResponseStatus(e);
            }
        }
        return null;
    }

    private Representation handleSingleCatalog(final MediaType mediaType) throws ElementNotFoundException {
        File     file  = null;
        XFTTable table = new XFTTable();

        final String[] headers = CatalogUtils.FILE_HEADERS.clone();
        final String   locator;
        final String   queryVariable = getQueryVariable(LOCATOR);
        if (StringUtils.equalsAnyIgnoreCase(queryVariable, ABSOLUTE_PATH, PROJECT_PATH)) {
            headers[ArrayUtils.indexOf(headers, URI)] = locator = StringUtils.equalsIgnoreCase(queryVariable, ABSOLUTE_PATH) ? ABSOLUTE_PATH : PROJECT_PATH;
        } else {
            locator = URI;
        }
        table.initTable(headers);

        final CatalogUtils.CatEntryFilterI entryFilter = buildFilter();
        final Integer                      index       = (containsQueryVariable("index")) ? Integer.parseInt(getQueryVariable("index")) : null;

        final String projectId = proj.getId();
        final String rootArchivePath = proj.getRootArchivePath();

        if (resource.getItem().instanceOf("xnat:resourceCatalog")) {
            final boolean             includeRoot = isQueryVariableTrue("includeRootPath");
            final XnatResourcecatalog catResource = (XnatResourcecatalog) resource;
            final CatalogData         catalogData;
            try {
                catalogData = CatalogData.getOrCreateAndClean(rootArchivePath, catResource, includeRoot, projectId);
            } catch (ServerException e) {
                getResponse().setStatus(acceptNotFound ? Status.SUCCESS_NO_CONTENT : Status.CLIENT_ERROR_NOT_FOUND,
                        e.getMessage());
                return new StringRepresentation("");
            }

            final CatCatalogBean      cat         = catalogData.catBean;
            final String              parentPath  = catalogData.catPath;

            if (StringUtils.isBlank(filePath) && index == null) {
                String baseURI = getBaseURI();
                table.insertRows(CatalogUtils.getEntryDetails(cat, parentPath, baseURI + "/resources/" + catResource.getXnatAbstractresourceId() + "/files", catResource, false, entryFilter, proj, locator));
            } else {

                String zipEntryName = null;

                CatEntryI entry;
                if (index != null) {
                    entry = CatalogUtils.getEntryByFilter(cat, new CatEntryFilterI() {
                        private final AtomicInteger count = new AtomicInteger();

                        public boolean accept(final CatEntryI entry) {
                            if (entryFilter != null && entryFilter.accept(entry)) {
                                return index == count.getAndIncrement();
                            }
                            return false;
                        }
                    });
                } else {
                    for (final String raw : XDAT.getSiteConfigPreferences().getZipExtensionsAsArray()) {
                        final String  extension              = "." + raw;
                        final boolean containsExtensionBang  = StringUtils.containsIgnoreCase(filePath, extension + "!");
                        final boolean containsExtensionSlash = StringUtils.containsIgnoreCase(filePath, extension + "/");
                        if (containsExtensionBang || containsExtensionSlash) {
                            final String[] atoms = StringUtils.splitByWholeSeparator(filePath, extension + (containsExtensionBang ? "!" : "/"));
                            filePath = atoms[0] + extension;
                            zipEntryName = atoms[1];
                            break;
                        }
                    }
                    entry = CatalogUtils.getEntryByURIOrId(cat, filePath);
                }

                if (entry == null && filePath.endsWith("/")) {
                    //if no exact matches, look for a folder
                    final CatEntryFilterI folderFilter = getFolderFilter(entryFilter, parentPath);

                    //If there are no matching entries, I'm not sure if this should throw a 404, or return an empty list.
                    if (filePath.endsWith("/")) {
                        table.insertRows(CatalogUtils.getEntryDetails(cat, parentPath, getBaseURI() + "/resources/" + catResource.getXnatAbstractresourceId() + "/files", catResource, false, folderFilter, proj, locator));
                    } else {
                        getResponse().setStatus(acceptNotFound ? Status.SUCCESS_NO_CONTENT : Status.CLIENT_ERROR_NOT_FOUND, "Unable to find catalog entry for given uri.");
                        return new StringRepresentation("");
                    }
                } else if (entry == null) {
                    getResponse().setStatus(acceptNotFound ? Status.SUCCESS_NO_CONTENT : Status.CLIENT_ERROR_NOT_FOUND, "Unable to find catalog entry for given uri.");
                    return new StringRepresentation("");
                } else {
                    file = CatalogUtils.getFile(entry, parentPath, projectId);

                    if (file == null || !file.exists()) { // If file does not exist
                        getResponse().setStatus(acceptNotFound ? Status.SUCCESS_NO_CONTENT : Status.CLIENT_ERROR_NOT_FOUND, "Unable to find file.");
                        return new StringRepresentation("");
                    }

                    if (mediaType.equals(MediaType.IMAGE_JPEG) && Dcm2Jpg.isDicom(file)) {
                        try {
                            return new InputRepresentation(new ByteArrayInputStream(Dcm2Jpg.convert(file)), mediaType);
                        } catch (IOException e) {
                            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unable to convert this file to jpeg : " + e.getMessage());
                            return new StringRepresentation("");
                        }
                    }

                    final String filename = (zipEntryName == null ? file.getName() : zipEntryName).toLowerCase();

                    try {
                        // If the user is requesting a file within the zip archive
                        if (zipEntryName != null) {
                            // Get the zip entry requested
                            final ZipFile  zipFile  = new ZipFile(file);
                            final ZipEntry zipEntry = zipFile.getEntry(URLDecoder.decode(zipEntryName, "UTF-8"));
                            if (zipEntry == null) {
                                getResponse().setStatus(acceptNotFound ? Status.SUCCESS_NO_CONTENT : Status.CLIENT_ERROR_NOT_FOUND, "Unable to find file.");
                                return new StringRepresentation("");
                            } else { // Return the requested zip entry
                                return new InputRepresentation(zipFile.getInputStream(zipEntry), buildMediaType(mediaType, filename));
                            }
                            // If the user is requesting a list of the contents within the zip file
                        } else if (listContents && isFileZipArchive(filename)) {
                            // Create a new XFTTable with File Name and Size columns
                            final XFTTable fileTable = new XFTTable();
                            fileTable.initTable(new String[]{"File Name", "Size"});

                            // Get the contents of the zip file
                            try (final ZipFile zipFile = new ZipFile(file)) {
                                final Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

                                // Populate table rows and add the row to the table
                                while (zipEntries.hasMoreElements()) {
                                    final ZipEntry zipEntry = zipEntries.nextElement();
                                    fileTable.rows().add(new Object[]{zipEntry.getName(), zipEntry.getSize()});
                                }
                            }

                            // Set the table if we got any rows
                            if (!fileTable.rows().isEmpty()) {
                                table = fileTable;  // table gets passed into representTable() below
                            }
                        } else {
                            // Return the requested file
                            return getFileRepresentation(file, buildMediaType(mediaType, filename));
                        }
                    } catch (ZipException e) {
                        getResponse().setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, e.getMessage());
                        return new StringRepresentation("");
                    } catch (IOException e) {
                        getResponse().setStatus(acceptNotFound ? Status.SUCCESS_NO_CONTENT : Status.CLIENT_ERROR_NOT_FOUND, e.getMessage());
                        return new StringRepresentation("");
                    }

                }
            }
        } else {
            if (filePath == null || filePath.equals("")) {
                String baseURI = getBaseURI();
                if (entryFilter == null) {
                    ArrayList<File> files = resource.getCorrespondingFiles(rootArchivePath);
                    for (File subFile : files) {
                        Object[] row = new Object[13];
                        row[0] = (subFile.getName());
                        row[1] = (subFile.length());
                        if (locator.equalsIgnoreCase(URI)) {
                            row[2] = baseURI + "/resources/" + resource.getXnatAbstractresourceId() + "/files/" + subFile.getName();
                        } else if (locator.equalsIgnoreCase(ABSOLUTE_PATH)) {
                            row[2] = subFile.getAbsolutePath();
                        } else {
                            row[2] = subFile.getAbsolutePath().substring(rootArchivePath.substring(0, rootArchivePath.lastIndexOf(projectId)).length());
                        }
                        row[3] = resource.getLabel();
                        row[4] = resource.getTagString();
                        row[5] = resource.getFormat();
                        row[6] = resource.getContent();
                        row[7] = resource.getXnatAbstractresourceId();
                        table.insertRow(row);
                    }
                }
            } else {
                ArrayList<File> files = resource.getCorrespondingFiles(rootArchivePath);
                for (File subFile : files) {
                    if (subFile.getName().equals(filePath)) {
                        file = subFile;
                        break;
                    }
                }

                if (file != null && file.exists()) {
                    return getFileRepresentation(file, mediaType);
                } else {
                    getResponse().setStatus(acceptNotFound ? Status.SUCCESS_NO_CONTENT : Status.CLIENT_ERROR_NOT_FOUND, "Unable to find file.");
                    return new StringRepresentation("");
                }
            }
        }

        final Pair<Hashtable<String, Object>, Map<String, Map<String, String>>> parametersAndProperties = getDefaultParametersAndColumnProperties();
        parametersAndProperties.getRight().get(URI).put("serverRoot", getContextPath());
        return representTable(table, mediaType, parametersAndProperties.getLeft(), parametersAndProperties.getRight(), getSessionMaps());
    }

    private CatEntryBean getCatalogEntry(final CatCatalogBean catalog) {
        final CatEntryBean firstTry = (CatEntryBean) CatalogUtils.getEntryByURI(catalog, filePath);
        if (firstTry != null) {
            return firstTry;
        }
        return (CatEntryBean) CatalogUtils.getEntryById(catalog, filePath);
    }

    @Nonnull
    private Representation buildXcatRepresentation(final XFTTable table, final MediaType mediaType, final Map<String, String> session_mapping) {
        final String server          = StringUtils.removeEnd(TurbineUtils.GetFullServerPath(getHttpServletRequest()), "/");
        final int    uriIndex        = table.getColumnIndex(URI);
        final int    sizeIndex       = table.getColumnIndex("Size");
        final int    collectionIndex = table.getColumnIndex("collection");
        final int    cat_IDIndex     = table.getColumnIndex("cat_ID");

        final CatCatalogBean      cat             = new CatCatalogBean();
        final Map<String, String> valuesToReplace = getReMaps();

        for (final Object[] row : table.rows()) {
            final String uri      = (String) row[uriIndex];
            final String initial  = RestFileUtils.replaceResourceLabel(RestFileUtils.getRelativePath(uri, session_mapping).replace('\\', '/'), row[cat_IDIndex], (String) row[collectionIndex]);
            final String relative = StringUtils.replaceEachRepeatedly(initial, valuesToReplace.keySet().toArray(new String[0]), valuesToReplace.values().toArray(new String[0]));

            final CatEntryBean entry = new CatEntryBean();
            entry.setUri(server + uri);
            CatalogUtils.setCatEntryBeanMetafields(entry, relative, row[sizeIndex].toString());
            cat.addEntries_entry(entry);
        }

        setContentDisposition("files.xcat", false);

        return new BeanRepresentation(cat, mediaType, false);
    }

    @Nullable
    private Representation buildZipRepresentation(final XFTTable table, final MediaType mediaType, final Map<String, String> session_mapping) {
        final ZipRepresentation zipRepresentation;
        try {
            zipRepresentation = new ZipRepresentation(mediaType, getSessionIds(), identifyCompression(null));
        } catch (ActionException e) {
            log.error("", e);
            setResponseStatus(e);
            return null;
        }

        final int uriIndex        = table.getColumnIndex(URI);
        final int fileIndex       = table.getColumnIndex("file");
        final int collectionIndex = table.getColumnIndex("collection");
        final int cat_IDIndex     = table.getColumnIndex("cat_ID");

        //Refactored on 3/24 to allow the returning of the old file structure. This was to support Mohana's legacy pipelines.
        String structure = StringUtils.defaultIfBlank(getQueryVariable("structure"), "default");

        final Map<String, String> valuesToReplace = StringUtils.equalsAnyIgnoreCase(structure, "legacy", "simplified") ? new HashMap<String, String>() : getReMaps();

        //TODO: This should all be rewritten.  The implementation of the path relativization should be injectable, particularly to support other possible structures.
        for (final Object[] row : table.rows()) {
            final String uri   = (String) row[uriIndex];
            final File   child = (File) row[fileIndex];

            if (child != null && child.exists()) {
                final String pathForZip;
                if (structure.equalsIgnoreCase("improved")) {
                    pathForZip = getImprovedPath(uri, row[cat_IDIndex], mediaType);
                } else if (structure.equalsIgnoreCase("legacy")) {
                    pathForZip = child.getAbsolutePath();
                } else {
                    pathForZip = uri;
                }

                final String relative;
                switch (structure) {
                    case "improved":
                        relative = pathForZip;
                        break;
                    case "simplified":
                        relative = RestFileUtils.buildRelativePath(pathForZip, session_mapping, valuesToReplace, row[cat_IDIndex], (String) row[collectionIndex]).replace("/resources", "").replace("/files", "");
                        break;
                    default:
                        relative = RestFileUtils.buildRelativePath(pathForZip, session_mapping, valuesToReplace, row[cat_IDIndex], (String) row[collectionIndex]);
                }

                zipRepresentation.addEntry(relative, child);
            }
        }

        if (zipRepresentation.getEntryCount() == 0) {
            getResponse().setStatus(acceptNotFound ? Status.SUCCESS_NO_CONTENT : Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }

        return zipRepresentation;
    }

    private String getImprovedPath(String fileUri, Object catNumber, MediaType mt) {
        String         root = "";
        List<Object[]> rows = getCatalogs().rows();
        for (Object[] row : rows) {         // iterate through the rows of the catalog to find
            if (row[0].equals(catNumber)) { // the catalog entry matching the current object
                root = row[3].toString() + "/"; // resource type, e.g. scans, resources, assessors
                if (row[4] != null && !row[4].equals("")) { // folder name, usually scan number_scan type
                    root += row[4].toString();
                    // extend the folder name with scan type as long as it's not a tar (tar's have a 100 character limit)
                    if (!mt.equals(MediaType.APPLICATION_GNU_TAR) && !mt.equals(MediaType.APPLICATION_TAR) &&
                            row[5] != null && !row[5].equals("")) {
                        // session types can have special characters that interfere with file-path creation, so those should be replaced with underscores
                        root += "_" + row[5].toString().replaceAll("[\\/\\\\:\\*\\?\"<>\\|]", "_");
                    }
                    root += "/";
                }
                if (row[1] != null && !row[1].equals("")) {
                    root += row[1].toString() + "/"; // data subfolder, most commonly DICOM
                } else {
                    root += row[0].toString() + "/"; // if no subfolder name, use resource id
                }
            }
        }
        int filesStart = fileUri.lastIndexOf("/files/");
        return root + fileUri.substring(filesStart + 7);
    }

    private List<FileWriterWrapperI> getReferenceWrapper(String value) throws FileUploadException {
        File file = new File(value);
        if (!file.exists()) {
            throw new FileUploadException("The resource referenced does not exist: " + value);
        }
        List<FileWriterWrapperI> files = new ArrayList<>();
        if (file.isFile()) {
            files.add(new StoredFile(file, true, "", true));
        } else {
            // TODO: This is a simple recursive find of all files underneath the specified root. It'd be nice to support manifest files containing ant path specifiers or something similar to that.
            Collection found = org.apache.commons.io.FileUtils.listFiles(file, FileFileFilter.FILE, DirectoryFileFilter.DIRECTORY);
            for (Object foundObject : found) {
                if (!(foundObject instanceof File)) {

                    throw new RuntimeException("Something went really wrong");
                }
                File foundFile = (File) foundObject;
                if (foundFile.isFile()) {
                    files.add(new StoredFile(foundFile, true, file.toURI().relativize(foundFile.getParentFile().toURI()).getPath(), true));
                }
            }
        }
        return files;
    }

    private CatEntryFilterI getFolderFilter(final CatEntryFilterI entryFilter, final String parentPath) {
        final boolean recursive = !(isQueryVariableFalse("recursive"));
        final String  dir       = filePath;
        return new CatEntryFilterI() {
            @Override
            public boolean accept(final CatEntryI entry) {
                String relPath = CatalogUtils.getRelativePathForCatalogEntry(entry, parentPath);
                return relPath.startsWith(dir) && (recursive || StringUtils.contains(relPath.substring(dir.length() + 1), "/")) && (entryFilter == null || entryFilter.accept(entry));
            }
        };
    }

    private CatEntryFilterI buildFilter() {
        final String[] contents    = getQueryVariables("file_content");
        final String[] formats     = getQueryVariables("file_format");
        final boolean  hasContents = !ArrayUtils.isEmpty(contents);
        final boolean  hasFormats  = !ArrayUtils.isEmpty(formats);
        if (!hasContents && !hasFormats) {
            return null;
        }
        return new CatEntryFilterI() {
            public boolean accept(final CatEntryI entry) {
                if (hasFormats && ((entry.getFormat() == null && !ArrayUtils.contains(formats, "NULL")) || !ArrayUtils.contains(formats, entry.getFormat()))) {
                    return false;
                }
                if (hasContents) {
                    return entry.getContent() == null ? ArrayUtils.contains(contents, "NULL") : ArrayUtils.contains(contents, entry.getContent());
                }
                return true;
            }
        };
    }

    /**
     * Function determines if the given file is a zip archive by
     * checking whether the fileName contains a zip extension
     *
     * @param f - the file name
     *
     * @return - true / false is the file a zip file?
     */
    private boolean isFileZipArchive(String f) {
        for (String s : XDAT.getSiteConfigPreferences().getZipExtensionsAsArray()) {
            if (f.contains(s)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, String> getReMaps() {
        return RestFileUtils.getReMaps(scans, recons);
    }

    private Map<String, String> getSessionMaps() {
        final Map<String, String> sessionIds = new Hashtable<>();
        // Check if the session is an assessor to an "assessed" session
        if (!assesseds.isEmpty()) {
            // Check if the session containing the assessor has an "ASSESSORS" directory.
            // This signifies that the directory structure is based on a "modern" version of XNAT.
            if (!expts.isEmpty() && new File(assesseds.get(0).getSessionDir(), "ASSESSORS").isDirectory()) {
                for (final XnatExperimentdata session : expts) {
                    sessionIds.put(session.getId(), session.getArchiveDirectoryName());
                }
                return sessionIds;
            }
            //IOWA customization: to include project and subject in path
            final boolean projectIncludedInPath = isQueryVariableTrue("projectIncludedInPath");
            final boolean subjectIncludedInPath = isQueryVariableTrue("subjectIncludedInPath");
            for (final XnatExperimentdata session : assesseds) {
                final StringBuilder sessionUri = new StringBuilder();
                if (projectIncludedInPath) {
                    sessionUri.append(session.getProject()).append("/");
                }
                if (subjectIncludedInPath) {
                    if (session instanceof XnatImagesessiondata) {
                        final XnatSubjectdata subject = XnatSubjectdata.getXnatSubjectdatasById(((XnatImagesessiondata) session).getSubjectId(), getUser(), false);
                        sessionUri.append(subject.getLabel()).append("/");
                    }
                }
                sessionUri.append(session.getArchiveDirectoryName());
                sessionIds.put(session.getId(), sessionUri.toString());
            }
            return sessionIds;
        }
        if (!expts.isEmpty()) {
            for (final XnatExperimentdata session : expts) {
                sessionIds.put(session.getId(), session.getArchiveDirectoryName());
            }
            return sessionIds;
        }
        if (sub != null) {
            sessionIds.put(sub.getId(), sub.getArchiveDirectoryName());
            return sessionIds;
        }
        if (proj != null) {
            sessionIds.put(proj.getId(), proj.getId());
            return sessionIds;
        }

        return sessionIds;
    }

    private List<String> getSessionIds() {
        final List<XnatExperimentdata> experiments = !assesseds.isEmpty() ? assesseds : !expts.isEmpty() ? expts : null;
        if (experiments != null) {
            return Lists.transform(experiments, new Function<XnatExperimentdata, String>() {
                @Override
                public String apply(final XnatExperimentdata experiment) {
                    return experiment.getArchiveDirectoryName();
                }
            });
        }
        if (sub != null) {
            return Collections.singletonList(sub.getArchiveDirectoryName());
        }
        if (proj != null) {
            return Collections.singletonList(proj.getId());
        }
        return Collections.emptyList();
    }

    private FileRepresentation getFileRepresentation(File f, MediaType mt) {
        return setFileRepresentation(f, mt);
    }

    private FileRepresentation setFileRepresentation(File f, MediaType mt) {
        setResponseHeader("Cache-Control", "must-revalidate");
        return representFile(f, mt);
    }

    @Nullable
    private XnatImageassessordata getAssessor(final @Nonnull XnatResourcecatalog resource) {
        try {
            final Matcher assessorUriMatcher = PATTERN_ASSESSOR_URI.matcher(resource.getUri());
            if (assessorUriMatcher.find()) {
                final String assessorId = assessorUriMatcher.group(1);
                if (StringUtils.isNotBlank(assessorId)) {
                    final XnatImageassessordata assessor = (XnatImageassessordata) XnatExperimentdata.getXnatExperimentdatasById(assessorId, Users.getAdminUser(), false);
                    if (assessor != null) {
                        return assessor;
                    }
                    final Matcher archiveUriMatcher = PATTERN_ARCHIVE_URI.matcher(resource.getUri());
                    if (archiveUriMatcher.find()) {
                        return (XnatImageassessordata) XnatExperimentdata.GetExptByProjectIdentifier(archiveUriMatcher.group(1), assessorId, Users.getAdminUser(), false);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error getting assessor object to check permissions.", e);
        }
        return null;
    }

    private Pair<Hashtable<String, Object>, Map<String, Map<String, String>>> getDefaultParametersAndColumnProperties() {
        final Hashtable<String, Object> parameters = new Hashtable<>();
        parameters.put("title", "Files");
        final Map<String, Map<String, String>> columnProperties = new Hashtable<>();
        columnProperties.put(URI, new Hashtable<String, String>());
        return ImmutablePair.of(parameters, columnProperties);
    }

    private static final List<Variant> VARIANTS             = Arrays.asList(new Variant(MediaType.APPLICATION_JSON), new Variant(MediaType.TEXT_HTML), new Variant(MediaType.TEXT_XML), new Variant(MediaType.IMAGE_JPEG));
    private static final Pattern       PATTERN_ASSESSOR_URI = Pattern.compile("/assessors/([^/]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern       PATTERN_ARCHIVE_URI  = Pattern.compile("/archive/([^/]+)");

    private final boolean              listContents = isQueryVariableTrueHelper(getQueryVariable("listContents"));

    private       String               filePath     = null;
    private       XnatAbstractresource resource     = null;
    private       String               reference;
    private final boolean              acceptNotFound;
    private       boolean              delete;
    private       boolean              async;
    private       String[]             notifyList;
}
