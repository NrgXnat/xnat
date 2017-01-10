/*
 * web: org.nrg.xapi.rest.AbstractXapiProjectRestController
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest;

import org.jetbrains.annotations.Nullable;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.rest.AbstractXapiRestController;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.springframework.http.HttpStatus;

/**
 * Adds XNAT data-model-aware functions to the base XAPI REST controller.
 */
public abstract class AbstractXapiProjectRestController extends AbstractXapiRestController {
    protected AbstractXapiProjectRestController(final UserManagementServiceI userManagementService, final RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
    }

    @Nullable
    protected HttpStatus canReadProject(final String projectId) throws Exception {
        final UserI user = getSessionUser();
        final XnatProjectdata project = XnatProjectdata.getProjectByIDorAlias(projectId, user, false);
        if (project == null) {
            return HttpStatus.NOT_FOUND;
        }
        if (!project.canRead(user)) {
            return HttpStatus.FORBIDDEN;
        }
        return null;
    }

    @Nullable
    protected HttpStatus canEditProject(final String projectId) throws Exception {
        final UserI user = getSessionUser();
        final XnatProjectdata project = XnatProjectdata.getProjectByIDorAlias(projectId, user, false);
        if (project == null) {
            return HttpStatus.NOT_FOUND;
        }
        if (!project.canEdit(user)) {
            return HttpStatus.FORBIDDEN;
        }
        return null;
    }

    @Nullable
    protected HttpStatus canDeleteProject(final String projectId) throws Exception {
        final UserI user = getSessionUser();
        final XnatProjectdata project = XnatProjectdata.getProjectByIDorAlias(projectId, user, false);
        if (project == null) {
            return HttpStatus.NOT_FOUND;
        }
        if (!project.canDelete(user)) {
            return HttpStatus.FORBIDDEN;
        }
        return null;
    }


}
