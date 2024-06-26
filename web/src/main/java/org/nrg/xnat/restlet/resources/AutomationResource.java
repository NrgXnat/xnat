/*
 * web: org.nrg.xnat.restlet.resources.AutomationResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.nrg.action.ServerException;
import org.nrg.framework.constants.Scope;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SuppressWarnings("WeakerAccess")
@Slf4j
public abstract class AutomationResource extends SecureResource {
    public AutomationResource(final Context context, final Request request, final Response response) throws ResourceException {
        super(context, request, response);

        final String  entityId     = (String) getRequest().getAttributes().get(ENTITY_ID);
        final String  projectId    = (String) getRequest().getAttributes().get(PROJECT_ID);
        final boolean hasEntityId  = StringUtils.isNotBlank(entityId);
        final boolean hasProjectId = StringUtils.isNotBlank(projectId);

        if (hasEntityId && hasProjectId) {
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "You can't specify both entity and project ID. Pick one and stick with it.");
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "You can't specify both entity and project ID. Pick one and stick with it.");
        }

        if (!hasEntityId && !hasProjectId) {
            _scope = null;
            _projectId = null;
            _hasProjectId = false;
        } else if (hasEntityId && entityId.equalsIgnoreCase(Scope.Site.code())) {
            _scope = Scope.Site;
            _projectId = null;
            _hasProjectId = false;
        } else {
            final Map<String, String> values;
            if (hasEntityId) {
                final Map<String, String> entityProps = Scope.decode(entityId);
                _scope = Scope.getScope(entityProps.get("scope"));
                values = validateEntityId(entityProps.get("entityId"));
            } else {
                _scope = Scope.Project;
                values = validateEntityId(projectId);
            }
            // For now, we presume entity ID is a project ID. This will change soon.
            if (values != null) {
                _projectId = values.get(KEY_PROJECT_ID);
                _hasProjectId = true;
            } else {
                _projectId = null;
                _hasProjectId = false;
            }
        }

        _path = request.getResourceRef().getRemainingPart();
    }

    protected void validateProjectAccess(final String projectId, final boolean requiresWrite) throws ResourceException {
        final UserI           user    = getUser();
        final XnatProjectdata project = XnatProjectdata.getXnatProjectdatasById(projectId, user, false);
        if (project == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Can't find project with ID: " + getProjectId());
        }
        try {
            if ((requiresWrite && !project.canEdit(user)) || !project.canRead(user)) {
                log.warn("User {} attempted to access project {} with insufficient privileges.", user.getLogin(), getProjectId());
                throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
            }
        } catch (Exception e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Something went wrong accessing project info for " + getProjectId());
        }
    }

    protected abstract String getResourceType();

    protected abstract String getResourceId();

    protected Scope getScope() {
        return Optional.ofNullable(_scope).orElse(Scope.Site);
    }

    protected boolean hasProjectId() {
        return _hasProjectId;
    }

    protected String getProjectId() {
        return _projectId;
    }

    protected void setProjectId(final String projectId) {
        _projectId = projectId;
        _hasProjectId = StringUtils.isNotBlank(_projectId);
    }

    protected String getAssociation() {
        if (getScope() == null) {
            return null;
        }
        return Scope.encode(getScope(), getProjectId());
    }

    protected void setAssociation(final String association) {
        final Map<String, String> atoms = Scope.decode(association);
        _scope = Scope.getScope(atoms.get("scope"));
        _projectId = _scope == Scope.Site ? null : atoms.get("entityId");
    }

    protected void recordAutomationEvent(final String automationId, final String containerId, final String operation, final Class<?> type) {
        try {
            final EventDetails  instance = EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, operation, "", operation + " " + type + " with ID " + automationId);
            final PersistentWorkflowI workflow = PersistentWorkflowUtils.buildOpenWorkflow(getUser(), type.getName(), automationId, containerId, instance);
            assert workflow != null;
            workflow.setStatus(PersistentWorkflowUtils.COMPLETE);
            WorkflowUtils.save(workflow, workflow.buildEvent());
        } catch (PersistentWorkflowUtils.ActionNameAbsent | PersistentWorkflowUtils.IDAbsent | PersistentWorkflowUtils.JustificationAbsent exception) {
            // This is not really going to happen because we're providing all the attributes required, but we still have to handle it.
            log.warn("An error occurred trying to save a workflow when working with event", exception);
        } catch (Exception exception) {
            log.error("An error occurred trying to save a workflow when working with event", exception);
        }
    }

    protected String getPath() {
        return _path;
    }

    protected String getRequestContext(final String header) {
        final StringBuilder buffer = new StringBuilder(header).append(":\n");
        if (_scope != null) {
            buffer.append(" * Scope: ").append(_scope);
            if (_scope == Scope.Project) {
                buffer.append("\n * Project ID: ").append(_projectId);
            }
        }
        if (StringUtils.isNotBlank(getResourceId())) {
            buffer.append("\n * ").append(getResourceType()).append(" ID: ").append(getResourceId());
        }
        if (StringUtils.isNotBlank(_path)) {
            buffer.append("\n * Path: ").append(_path);
        }
        return buffer.toString();
    }

    protected Properties decodeProperties(final Representation entity, final MediaType mediaType) throws ServerException {
        final Properties properties;
        if (mediaType.equals(MediaType.APPLICATION_WWW_FORM)) {
            try {
                properties = new Properties();
                for (final NameValuePair entry : URLEncodedUtils.parse(entity.getText(), StandardCharsets.UTF_8)) {
                    properties.setProperty(entry.getName(), entry.getValue());
                }
            } catch (IOException e) {
                throw new ServerException(Status.SERVER_ERROR_INTERNAL, "An error occurred trying to read the submitted form body.", e);
            }
        } else {
            try {
                properties = getSerializer().deserializeJson(entity.getText(), Properties.class);
            } catch (IOException e) {
                throw new ServerException(Status.SERVER_ERROR_INTERNAL, "An error occurred processing the script properties", e);
            }
        }
        return properties;
    }

    private Map<String, String> validateEntityId(final String entityId) throws ResourceException {
        if (getScope() == null) {
            return null;
        }

        switch (getScope()) {
            case Site:
                return null;

            case Project:
                if (StringUtils.isBlank(entityId)) {
                    throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "You must specify an ID for the project scope.");
                }
                final Map<String, String> ids = new HashMap<>();
                // Check to see if entityId is actually a project ID. If so, convert it to a Long.
                final Long resolved = XnatProjectdata.getProjectInfoIdFromStringId(entityId);
                if (resolved != null) {
                    ids.put(KEY_PROJECT_DATA_INFO, resolved.toString());
                    ids.put(KEY_PROJECT_ID, entityId);
                } else {
                    if (!NumberUtils.isParsable(entityId)) {
                        throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "The specified entity ID " + entityId + " does not match an existing project ID, but is also not a parseable number, so is not project data ID.");
                    }
                    try {
                        XFTTable table = XFTTable.Execute("SELECT id FROM xnat_projectdata WHERE projectdata_info = " + entityId, getUser().getDBName(), userName);
                        if (table.size() != 1) {
                            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Couldn't find a project with the ID or alias of " + entityId);
                        }
                        ids.put(KEY_PROJECT_DATA_INFO, entityId);
                        ids.put(KEY_PROJECT_ID, (String) table.convertColumnToArrayList("id").get(0));
                    } catch (Exception e) {
                        throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "An error occurred trying to access the database.", e);
                    }
                }
                return ids;

            default:
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "The specified scope " + getScope().code() + " is not currently supported. Supported scopes include: " + Scope.Site.code() + " and " + Scope.Project.code());
        }
    }

    private static final String ENTITY_ID             = "ENTITY_ID";
    private static final String PROJECT_ID            = "PROJECT_ID";
    private static final String KEY_PROJECT_DATA_INFO = "projectDataInfo";
    private static final String KEY_PROJECT_ID        = "projectId";

    private final String  _path;
    private       Scope   _scope;
    private       boolean _hasProjectId;
    private       String  _projectId;
}
