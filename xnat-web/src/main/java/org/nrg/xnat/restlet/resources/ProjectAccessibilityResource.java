/*
 * web: org.nrg.xnat.restlet.resources.ProjectAccessibilityResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

@Slf4j
public class ProjectAccessibilityResource extends SecureResource {
    public ProjectAccessibilityResource(final Context context, final Request request, final Response response) {
        super(context, request, response);

        access = (String) getParameter(request, "ACCESS_LEVEL");

        final String pID = (String) getParameter(request, "PROJECT_ID");

        final boolean hasProjectId          = StringUtils.isNotBlank(pID);
        final boolean missingRequiredAccess = StringUtils.isBlank(access) && request.getMethod() == Method.PUT;

        project = (hasProjectId && !missingRequiredAccess) ? XnatProjectdata.getProjectByIDorAlias(pID, getUser(), false) : null;

        if (!hasProjectId && missingRequiredAccess) {
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "You must specify the PROJECT_ID and ACCESS_ID parameters for this PUT call");
            return;
        }
        if (missingRequiredAccess) {
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "You must specify the ACCESS_ID parameter for this PUT call");
            return;
        }
        if (!hasProjectId) {
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "You must specify the PROJECT_ID parameter for this call");
            return;
        }
        if (project != null) {
            getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        } else {
            response.setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Could not find the project with ID '" + pID + "'");
        }
    }

    @Override
    public boolean allowGet() {
        return true;
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void handlePut() {
        if (StringUtils.isBlank(access) || project == null) {
            return;
        }

        try {
            final UserI user = getUser();
            if (!Permissions.canDelete(user, project)) {
                getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                return;
            }

            final String currentAccess = getProjectAccessibility();
            if (!StringUtils.equals(currentAccess, access)) {
                final PersistentWorkflowI workflow = WorkflowUtils.buildProjectWorkflow(user, project, newEventInstance(EventUtils.CATEGORY.PROJECT_ACCESS, EventUtils.MODIFY_PROJECT_ACCESS));
                final EventMetaI          event    = workflow.buildEvent();
                if (Permissions.setDefaultAccessibility(project.getId(), access, true, user, event)) {
                    WorkflowUtils.complete(workflow, event);
                }
            }
            returnDefaultRepresentation();
        } catch (Exception e) {
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            log.error("An error occurred trying to set the default accessibility of project '{}' to '{}'", project.getId(), access, e);
        }
    }

    @Override
    public Representation represent(final Variant variant) {
        return new StringRepresentation(getProjectAccessibility(), variant.getMediaType());
    }

    private String getProjectAccessibility() {
        if (project != null) {
            try {
                return project.getPublicAccessibility();
            } catch (Exception e) {
                log.error("An error occurred trying to retrieve the accessibility setting for the project '{}'", project.getId(), e);
            }
        }
        return "";
    }

    final private XnatProjectdata project;
    final private String          access;


}
