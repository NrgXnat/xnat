/*
 * web: org.nrg.xnat.restlet.resources.files.FileList
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources.files;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.dcm.Dcm2Jpg;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.bean.CatEntryBean;
import org.nrg.xdat.bean.CatEntryMetafieldBean;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.services.messaging.file.MoveStoredFileRequest;
import org.nrg.xnat.helpers.file.StoredFile;
import org.nrg.xnat.helpers.resource.direct.ResourceModifierA.UpdateMeta;
import org.nrg.xnat.restlet.files.utils.RestFileUtils;
import org.nrg.xnat.restlet.representations.BeanRepresentation;
import org.nrg.xnat.restlet.representations.JSONObjectRepresentation;
import org.nrg.xnat.restlet.representations.ZipRepresentation;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.turbine.utils.ArchivableItem;
import org.nrg.xnat.utils.CatalogUtils;
import org.nrg.xnat.utils.CatalogUtils.CatEntryFilterI;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * @author timo
 */
public class FileList extends XNATCatalogTemplate {
    private static final Logger logger = LoggerFactory.getLogger(FileList.class);
    private String filepath = null;
    private String reference = null;
    private boolean delete;
    private boolean async;
    private String[] notifyList = new String[0];
    private XnatAbstractresource resource = null;
    private final boolean listContents = isQueryVariableTrueHelper(this.getQueryVariable("listContents"));

