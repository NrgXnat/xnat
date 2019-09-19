/*
 * web: org.nrg.xnat.restlet.resources.prearchive.PrearcSessionListResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources.prearchive;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.PermissionsServiceImpl;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.XFTTable;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.utils.functions.Functions;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import static lombok.AccessLevel.PROTECTED;
import static org.restlet.data.Method.GET;
import static org.restlet.data.Status.CLIENT_ERROR_FORBIDDEN;
import static org.restlet.data.Status.SERVER_ERROR_INTERNAL;

@Slf4j
@Getter(PROTECTED)
@Accessors(prefix = "_")
public final class PrearcSessionListResource extends SecureResource {
    public PrearcSessionListResource(final Context context, final Request request, final Response response) throws ClientException {
        super(context, request, response);

        _permissions = XDAT.getContextService().getBean(PermissionsServiceImpl.class);

        // Project is explicit in the request
        _project = (String) getParameter(request, PROJECT_ATTR);
        _projectRequest = StringUtils.isNotBlank(getProject());

        // UID to search on
        _tag = getQueryVariable("tag");
        _tagRequest = StringUtils.isNotBlank(getTag());

        final UserI user = getUser();
        _dataAdmin = Groups.hasAllDataAdmin(user);
        _dataAccess = Groups.hasAllDataAccess(user);

        if (request.getMethod() == Method.PUT && !isDataAdmin()) {
            throw new ClientException(CLIENT_ERROR_FORBIDDEN, "Only site or data administrators can request a rebuild of the prearchive.");
        }
        if (request.getMethod() == GET) {
            if (isTagRequest() && !isDataAccess()) {
                throw new ClientException(CLIENT_ERROR_FORBIDDEN, "Only site or data administrators and reviewers can query by tag");
            }
            if (isProjectRequest() && !isDataAccess() && !Permissions.canReadProject(user, getProject())) {
                throw new ClientException(CLIENT_ERROR_FORBIDDEN, "The current user does not have access to the requested project: " + getProject());
            }
        }

        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new Variant(MediaType.TEXT_XML));
    }

    /**
     * Refresh all the sessions
     */
    @Override
    public boolean allowPut() {
        return true;
    }

    public void handlePut() {
        try {
            PrearcDatabase.refresh(true);
        } catch (Exception e) {
            log.error("Unable to refresh sessions", e);
            getResponse().setStatus(SERVER_ERROR_INTERNAL, e.getMessage());
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see org.restlet.resource.Resource#represent(org.restlet.resource.Variant)
     */
    @Override
    public Representation represent(final Variant variant) { 
        final MediaType mediaType = overrideVariant(variant);

        try {
            final XFTTable table;
            if (StringUtils.isNotBlank(getTag())) {
                table = PrearcUtils.convertArrayLtoTable(PrearcDatabase.buildRows(Lists.transform(new ArrayList<>(PrearcDatabase.getSessionByUID(getTag())), Functions.SESSION_DATA_TO_SESSION_DATA_TRIPLE)));
            } else {
                final List<String> projects = new ArrayList<>(StringUtils.isNotBlank(getProject()) ? Arrays.asList(getProject().split("\\s*,\\s*")) : getPermissions().getUserEditableProjects(getUser().getUsername()));
                if (isDataAccess()) {
                    projects.add(null);
                }
                table = PrearcUtils.convertArrayLtoTable(PrearcDatabase.buildRows(projects.toArray(new String[0])));
            }

            return representTable(table, mediaType, new Hashtable<String, Object>());
        } catch (Exception e) {
            log.error("An error occurred trying to build the table of prearchive entries.");
            getResponse().setStatus(SERVER_ERROR_INTERNAL, e.getMessage());
            return null;
        }
    }

    private static final String PROJECT_ATTR = "PROJECT_ID";

    private final PermissionsServiceImpl _permissions;
    private final String                 _project;
    private final boolean                _projectRequest;
    private final String                 _tag;
    private final boolean                _tagRequest;
    private final boolean                _dataAdmin;
    private final boolean                _dataAccess;
}
