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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.ArcProject;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.base.BaseXnatProjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.XftStringUtils;
import org.nrg.xnat.helpers.xmlpath.XMLPathShortcuts;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Collection;
import java.util.List;

import static org.nrg.xdat.om.base.auto.AutoXnatProjectdata.SCHEMA_ELEMENT_NAME;
import static org.restlet.data.Status.*;

@Slf4j
public class ProjectResource extends ItemResource {
    private final XnatProjectdata project;
    private final String          projectId;

    public ProjectResource(Context context, Request request, Response response) throws ClientException {
        super(context, request, response);

        // This was part of a fix for XNAT-3453, but it breaks other non-standard REST ways of setting project properties.
        // if (!validateCleanUrl(request, response)) {
        //     throw new ResourceException(response.getStatus());
        // }

        projectId = (String) getParameter(request, "PROJECT_ID");
        if (StringUtils.isBlank(projectId)) {
            throw new ClientException(CLIENT_ERROR_BAD_REQUEST, "No project ID specified for the REST call. This shouldn't happen.");
        }
        project = XnatProjectdata.getProjectByIDorAlias(projectId, getUser(), false);

        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new Variant(MediaType.TEXT_XML));

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
        if (project == null || StringUtils.isNotBlank(filepath)) {
            getResponse().setStatus(CLIENT_ERROR_BAD_REQUEST);
            return;
        }
        final UserI user = getUser();
        try {
            if (user.isGuest() || !Permissions.canDelete(user, project)) {
                getResponse().setStatus(CLIENT_ERROR_FORBIDDEN);
                return;
            }
        } catch (Exception e) {
            log.error("An error occurred checking permissions for user " + user.getUsername() + " to delete the project " + projectId, e);
            getResponse().setStatus(SERVER_ERROR_INTERNAL);
            return;
        }

        try {
            final PersistentWorkflowI workflow = WorkflowUtils.getOrCreateWorkflowData(getEventId(), user, SCHEMA_ELEMENT_NAME, projectId, projectId, newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN, EventUtils.getDeleteAction(XnatProjectdata.SCHEMA_ELEMENT_NAME)));
            final EventMetaI          event    = workflow.buildEvent();

