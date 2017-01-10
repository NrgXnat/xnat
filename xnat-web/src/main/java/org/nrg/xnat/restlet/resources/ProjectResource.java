/*
 * web: org.nrg.xnat.restlet.resources.ProjectResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ActionException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.ArcProject;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.base.BaseXnatProjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.XftStringUtils;
import org.nrg.xnat.helpers.xmlpath.XMLPathShortcuts;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

public class ProjectResource extends ItemResource {
    private static final Logger logger = LoggerFactory.getLogger(ProjectResource.class);

    private XnatProjectdata project = null;
    private final String projectId;

    public ProjectResource(Context context, Request request, Response response) throws ResourceException {
        super(context, request, response);

        // This was part of a fix for XNAT-3453, but it breaks other non-standard REST ways of setting project properties.
        // if (!validateCleanUrl(request, response)) {
        //     throw new ResourceException(response.getStatus());
        // }

        projectId = (String) getParameter(request, "PROJECT_ID");
        if (projectId != null) {
            project = XnatProjectdata.getProjectByIDorAlias(projectId, getUser(), false);
        }

        if (project != null) {
            getVariants().add(new Variant(MediaType.TEXT_HTML));
            getVariants().add(new Variant(MediaType.TEXT_XML));
        }

        fieldMapping.putAll(XMLPathShortcuts.getInstance().getShortcuts(XMLPathShortcuts.PROJECT_DATA, false));
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void handleDelete() {
        final UserI user = getUser();

        if (user == null || user.isGuest()) {
            getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
        } else {
            if (filepath != null && !filepath.equals("")) {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return;
            }

            if (project != null) {
                try {
                    final boolean removeFiles = isQueryVariableTrue("removeFiles");
                    if (Permissions.canDelete(user, project)) {
                        final PersistentWorkflowI workflow = WorkflowUtils.getOrCreateWorkflowData(getEventId(), user, XnatProjectdata.SCHEMA_ELEMENT_NAME, project.getId(), project.getId(), newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN, EventUtils.getDeleteAction(project.getXSIType())));
                        final EventMetaI ci = workflow.buildEvent();

                        try {
                            project.delete(removeFiles, user, ci);
                            PersistentWorkflowUtils.complete(workflow, ci);

                            Date curr = new Date();
                            long timestamp = curr.getTime();
                            String query = "UPDATE xnat_projectdata_history SET id = '" + project.getId() + timestamp + "' where id = '" + project.getId() + "';";
                            PoolDBUtils.ExecuteNonSelectQuery(query, project.getItem().getDBName(), user.getLogin());
                        } catch (Exception e) {
                            logger.error("", e);
                            PersistentWorkflowUtils.fail(workflow, ci);
                            throw e;
                        }
                    } else {
                        getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "User account doesn't have permission to delete this project.");
                    }
                } catch (Exception e) {
                    logger.error("", e);
                    getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                }
            }
        }
    }

    @Override
    public void handlePut() {
        final UserI user = getUser();
        if (user == null || user.isGuest()) {
            getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
        } else {
            try {
                if (project == null || Permissions.canEdit(user, project)) {
                    XFTItem item = loadItem("xnat:projectData", true);

                    if (item == null) {
                        String xsiType = getQueryVariable("xsiType");
                        if (xsiType != null) {
                            item = XFTItem.NewItem(xsiType, user);
                        }
                    }

                    if (item == null) {
                        if (project != null) {
                            item = project.getItem();
                        }
                    }

                    if (item == null) {
                        getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Need PUT Contents");
                        return;
                    }

                    boolean allowDataDeletion = false;
                    if (getQueryVariable("allowDataDeletion") != null && getQueryVariable("allowDataDeletion").equalsIgnoreCase("true")) {
                        allowDataDeletion = true;
                    }

                    if (item.instanceOf("xnat:projectData")) {
                        XnatProjectdata project = new XnatProjectdata(item);

                        if (filepath != null && !filepath.equals("")) {
                            if (project.getId() == null) {
                                item = this.project.getItem();
                                project = this.project;
                            }

                            if (!Permissions.canEdit(user, item)) {
                                getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "User account doesn't have permission to edit this project.");
                                return;
                            }
                            if (filepath.startsWith("quarantine_code/")) {
                                String qc = filepath.substring(16);
                                if (!qc.equals("")) {
                                    ArcProject ap = project.getArcSpecification();
                                    try {
                                        Integer qcI = Integer.valueOf(qc);
                                        ap.setQuarantineCode(qcI);
                                    } catch (NumberFormatException e) {
                                        switch (qc) {
                                            case "true":
                                                ap.setQuarantineCode(1);
                                                break;
                                            case "false":
                                                ap.setQuarantineCode(0);
                                                break;
                                            default:
                                                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Prearchive code must be an integer.");
                                                return;
                                        }
                                    }

                                    create(project, ap, false, false, newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN, "Configured quarantine code"));
                                    ArcSpecManager.Reset();
                                }
                            } else if (filepath.startsWith("prearchive_code/")) {
                                String qc = filepath.substring(16);
                                if (!qc.equals("")) {
                                    if (XDAT.getBoolSiteConfigurationProperty("project.allow-auto-archive", true) || StringUtils.equals(qc, "0")) {
                                        ArcProject ap = project.getArcSpecification();
                                        try {
                                            Integer qcI = Integer.valueOf(qc);
                                            ap.setPrearchiveCode(qcI);
                                        } catch (NumberFormatException e) {
                                            switch (qc) {
                                                case "true":
                                                    ap.setPrearchiveCode(1);
                                                    break;
                                                case "false":
                                                    ap.setPrearchiveCode(0);
                                                    break;
                                                default:
                                                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Prearchive code must be an integer.");
                                                    return;
                                            }
                                        }
                                        create(project, ap, false, false, newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN, "Configured prearchive code"));
                                        ArcSpecManager.Reset();
                                    } else {
                                        this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                                    }
                                }
                            } else if (filepath.startsWith("current_arc/")) {
                                String qc = filepath.substring(12);
                                if (!qc.equals("")) {
                                    ArcProject ap = project.getArcSpecification();
                                    ap.setCurrentArc(qc);

                                    create(project, ap, false, false, newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN, "Configured current arc"));
                                    ArcSpecManager.Reset();
                                }
                            } else if (filepath.startsWith("scan_type_mapping/")) {
                                String stm = filepath.substring(18);
                                if (stm.equals("false")) {
                                    project.setUseScanTypeMapping(false);
                                } else if (stm.equals("true")) {
                                    project.setUseScanTypeMapping(true);
                                }
                            } else {
                                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                            }
                        } else {
                            if (StringUtils.isBlank(project.getId())) {
                                project.setId(projectId);
                            }

                            if (StringUtils.isBlank(project.getId())) {
                                getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Requires XNAT ProjectData ID");
                                return;
                            }

                            if (!XftStringUtils.isValidId(project.getId()) && !isQueryVariableTrue("testHyphen")) {
                                getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Invalid character in project ID.");
                                return;
                            }

                            if (item.getCurrentDBVersion() != null) {
                                if (!Permissions.canEdit(user, item)) {
                                    getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "User account doesn't have permission to edit this project.");
                                    return;
                                }
                            } else {
                                Long count = (Long) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(ID) FROM xnat_projectdata_history WHERE ID='" + project.getId() + "';", "COUNT", null, null);
                                if (count > 0) {
                                    getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Project '" + project.getId() + "' was used in a previously deleted project and cannot be reused.");
                                    return;
                                }
                            }

                            if (XDAT.getSiteConfigPreferences().getUiAllowNonAdminProjectCreation() || Roles.isSiteAdmin(user)) {
                                project.preSave();
                                BaseXnatProjectdata.createProject(project, user, allowDataDeletion, true, newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN), getQueryVariable("accessibility"));
                            } else {
                                getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "User account doesn't have permission to edit this project.");
                            }
                        }
                    }
                } else {
                    getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "User account doesn't have permission to edit this project.");
                }
            } catch (ActionException e) {
                getResponse().setStatus(e.getStatus(), e.getMessage());
            } catch (InvalidPermissionException | IllegalArgumentException e) {
                getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, e.getMessage());
            } catch (Exception e) {
                logger.error("Unknown exception type", e);
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            }
        }
    }

    @Override
    public Representation represent(Variant variant) {

        if (project != null) {
            FilteredResourceHandlerI handler = null;
            try {
                final List<FilteredResourceHandlerI> handlers = getHandlers("org.nrg.xnat.restlet.projectResource.extensions", _defaultHandlers);
                for (final FilteredResourceHandlerI filter : handlers) {
                    if (filter.canHandle(this)) {
                        handler = filter;
                    }
                }
            } catch (InstantiationException | IllegalAccessException e1) {
                logger.error("", e1);
            }

            try {
                if (handler != null) {
                    return handler.handle(this, variant);
                } else {
                    return null;
                }
            } catch (Exception e) {
                logger.error("", e);
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                return null;
            }
        } else {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unable to find the specified experiment.");
            return null;
        }
    }

    public String getProjectId() {
        return project == null ? projectId : project.getId();
    }

    public final static List<FilteredResourceHandlerI> _defaultHandlers = Lists.newArrayList();

    static {
        _defaultHandlers.add(new DefaultProjectHandler());
    }

    public static class DefaultProjectHandler implements FilteredResourceHandlerI {

        @Override
        public boolean canHandle(SecureResource resource) {
            return true;
        }

        @Override
        public Representation handle(SecureResource resource, Variant variant) throws Exception {
            MediaType mt = resource.overrideVariant(variant);
            ProjectResource projResource = (ProjectResource) resource;
            if (resource.filepath != null && !resource.filepath.equals("")) {
                if (resource.filepath.equals("quarantine_code")) {
                    try {
                        return new StringRepresentation(projResource.project.getArcSpecification().getQuarantineCode().toString(), mt);
                    } catch (Throwable e) {
                        logger.error("", e);
                        projResource.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
                        return null;
                    }
                } else if (resource.filepath.startsWith("prearchive_code")) {
                    try {
                        return new StringRepresentation(projResource.project.getArcSpecification().getPrearchiveCode().toString(), mt);
                    } catch (Throwable e) {
                        logger.error("", e);
                        projResource.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
                        return null;
                    }
                } else if (resource.filepath.startsWith("current_arc")) {
                    try {
                        return new StringRepresentation(projResource.project.getArcSpecification().getCurrentArc(), mt);
                    } catch (Throwable e) {
                        logger.error("", e);
                        resource.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
                        return null;
                    }
                } else {
                    resource.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    return null;
                }
            } else {
                return projResource.representItem(projResource.project.getItem(), mt);
            }
        }

    }
}