    public FileList(Context context, Request request, Response response) {
        super(context, request, response, isQueryVariableTrue("all", request));
        reference = getQueryVariable("reference");
        delete = isQueryVariableTrue("delete", request);
        async = isQueryVariableTrue("async", request);
        notifyList = isQueryVariableTrue("notify", request) ? getQueryVariable("notify").split(",") : new String[0];
        try {
            final UserI user = getUser();
            if (resource_ids != null) {
                List<Integer> alreadyAdded = new ArrayList<>();
                if (catalogs != null && catalogs.size() > 0) {
                    for (Object[] row : catalogs.rows()) {
                        Integer id = (Integer) row[0];
                        String label = (String) row[1];

                        for (String resourceID : resource_ids) {
                            if (!alreadyAdded.contains(id) && (id.toString().equals(resourceID) || (label != null && label.equals(resourceID)))) {
                                XnatAbstractresource res = XnatAbstractresource.getXnatAbstractresourcesByXnatAbstractresourceId(id, user, false);
                                if (row.length == 7) res.setBaseURI((String) row[6]);
                                resources.add(res);
                                alreadyAdded.add(id);
                            }
                        }
                    }
                }
                // if caller is asking for the files directly by resource ID (e.g. /experiments/{EXPT_ID}/resources/{RESOURCE_ID}/files),
                // the catalog will not be found by the superclass
                // (unless caller passes all=true, which seems clunky to require given that they are passing in the resource PK).
                // So here we provide an alternate path finding the resource
                // added check to make sure it's an number.  You can also reference resource labels here (not just pks).
                for (String resourceID : resource_ids) {
                    try {
                        Integer id = Integer.parseInt(resourceID);
                        XnatAbstractresource res = XnatAbstractresource.getXnatAbstractresourcesByXnatAbstractresourceId(id, user, false);
                        if (res != null && !alreadyAdded.contains(id)) {
                            resources.add(res);
                        }
                    } catch (NumberFormatException e) {
                        // ignore... this is probably a resource label
                    }
                }
            }

            if (resources.size() > 0) {
                resource = resources.get(0);
            }

            filepath = getRequest().getResourceRef().getRemainingPart();
            if (filepath != null && filepath.contains("?")) {
                filepath = filepath.substring(0, filepath.indexOf("?"));
            }

            if (filepath != null && filepath.startsWith("/")) {
                filepath = filepath.substring(1);
            }

            getVariants().add(new Variant(MediaType.APPLICATION_JSON));
            getVariants().add(new Variant(MediaType.TEXT_HTML));
            getVariants().add(new Variant(MediaType.TEXT_XML));
            getVariants().add(new Variant(MediaType.IMAGE_JPEG));
        } catch (Exception e) {
            logger.error("Error occurred while initializing FileList service", e);
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
     * if(filepath>"")then returns File
     * else returns table of files
     */
    @Override
    @SuppressWarnings("unchecked")
    public Representation represent(Variant variant) {
        MediaType mt = overrideVariant(variant);
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

            if (resources.size() == 1 && !(isZIPRequest(mt))) {
                //one catalog
                return handleSingleCatalog(mt);
            } else if (resources.size() > 0) {
                //multiple catalogs
                return handleMultipleCatalogs(mt);
            } else {
                //all catalogs
                catalogs.resetRowCursor();
                for (Hashtable<String, Object> rowHash : catalogs.rowHashs()) {
                    Object o = rowHash.get("xnat_abstractresource_id");
                    XnatAbstractresource res = XnatAbstractresource.getXnatAbstractresourcesByXnatAbstractresourceId(o, getUser(), false);
                    if (rowHash.containsKey("resource_path")) res.setBaseURI((String) rowHash.get("resource_path"));
                    resources.add(res);
                }

                return handleMultipleCatalogs(mt);
            }
        } catch (ElementNotFoundException e) {
            logger.error("", e);
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unable to find file.");
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
                if (Permissions.canEdit(user,security)) {
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
                        if (catalogs.rows().size() > 0) {
                            resourceIdentifier = catalogs.getFirstObject();
                        } else {
                            if (resource_ids != null && resource_ids.size() > 0) {
                                resourceIdentifier = resource_ids.get(0);
                            } else {
                                resourceIdentifier = null;
                            }
                        }
                    } else {
                        resourceIdentifier = resource.getXnatAbstractresourceId();
                    }

                    final boolean overwrite = isQueryVariableTrue("overwrite");
                    final boolean extract = isQueryVariableTrue("extract");

                    PersistentWorkflowI wrk = PersistentWorkflowUtils.getWorkflowByEventId(user, getEventId());
                    if (wrk == null && resource != null && "SNAPSHOTS".equals(resource.getLabel())) {
                        if (getSecurityItem() instanceof XnatExperimentdata) {
                            Collection<? extends PersistentWorkflowI> workflows = PersistentWorkflowUtils.getOpenWorkflows(user, ((ArchivableItem) security).getId());
                            if (workflows != null && workflows.size() == 1) {
                                wrk = (WrkWorkflowdata) CollectionUtils.get(workflows, 0);
                                if (!"xnat_tools/AutoRun.xml".equals(wrk.getPipelineName())) {
                                    wrk = null;
                                }
                            }
                        }
                    }

                    boolean skipUpdateStats = isQueryVariableFalse("update-stats");

                    boolean isNew = false;
                    if (wrk == null && !skipUpdateStats) {
                        isNew = true;
                        wrk = PersistentWorkflowUtils.buildOpenWorkflow(user, getSecurityItem().getItem(), newEventInstance(EventUtils.CATEGORY.DATA, (getAction() != null) ? getAction() : EventUtils.UPLOAD_FILE));
                    }

                    final EventMetaI i;
                    if (wrk == null) {
                        i = EventUtils.DEFAULT_EVENT(user, null);
                    } else {
                        i = wrk.buildEvent();
                    }

                    UpdateMeta um = new UpdateMeta(i, !(skipUpdateStats));

                    try {
                        final List<FileWriterWrapperI> writers = getFileWriters();

                        if (writers != null) {
                            if (writers.size() > 0) {
                                if (StringUtils.isEmpty(reference) || !async) {
                                    List<String> duplicates = buildResourceModifier(overwrite, um).addFile(writers, resourceIdentifier, type, filepath, buildResourceInfo(um), extract);
                                    if (!overwrite && duplicates.size() > 0) {
                                        getResponse().setStatus(Status.SUCCESS_OK);
                                        JSONObject json = new JSONObject();
                                        json.put("duplicates", duplicates);
                                        getResponse().setEntity(new JSONObjectRepresentation(MediaType.TEXT_HTML, json));
                                    }else{
                                        getResponse().setStatus(Status.SUCCESS_OK);
                                        getResponse().setEntity(new StringRepresentation("", MediaType.TEXT_PLAIN));
                                    }
                                } else {
                                    assert wrk != null;
                                    wrk.setStatus(PersistentWorkflowUtils.QUEUED);
                                    WorkflowUtils.save(wrk, wrk.buildEvent());
                                    MoveStoredFileRequest request = new MoveStoredFileRequest(buildResourceModifier(overwrite, um), resourceIdentifier, writers, user, wrk.getWorkflowId(), delete, notifyList, type, filepath, buildResourceInfo(um), extract);
                                    XDAT.sendJmsRequest(request);

                                    getResponse().setStatus(Status.SUCCESS_OK);
                                    JSONObject json = new JSONObject();
                                    json.put("workflowId", wrk.getWorkflowId());
                                    getResponse().setEntity(new JSONObjectRepresentation(MediaType.TEXT_HTML, json));
                                }
                            } else {
                                final String method = getRequest().getMethod().toString();
                                final long size = getRequest().getEntity().getAvailableSize();
                                if (size == 0) {
                                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "You tried to " + method + " to this service, but didn't provide any data (found request entity size of 0). Please check the format of your service request.");
                                } else {
                                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "You tried to " + method + " a payload of " + CatalogUtils.formatSize(size) + " to this service, but didn't provide any data. If you think you sent data to upload, you can try to " + method + " with the query-string parameter inbody=true or use multipart/form-data encoding.");
                                }
                            }
                        }
                    } catch (Exception e) {
                            logger.error("Error occurred while trying to POST file", e);
                            throw e;
                        }

                    if (StringUtils.isEmpty(reference) && wrk != null && isNew) {
                        WorkflowUtils.complete(wrk, i);
                    }
                }
            } catch(IllegalArgumentException e){ // XNAT-2989
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
                logger.error("", e);
            } catch (Exception e) {
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
                logger.error("", e);
            }
        }
    }

    @Override
    public void handleDelete() {
        if (resource != null && parent != null && security != null) {
            try {
                final UserI user = getUser();
                if (Permissions.canDelete(user,security)) {
                    if (!((security).getItem().isActive() || (security).getItem().isQuarantine())) {
                        //cannot modify it if it isn't active
                        throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN, new Exception());
                    }

                    if (proj == null) {
                        if (parent.getItem().instanceOf("xnat:experimentData")) {
                            proj = ((XnatExperimentdata) parent).getPrimaryProject(false);
                        } else if (security.getItem().instanceOf("xnat:experimentData")) {
                            proj = ((XnatExperimentdata) security).getPrimaryProject(false);
                        }
                    }

                    if (resource instanceof XnatResourcecatalog) {
                        Collection<CatEntryI> entries = new ArrayList<>();

                        final XnatResourcecatalog catResource = (XnatResourcecatalog) resource;

                        final File catFile = catResource.getCatalogFile(proj.getRootArchivePath());
                        final String parentPath = catFile.getParent();
                        final CatCatalogBean cat = catResource.getCleanCatalog(proj.getRootArchivePath(), false, null, null);

                        CatEntryBean e = (CatEntryBean) CatalogUtils.getEntryByURI(cat, filepath);
                        if (e != null) {
                            entries.add(e);
                        }
                        if (entries.size() == 0) {
                            e = (CatEntryBean) CatalogUtils.getEntryById(cat, filepath);
                            if (e != null) {
                                entries.add(e);
                            }
                        }

                        if (entries.size() == 0 && filepath.endsWith("/")) {
                            final CatalogUtils.CatEntryFilterI folderFilter=new CatalogUtils.CatEntryFilterI() {
            					@Override
            					public boolean accept(CatEntryI entry) {
            						return entry.getUri().startsWith(filepath);
            					}
            				};
            				
                            entries.addAll(CatalogUtils.getEntriesByFilter(cat, folderFilter));
                        }

                        if (entries.size() == 0 && filepath.endsWith("*")) {
                            StringBuilder regex = new StringBuilder(filepath);
                            int lastIndex = filepath.lastIndexOf("*");
                            regex.replace(lastIndex, lastIndex + 1, ".*");
                            entries.addAll(CatalogUtils.getEntriesByRegex(cat, regex.toString()));
                        }

                        for (CatEntryI entry : entries) {

                            final File f = new File(parentPath, entry.getUri());

                            if (f.exists()) {
                                PersistentWorkflowI work = WorkflowUtils.getOrCreateWorkflowData(getEventId(), user, security.getItem(), newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.REMOVE_FILE));
                                EventMetaI ci = work.buildEvent();

                                CatalogUtils.removeEntry(cat, entry);
                                //update for file deletion
                                CatalogUtils.writeCatalogToFile(cat, catFile);

                                CatalogUtils.moveToHistory(catFile, f, (CatEntryBean) entry, ci);

                                if (!isQueryVariableFalse("removeFiles") && !f.delete()) {
                                    logger.warn("Error attempting to delete physical file for deleted resource: " + f.getAbsolutePath());
                                }

                                //if parent folder is empty, then delete folder
                                if (FileUtils.CountFiles(f.getParentFile(), true) == 0) {
                                    FileUtils.DeleteFile(f.getParentFile());
                                }

                                CatalogUtils.populateStats(catResource, proj.getRootArchivePath());
                                SaveItemHelper.authorizedSave(catResource, user, false, false, ci);

                                WorkflowUtils.complete(work, ci);
                            } else {
                                getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "File missing");
                            }
                        }
                    } else {
                        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "File is not an instance of XnatResourcecatalog. Delete operation not supported.");
                    }
                } else {
                    getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "User account doesn't have permission to modify this session.");
                }
            } catch (Exception e) {
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
            }
        }
    }

    public Representation representTable(XFTTable table, MediaType mt, Hashtable<String, Object> params, Map<String, Map<String, String>> cp, Map<String, String> session_mapping) {
        if (mt.equals(SecureResource.APPLICATION_XCAT)) {
            //"Name","Size","URI","collection","file_tags","file_format","file_content","cat_ID"
            CatCatalogBean cat = new CatCatalogBean();

            String server = TurbineUtils.GetFullServerPath(getHttpServletRequest());
            if (server.endsWith("/")) {
                server = server.substring(0, server.length() - 1);
            }

            final int uriIndex = table.getColumnIndex("URI");
            final int sizeIndex = table.getColumnIndex("Size");

            final int collectionIndex = table.getColumnIndex("collection");
            final int cat_IDIndex = table.getColumnIndex("cat_ID");

            Map<String, String> valuesToReplace = getReMaps();

            for (Object[] row : table.rows()) {

                CatEntryBean entry = new CatEntryBean();

                String uri = (String) row[uriIndex];
                String relative = RestFileUtils.getRelativePath(uri, session_mapping);

                entry.setUri(server + uri);

                relative = relative.replace('\\', '/');

                relative = RestFileUtils.replaceResourceLabel(relative, row[cat_IDIndex], (String) row[collectionIndex]);

                for (Map.Entry<String, String> e : valuesToReplace.entrySet()) {
                    relative = RestFileUtils.replaceInPath(relative, e.getKey(), e.getValue());
                }

                entry.setCachepath(relative);

                CatEntryMetafieldBean meta = new CatEntryMetafieldBean();
                meta.setMetafield(relative);
                meta.setName("RELATIVE_PATH");
                entry.addMetafields_metafield(meta);

                meta = new CatEntryMetafieldBean();
                meta.setMetafield(row[sizeIndex].toString());
                meta.setName("SIZE");
                entry.addMetafields_metafield(meta);

                cat.addEntries_entry(entry);
            }

            setContentDisposition("files.xcat", false);

            return new BeanRepresentation(cat, mt, false);
        } else if (isZIPRequest(mt)) {
            ZipRepresentation rep;
            try {
                rep = new ZipRepresentation(mt, getSessionIds(), identifyCompression(null));
            } catch (ActionException e) {
                logger.error("", e);
                setResponseStatus(e);
                return null;
            }

            final int uriIndex = table.getColumnIndex("URI");
            final int fileIndex = table.getColumnIndex("file");

            final int collectionIndex = table.getColumnIndex("collection");
            final int cat_IDIndex = table.getColumnIndex("cat_ID");

            //Refactored on 3/24 to allow the returning of the old file structure.  This was to support Mohana's legacy pipelines.
            String structure = getQueryVariable("structure");
            if (StringUtils.isEmpty(structure)) {
                structure = "default";
            }

            final Map<String, String> valuesToReplace;
            if (structure.equalsIgnoreCase("legacy") || structure.equalsIgnoreCase("simplified")) {
                valuesToReplace = new Hashtable<>();
            } else {
                valuesToReplace = getReMaps();
            }

            //TODO: This should all be rewritten.  The implementation of the path relativization should be injectable, particularly to support other possible structures.
            for (final Object[] row : table.rows()) {
                final String uri = (String) row[uriIndex];
                final File child = (File) row[fileIndex];

                if (child != null && child.exists()) {
                    final String pathForZip;
                    if (structure.equalsIgnoreCase("improved")) {
                        pathForZip = getImprovedPath(uri, row[cat_IDIndex], mt);
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

                    rep.addEntry(relative, child);
                }
            }

            if (rep.getEntryCount() == 0) {
                getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return null;
            }

            return rep;
        } else {
            return super.representTable(table, mt, params, cp);
        }
    }

    private String getImprovedPath(String fileUri, Object catNumber, MediaType mt) {
        String root = "";
        List<Object[]> rows = catalogs.rows();
        for (Object[] row : rows) {         // iterate through the rows of the catalog to find
            if (row[0].equals(catNumber)) { // the catalog entry matching the current object
                root = row[3].toString() + "/"; // resource type, e.g. scans, resources, assessors
                if (row[4] != null && !row[4].equals("")) { // folder name, usually scan number_scan type
                    root += row[4].toString();
                    // extend the folder name with scan type as long as it's not a tar (tar's have a 100 character limit)
                    if (!mt.equals(MediaType.APPLICATION_GNU_TAR) && !mt.equals(MediaType.APPLICATION_TAR) &&
                            row[5] != null && !row[5].equals("")) {
                        // session types can have special characters that interfere with filepath creation, so those should be replaced with underscores
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

    public CatEntryFilterI buildFilter() {
        final String[] file_content = getQueryVariables("file_content");
        final String[] file_format = getQueryVariables("file_format");
        if ((file_content != null && file_content.length > 0) || (file_format != null && file_format.length > 0)) {
            return new CatEntryFilterI() {
                public boolean accept(CatEntryI entry) {
                    if (file_format != null && file_format.length > 0) {
                        if (entry.getFormat() == null) {
                            if (!ArrayUtils.contains(file_format, "NULL")) return false;
                        } else {
                            if (!ArrayUtils.contains(file_format, entry.getFormat())) return false;
                        }
                    }

                    if (file_content != null && file_content.length > 0) {
                        if (entry.getContent() == null) {
                            if (!ArrayUtils.contains(file_content, "NULL")) return false;
                        } else {
                            if (!ArrayUtils.contains(file_content, entry.getContent())) return false;
                        }
                    }

                    return true;
                }
            };
        }

        return null;
    }

    public List<FileWriterWrapperI> getFileWritersAndLoadParams(final Representation entity, boolean useFileFieldName) throws FileUploadException, ClientException {
        if (StringUtils.isNotEmpty(reference)) {
            return getReferenceWrapper(reference);
        } else {
            return super.getFileWritersAndLoadParams(entity, useFileFieldName);
        }
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

    protected Representation handleMultipleCatalogs(MediaType mt) throws ElementNotFoundException {
        final boolean isZip = isZIPRequest(mt);

        File f = null;
        Map<String, File> fileList = new HashMap<>();

        final XFTTable table = new XFTTable();

        String[] headers;
        if (isZip)
            headers = CatalogUtils.FILE_HEADERS_W_FILE.clone();
        else
            headers = CatalogUtils.FILE_HEADERS.clone();

        String locator = "URI";
        // NOTE:  zip representations must have URI
        if (!isZip && getQueryVariable("locator") != null) {
            if (getQueryVariable("locator").equalsIgnoreCase("absolutePath")) {
                locator = "absolutePath";
                headers[ArrayUtils.indexOf(headers, "URI")] = locator;
            } else if (getQueryVariable("locator").equalsIgnoreCase("projectPath")) {
                locator = "projectPath";
                headers[ArrayUtils.indexOf(headers, "URI")] = locator;
            }
        }
        table.initTable(headers);

        final String baseURI = getBaseURI();

        final CatEntryFilterI entryFilter = buildFilter();

        final Integer index = (containsQueryVariable("index")) ? Integer.parseInt(getQueryVariable("index")) : null;

        for (final XnatAbstractresource temp : resources) {
            final String rootArchivePath = proj.getRootArchivePath();
            if (temp.getItem().instanceOf("xnat:resourceCatalog")) {
                final boolean includeRoot = isQueryVariableTrue("includeRootPath");

                final XnatResourcecatalog catResource = (XnatResourcecatalog) temp;


                final CatCatalogBean cat = catResource.getCleanCatalog(rootArchivePath, includeRoot, null, null);
                final String parentPath = catResource.getCatalogFile(rootArchivePath).getParent();

                if (cat != null) {
                    if (filepath == null || filepath.equals("")) {
                        table.rows().addAll(CatalogUtils.getEntryDetails(cat, parentPath, (catResource.getBaseURI() != null) ? catResource.getBaseURI() + "/files" : baseURI + "/resources/" + catResource.getXnatAbstractresourceId() + "/files", catResource, isZip || (index != null), entryFilter, proj, locator));
                    } else {
                        ArrayList<CatEntryI> entries = new ArrayList<>();

                        CatEntryBean e = (CatEntryBean) CatalogUtils.getEntryByURI(cat, filepath);
                        if (e != null) {
                            entries.add(e);
                        }
                        if (entries.size() == 0) {
                            e = (CatEntryBean) CatalogUtils.getEntryById(cat, filepath);
                            if (e != null) {
                                entries.add(e);
                            }
                        }
                        if (entries.size() == 0 && filepath.endsWith("/")) {
                        	//recursion is on by default
                        	final boolean recursive=!(this.isQueryVariableFalse("recursive"));
                        	final String dir=filepath;
                            final CatalogUtils.CatEntryFilterI folderFilter=new CatalogUtils.CatEntryFilterI() {
            					@Override
            					public boolean accept(CatEntryI entry) {
            						if(entry.getUri().startsWith(dir)){ 
            							if(recursive || StringUtils.contains(entry.getUri().substring(dir.length()+1),"/"))
            							{
                							return (entryFilter == null || entryFilter.accept(entry));
            							}
            						}
        							return false;
            					}
            				};
                            entries.addAll(CatalogUtils.getEntriesByFilter(cat, folderFilter));
                        }
                        if (entries.size() == 0 && filepath.endsWith("*")) {
                            StringBuilder regex = new StringBuilder(filepath);
                            int lastIndex = filepath.lastIndexOf("*");
                            regex.replace(lastIndex, lastIndex + 1, ".*");
                            entries.addAll(CatalogUtils.getEntriesByRegex(cat, regex.toString()));
                        }


                        if (entries.size() == 1) {
                            if (FileUtils.IsAbsolutePath(entries.get(0).getUri())) {
                                f = new File(entries.get(0).getUri());
                            } else {
                                f = new File(parentPath, entries.get(0).getUri());
                            }

                            if (f.exists()) break;

                        } else {

                            for (CatEntryI entry : entries) {
                                if (FileUtils.IsAbsolutePath(entry.getUri())) {
                                    f = new File(entry.getUri());
                                } else {
                                    f = new File(parentPath, entry.getUri());
                                }

                                if (f.exists()) {
                                    fileList.put(entry.getUri(), f);
                                }

                            }
                            break;
                        }
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
                            if (locator.equalsIgnoreCase("URI")) {
                                row.add(temp.getBaseURI() != null ? temp.getBaseURI() + "/files/" + subFile.getName() : baseURI + "/resources/" + temp.getXnatAbstractresourceId() + "/files/" + subFile.getName());
                            } else if (locator.equalsIgnoreCase("absolutePath")) {
                                row.add(subFile.getAbsolutePath());
                            } else if (locator.equalsIgnoreCase("projectPath")) {
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
                            table.rows().add(row.toArray());
                        }
                    }
                }
            }
        }

        String downloadName;
        if (security != null) {
            downloadName = ((ArchivableItem) security).getArchiveDirectoryName();
        } else {
            downloadName = getSessionMaps().get(Integer.toString(0));
        }

        if (mt.equals(MediaType.APPLICATION_ZIP)) {
            setContentDisposition(downloadName + ".zip");
        } else if (mt.equals(MediaType.APPLICATION_GNU_TAR)) {
            setContentDisposition(downloadName + ".tar.gz");
        } else if (mt.equals(MediaType.APPLICATION_TAR)) {
            setContentDisposition(downloadName + ".tar");
        }

        if (StringUtils.isEmpty(filepath) && index == null) {
            Hashtable<String, Object> params = new Hashtable<>();
            params.put("title", "Files");

            Map<String, Map<String, String>> cp = new Hashtable<>();
            cp.put("URI", new Hashtable<String, String>());
            String rootPath = getRequest().getRootRef().getPath();
            if (rootPath.endsWith("/data")) {
                rootPath = rootPath.substring(0, rootPath.indexOf("/data"));
            }
            if (rootPath.endsWith("/REST")) {
                rootPath = rootPath.substring(0, rootPath.indexOf("/REST"));
            }
            cp.get("URI").put("serverRoot", rootPath);

            return representTable(table, mt, params, cp, getSessionMaps());
        } else {
            if (index != null && table.rows().size() > index) {
                f = (File) table.rows().get(index)[8];
            }

            if (f == null || !f.exists()) {
                getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unable to find file.");
                return null;
            }

            final String name = f.getName();

            //return file
            if (fileList.size() > 0) {
                if ((mt.equals(MediaType.APPLICATION_ZIP) && !name.toLowerCase().endsWith(".zip"))
                        || (mt.equals(MediaType.APPLICATION_GNU_TAR) && !name.toLowerCase().endsWith(".tar.gz"))
                        || (mt.equals(MediaType.APPLICATION_TAR) && !name.toLowerCase().endsWith(".tar"))) {
                    final ZipRepresentation rep;
                    try {
                        rep = new ZipRepresentation(mt, ((ArchivableItem) security).getArchiveDirectoryName(), identifyCompression(null));
                    } catch (ActionException e) {
                        logger.error("", e);
                        setResponseStatus(e);
                        return null;
                    }
                    for (String fn : fileList.keySet()) {
                        rep.addEntry(fn, fileList.get(fn));
                    }
                    return rep;
                }
            } else {
                if ((mt.equals(MediaType.APPLICATION_ZIP) && !name.toLowerCase().endsWith(".zip"))
                        || (mt.equals(MediaType.APPLICATION_GNU_TAR) && !name.toLowerCase().endsWith(".tar.gz"))
                        || (mt.equals(MediaType.APPLICATION_TAR) && !name.toLowerCase().endsWith(".tar"))) {
                    final ZipRepresentation rep;
                    try {
                        rep = new ZipRepresentation(mt, ((ArchivableItem) security).getArchiveDirectoryName(), identifyCompression(null));
                    } catch (ActionException e) {
                        logger.error("", e);
                        setResponseStatus(e);
                        return null;
                    }
                    rep.addEntry(name, f);
                    return rep;
                } else {
                    return getFileRepresentation(f, mt);
                }
            }
        }
        return null;
    }

    protected Representation handleSingleCatalog(MediaType mt) throws ElementNotFoundException {
        File f = null;
        XFTTable table = new XFTTable();

        String[] headers = CatalogUtils.FILE_HEADERS.clone();
        String locator = "URI";
        if (getQueryVariable("locator") != null) {
            if (getQueryVariable("locator").equalsIgnoreCase("absolutePath")) {
                locator = "absolutePath";
                headers[ArrayUtils.indexOf(headers, "URI")] = locator;
            } else if (getQueryVariable("locator").equalsIgnoreCase("projectPath")) {
                locator = "projectPath";
                headers[ArrayUtils.indexOf(headers, "URI")] = locator;
            }
        }
        table.initTable(headers);

        final CatalogUtils.CatEntryFilterI entryFilter = buildFilter();
        final Integer index = (containsQueryVariable("index")) ? Integer.parseInt(getQueryVariable("index")) : null;


        if (resource.getItem().instanceOf("xnat:resourceCatalog")) {
            boolean includeRoot = this.isQueryVariableTrue("includeRootPath");

            XnatResourcecatalog catResource = (XnatResourcecatalog) resource;
            CatCatalogBean cat = catResource.getCleanCatalog(proj.getRootArchivePath(), includeRoot, null, null);
            String parentPath = catResource.getCatalogFile(proj.getRootArchivePath()).getParent();

            if (StringUtils.isEmpty(filepath) && index == null) {
                String baseURI = getBaseURI();

                if (cat != null) {
                    table.rows().addAll(CatalogUtils.getEntryDetails(cat, parentPath, baseURI + "/resources/" + catResource.getXnatAbstractresourceId() + "/files", catResource, false, entryFilter, proj, locator));
                }
            } else {

                String zipEntry = null;

                CatEntryI entry;
                if (index != null) {
                    entry = CatalogUtils.getEntryByFilter(cat, new CatEntryFilterI() {
                        private int count = 0;
                        private CatEntryFilterI filter = entryFilter;

                        public boolean accept(CatEntryI entry) {
                            if (filter.accept(entry)) {
                                if (index.equals(count++)) {
                                    return true;
                                }
                            }

                            return false;
                        }

                    });
                } else {
                    String lowercase = filepath.toLowerCase();

                    for (String s : XDAT.getSiteConfigPreferences().getZipExtensionsAsArray()) {
                        s = "." + s;
                        if (lowercase.contains(s + "!") || lowercase.contains(s + "/")) {
                            zipEntry = filepath.substring(lowercase.indexOf(s) + s.length());
                            filepath = filepath.substring(0, lowercase.indexOf(s) + s.length());
                            if (zipEntry.startsWith("!") || zipEntry.startsWith("/")) {
                                zipEntry = zipEntry.substring(1);
                            }
                            break;
                        }
                    }
                    entry = CatalogUtils.getEntryByURI(cat, filepath);

                    if (entry == null) {
                        entry = CatalogUtils.getEntryById(cat, filepath);
                    }
                }

                if (entry == null && filepath.endsWith("/")) {
                	//if no exact matches, look for a folder
                	String baseURI = getBaseURI();

                	//recursion is on by default
                	final boolean recursive=!(this.isQueryVariableFalse("recursive"));
                	final String dir=filepath;
                    final CatalogUtils.CatEntryFilterI folderFilter=new CatalogUtils.CatEntryFilterI() {
    					@Override
    					public boolean accept(CatEntryI entry) {
    						if(entry.getUri().startsWith(dir)){ 
    							if(recursive || StringUtils.contains(entry.getUri().substring(dir.length()+1),"/"))
    							{
        							return (entryFilter == null || entryFilter.accept(entry));
    							}
    						}
							return false;
    					}
    				};
    				
    				
    				//If there are no matching entries, I'm not sure if this should throw a 404, or return an empty list. 
    				if(filepath.endsWith("/")){
    					table.rows().addAll(CatalogUtils.getEntryDetails(cat, parentPath, baseURI + "/resources/" + catResource.getXnatAbstractresourceId() + "/files", catResource, false, folderFilter, proj, locator));
    				}else{
                        getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unable to find catalog entry for given uri.");
                        return new StringRepresentation("");
    				}
                }else if (entry == null) {
                    getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unable to find catalog entry for given uri.");
                    return new StringRepresentation("");
                } else {
                    if (FileUtils.IsAbsolutePath(entry.getUri())) {
                        f = new File(entry.getUri());
                    } else {
                        f = new File(parentPath, entry.getUri());
                    }

                    if (f.exists()) {
                        String fName;
                        if (zipEntry == null) {
                            fName = f.getName().toLowerCase();
                        } else {
                            fName = zipEntry.toLowerCase();
                        }

                        if (mt.equals(MediaType.IMAGE_JPEG) && Dcm2Jpg.isDicom(f)) {
                            try {
                                return new InputRepresentation(new ByteArrayInputStream(Dcm2Jpg.convert(f)), mt);
                            } catch (IOException e) {
                                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unable to convert this file to jpeg : " + e.getMessage());
                                return new StringRepresentation("");
                            }
                        }

                        try {
                            // If the user is requesting a file within the zip archive
                            if (zipEntry != null) {
                                // Get the zip entry requested
                                ZipFile zF = new ZipFile(f);
                                ZipEntry zE = zF.getEntry(URLDecoder.decode(zipEntry, "UTF-8"));
                                if (zE == null) {
                                    getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unable to find file.");
                                    return new StringRepresentation("");
                                } else { // Return the requested zip entry
                                    return new InputRepresentation(zF.getInputStream(zE), buildMediaType(mt, fName));
                                }
                                // If the user is requesting a list of the contents within the zip file
                            } else if (listContents && isFileZipArchive(fName)) {
                                // Get the contents of the zip file
                                ZipFile zF = new ZipFile(f);
                                Enumeration<? extends ZipEntry> entries = zF.entries();

                                // Create a new XFTTable with File Name and Size columns
                                XFTTable t = new XFTTable();
                                t.initTable(new String[]{"File Name", "Size"});

                                // Populate table rows and add the row to the table
                                while (entries.hasMoreElements()) {
                                    ZipEntry zE = entries.nextElement();
                                    t.rows().add(new Object[]{zE.getName(), zE.getSize()});
                                }
                                zF.close();

                                // Set the table, if t has rows
                                if (t.rows().size() != 0) {
                                    table = t;  // table gets passed into representTable() below
                                }
                            } else {
                                // Return the requested file
                                return getFileRepresentation(f, buildMediaType(mt, fName));
                            }
                        } catch (ZipException e) {
                            getResponse().setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, e.getMessage());
                            return new StringRepresentation("");
                        } catch (IOException e) {
                            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, e.getMessage());
                            return new StringRepresentation("");
                        }

                    } else { // If file does not exist
                        getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unable to find file.");
                        return new StringRepresentation("");
                    }
                }
            }
        } else {
            if (filepath == null || filepath.equals("")) {
                String baseURI = getBaseURI();
                if (entryFilter == null) {
                    ArrayList<File> files = resource.getCorrespondingFiles(proj.getRootArchivePath());
                    for (File subFile : files) {
                        Object[] row = new Object[13];
                        row[0] = (subFile.getName());
                        row[1] = (subFile.length());
                        if (locator.equalsIgnoreCase("URI")) {
                            row[2] = baseURI + "/resources/" + resource.getXnatAbstractresourceId() + "/files/" + subFile.getName();
                        } else if (locator.equalsIgnoreCase("absolutePath")) {
                            row[2] = subFile.getAbsolutePath();
                        } else if (locator.equalsIgnoreCase("projectPath")) {
                            row[2] = subFile.getAbsolutePath().substring(proj.getRootArchivePath().substring(0, proj.getRootArchivePath().lastIndexOf(proj.getId())).length());
                        }
                        row[3] = resource.getLabel();
                        row[4] = resource.getTagString();
                        row[5] = resource.getFormat();
                        row[6] = resource.getContent();
                        row[7] = resource.getXnatAbstractresourceId();
                        table.rows().add(row);
                    }
                }
            } else {
                ArrayList<File> files = resource.getCorrespondingFiles(proj.getRootArchivePath());
                for (File subFile : files) {
                    if (subFile.getName().equals(filepath)) {
                        f = subFile;
                        break;
                    }
                }

                if (f != null && f.exists()) {
                    return getFileRepresentation(f, mt);
                } else {
                    getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unable to find file.");
                    return new StringRepresentation("");
                }
            }
        }

        Hashtable<String, Object> params = new Hashtable<>();
        params.put("title", "Files");

        Map<String, Map<String, String>> cp = new Hashtable<>();
        cp.put("URI", new Hashtable<String, String>());
        cp.get("URI").put("serverRoot", getContextPath());

        return representTable(table, mt, params, cp, getSessionMaps());
    }

    /**
     * Function determines if the given file is a zip archive by
     * checking whether the fileName contains a zip extension
     *
     * @param f - the file name
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
        Map<String, String> session_ids = new Hashtable<>();
        // Check if the session is an assessor to an "assessed" session
        if (assesseds.size() > 0) {
        	// Check if the session containing the assessor has an "ASSESSORS" directory.
        	// This signifies that the directory structure is based on a "modern" version of XNAT.
        	if (new File(assesseds.get(0).getSessionDir(),"ASSESSORS").isDirectory() && expts.size() > 0)
        	{
                for (XnatExperimentdata session : expts) {
                    session_ids.put(session.getId(), session.getArchiveDirectoryName());
                }
        	}
        	else
        	{
                //IOWA customization: to include project and subject in path
                boolean projectIncludedInPath = isQueryVariableTrue("projectIncludedInPath");
                boolean subjectIncludedInPath = isQueryVariableTrue("subjectIncludedInPath");
                for (XnatExperimentdata session : assesseds) {
                    String replacing = session.getArchiveDirectoryName();
                    if (subjectIncludedInPath) {
                        if (session instanceof XnatImagesessiondata) {
                            XnatSubjectdata subject = XnatSubjectdata.getXnatSubjectdatasById(((XnatImagesessiondata) session).getSubjectId(), getUser(), false);
                            replacing = subject.getLabel() + "/" + replacing;
                        }
                    }
                    if (projectIncludedInPath) {
                        replacing = session.getProject() + "/" + replacing;
                    }
                    session_ids.put(session.getId(), replacing);
                    //session_ids.put(session.getId(),session.getArchiveDirectoryName());   		
                }
        	}
        } else if (expts.size() > 0) {
            for (XnatExperimentdata session : expts) {
                session_ids.put(session.getId(), session.getArchiveDirectoryName());
            }
        } else if (sub != null) {
            session_ids.put(sub.getId(), sub.getArchiveDirectoryName());
        } else if (proj != null) {
            session_ids.put(proj.getId(), proj.getId());
        }

        return session_ids;
    }

    private ArrayList<String> getSessionIds() {
        ArrayList<String> session_ids = new ArrayList<>();
        if (assesseds.size() > 0) {
            for (XnatExperimentdata session : assesseds) {
                session_ids.add(session.getArchiveDirectoryName());
            }
        } else if (expts.size() > 0) {
            for (XnatExperimentdata session : expts) {
                session_ids.add(session.getArchiveDirectoryName());
            }
        } else if (sub != null) {
            session_ids.add(sub.getArchiveDirectoryName());
        } else if (proj != null) {
            session_ids.add(proj.getId());
        }

        return session_ids;
    }

    private FileRepresentation getFileRepresentation(File f, MediaType mt) {
        FileRepresentation fr = null;
        try {
            fr = setFileRepresentation(f, mt);
        } catch (IOException e) {
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "Unable to return file as " + mt.getName());
        }
        return fr;
    }

    private FileRepresentation setFileRepresentation(File f, MediaType mt) throws IOException {
        setResponseHeader("Cache-Control", "must-revalidate");
        return representFile(f, mt);
    }
}