            try {
                project.delete(isQueryVariableTrue("removeFiles"), user, event);
                PersistentWorkflowUtils.complete(workflow, event);
                return;
            } catch (Exception e) {
                log.error("An error occurred when user " + user.getUsername() + " tried to delete the project " + projectId, e);
                PersistentWorkflowUtils.fail(workflow, event);
            }
        } catch (Exception e) {
            log.error("An error occurred trying manage delete operation for user " + user.getUsername() + " on project " + projectId, e);
        }
        // If we got here, the delete operation failed, so the server error status should always be set.
        getResponse().setStatus(SERVER_ERROR_INTERNAL);

    }

    @Override
    public void handlePut() {
        final UserI user = getUser();
        if (user.isGuest()) {
            getResponse().setStatus(CLIENT_ERROR_FORBIDDEN);
            return;
        }

        // Project equal to null means a new project, so either non-admins must be able to create projects or the user must be an admin.
        if (project == null && !XDAT.getSiteConfigPreferences().getUiAllowNonAdminProjectCreation() && !Roles.isSiteAdmin(user)) {
            getResponse().setStatus(CLIENT_ERROR_FORBIDDEN, "User " + user.getUsername() + " doesn't have permission to create projects on this system");
            return;
        }
        // All file path settings require an existing project, so if there's a file path and no project, that's bad, m'kay?
        final boolean hasFilePath = StringUtils.isNotBlank(filepath);
        if (hasFilePath && project == null) {
            getResponse().setStatus(CLIENT_ERROR_BAD_REQUEST, "You can't set the '" + StringUtils.substringBefore(filepath, "/") + "' attribute without specifying the project on which you want to set it.");
            return;
        }
        // If we do have a project, we can go ahead and check permissions to edit it now before we go any farther.
        if (project != null && !Permissions.canEditProject(user, projectId)) {
            getResponse().setStatus(CLIENT_ERROR_FORBIDDEN, "User " + user.getUsername() + " doesn't have permission to edit the project " + projectId);
            return;
        }

        try {
            if (project == null || Permissions.canEdit(user, project)) {
                XFTItem item = getProjectXftItem(user);

                if (item == null) {
                    getResponse().setStatus(CLIENT_ERROR_EXPECTATION_FAILED, "Need PUT Contents");
                    return;
                }

                final boolean allowDataDeletion = BooleanUtils.toBoolean(getQueryVariable("allowDataDeletion"));
                if (item.instanceOf("xnat:projectData")) {
                    XnatProjectdata workingProject = new XnatProjectdata(item);

                    if (hasFilePath) {
                        if (StringUtils.isBlank(workingProject.getId())) {
                            item = project.getItem();
                            workingProject = project;
                        }

                        if (!Permissions.canEdit(user, item)) {
                            getResponse().setStatus(CLIENT_ERROR_FORBIDDEN, "User account doesn't have permission to edit this project.");
                            return;
                        }
                        if (filepath.startsWith("quarantine_code/")) {
                            final String quarantineCode = StringUtils.removeStart(filepath, "quarantine_code/");
                            if (StringUtils.isNotBlank(quarantineCode)) {
                                final ArcProject arcProject = workingProject.getArcSpecification();
                                arcProject.setQuarantineCode(translateArcProjectCode(quarantineCode));
                                create(workingProject, arcProject, false, false, newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN, "Configured quarantine code"));
                                ArcSpecManager.Reset();
                            }
                        } else if (filepath.startsWith("prearchive_code/")) {
                            final String prearchiveCode = StringUtils.removeStart(filepath, "prearchive_code/");
                            if (StringUtils.isNotBlank(prearchiveCode)) {
                                if (XDAT.getBoolSiteConfigurationProperty("project.allow-auto-archive", true) || StringUtils.equals(prearchiveCode, "0")) {
                                    final ArcProject arcProject = workingProject.getArcSpecification();
                                    arcProject.setPrearchiveCode(translateArcProjectCode(prearchiveCode));
                                    create(workingProject, arcProject, false, false, newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN, "Configured prearchive code"));
                                    ArcSpecManager.Reset();
                                } else {
                                    getResponse().setStatus(CLIENT_ERROR_FORBIDDEN);
                                }
                            }
                        } else if (filepath.startsWith("current_arc/")) {
                            final String currentArc = StringUtils.removeStart(filepath, "current_arc/");
                            if (StringUtils.isNotBlank(currentArc)) {
                                final ArcProject arcProject = workingProject.getArcSpecification();
                                arcProject.setCurrentArc(currentArc);
                                create(workingProject, arcProject, false, false, newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN, "Configured current arc"));
                                ArcSpecManager.Reset();
                            }
                        } else if (filepath.startsWith("scan_type_mapping/")) {
                            final String scanTypeMapping = StringUtils.removeStart(filepath, "scan_type_mapping/");
                            workingProject.setUseScanTypeMapping(BooleanUtils.toBoolean(scanTypeMapping));
                            update(workingProject, false, false, newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN, "Configured current arc"));
                            ArcSpecManager.Reset();
                        } else {
                            getResponse().setStatus(CLIENT_ERROR_BAD_REQUEST);
                        }
                    } else {
                        if (StringUtils.isBlank(workingProject.getId())) {
                            workingProject.setId(projectId);
                        } else if (!StringUtils.equalsIgnoreCase(projectId, workingProject.getId())) {
                            getResponse().setStatus(CLIENT_ERROR_EXPECTATION_FAILED, "The project ID for the REST call must match the value in submitted request body.");
                            return;
                        }

                        if (!XftStringUtils.isValidId(workingProject.getId()) && !isQueryVariableTrue("testHyphen")) {
                            getResponse().setStatus(CLIENT_ERROR_EXPECTATION_FAILED, "Invalid character in project ID.");
                            return;
                        }

                        if (item.getCurrentDBVersion() != null) {
                            if (!Permissions.canEdit(user, item)) {
                                getResponse().setStatus(CLIENT_ERROR_FORBIDDEN, "User account doesn't have permission to edit this project.");
                                return;
                            }
                        } else {
                            final Long count = XDAT.getContextService().getBean(NamedParameterJdbcTemplate.class).queryForObject("SELECT COUNT(id) FROM xnat_projectdata_history WHERE id = :projectId", new MapSqlParameterSource("projectId", projectId), Long.class);
                            if (count > 0) {
                                getResponse().setStatus(CLIENT_ERROR_FORBIDDEN, "Project '" + projectId + "' was used in a previously deleted project and cannot be reused.");
                                return;
                            }
                        }

                        // Validate project fields.  If there are conflicts, build a error message and display it to the user.
                        final Collection<String> conflicts = workingProject.validateProjectFields();
                        if (!conflicts.isEmpty()) {
                            getResponse().setStatus(CLIENT_ERROR_CONFLICT, "Requested new project conflicts with existing projects: " + StringUtils.join(conflicts, ", "));
                            return;
                        }

                        final String accessibility = getQueryVariable("accessibility");
                        if (project == null) {
                            BaseXnatProjectdata.createProject(workingProject, user, allowDataDeletion, true, newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN), accessibility);
                        } else {
                            SaveItemHelper.authorizedSave(item, user, false, false, newEventInstance(EventUtils.CATEGORY.PROJECT_ADMIN));
                            if (StringUtils.isNotBlank(accessibility) && !StringUtils.equals(workingProject.getPublicAccessibility(), accessibility)) {
                                final PersistentWorkflowI workflow = WorkflowUtils.buildProjectWorkflow(user, project, newEventInstance(EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.MODIFY_PROJECT_ACCESS));
                                Permissions.setDefaultAccessibility(workingProject.getId(), accessibility, false, user, workflow.buildEvent());
                            }
                            XDAT.triggerXftItemEvent(XnatProjectdata.SCHEMA_ELEMENT_NAME, projectId, XftItemEventI.UPDATE);
                        }
                    }
                }
            } else {
                getResponse().setStatus(CLIENT_ERROR_FORBIDDEN, "User account doesn't have permission to edit this project.");
            }
        } catch (ActionException e) {
            getResponse().setStatus(e.getStatus(), e.getMessage());
        } catch (InvalidPermissionException | IllegalArgumentException e) {
            getResponse().setStatus(CLIENT_ERROR_FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            log.error("Unknown exception type", e);
            getResponse().setStatus(SERVER_ERROR_INTERNAL);
        }
    }

    @Nullable
    protected XFTItem getProjectXftItem(final UserI user) throws ClientException, ServerException, XFTInitException, ElementNotFoundException {
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
        return item;
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
                log.error("", e1);
            }

            try {
                if (handler != null) {
                    return handler.handle(this, variant);
                } else {
                    return null;
                }
            } catch (Exception e) {
                log.error("", e);
                getResponse().setStatus(SERVER_ERROR_INTERNAL);
                return null;
            }
        } else {
            getResponse().setStatus(CLIENT_ERROR_NOT_FOUND, "Unable to find the specified experiment.");
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
        public Representation handle(SecureResource resource, Variant variant) {
            MediaType       mt           = resource.overrideVariant(variant);
            ProjectResource projResource = (ProjectResource) resource;
            if (resource.filepath != null && !resource.filepath.equals("")) {
                if (resource.filepath.equals("quarantine_code")) {
                    try {
                        return new StringRepresentation(projResource.project.getArcSpecification().getQuarantineCode().toString(), mt);
                    } catch (Throwable e) {
                        log.error("", e);
                        projResource.getResponse().setStatus(SERVER_ERROR_INTERNAL, e.getMessage());
                        return null;
                    }
                } else if (resource.filepath.startsWith("prearchive_code")) {
                    try {
                        return new StringRepresentation(projResource.project.getArcSpecification().getPrearchiveCode().toString(), mt);
                    } catch (Throwable e) {
                        log.error("", e);
                        projResource.getResponse().setStatus(SERVER_ERROR_INTERNAL, e.getMessage());
                        return null;
                    }
                } else if (resource.filepath.startsWith("current_arc")) {
                    try {
                        return new StringRepresentation(projResource.project.getArcSpecification().getCurrentArc(), mt);
                    } catch (Throwable e) {
                        log.error("", e);
                        resource.getResponse().setStatus(SERVER_ERROR_INTERNAL, e.getMessage());
                        return null;
                    }
                } else {
                    resource.getResponse().setStatus(CLIENT_ERROR_BAD_REQUEST);
                    return null;
                }
            } else {
                return projResource.representItem(projResource.project.getItem(), mt);
            }
        }
    }

    private int translateArcProjectCode(final String code) throws ClientException {
        if (NumberUtils.isCreatable(code)) {
            return NumberUtils.createInteger(code);
        }
        switch (code) {
            case "true":
                return 1;
            case "false":
                return 0;
            default:
                throw new ClientException(CLIENT_ERROR_BAD_REQUEST, "The submitted code " + code + " is invalid: must be an integer.");
        }
    }
}
