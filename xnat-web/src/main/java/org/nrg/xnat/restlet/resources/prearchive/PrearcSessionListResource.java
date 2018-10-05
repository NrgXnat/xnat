/*
 * web: org.nrg.xnat.restlet.resources.prearchive.PrearcSessionListResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources.prearchive;

import com.google.common.base.Function;
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
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.XFTTable;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.helpers.prearchive.SessionDataTriple;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.util.ArrayList;
import java.util.Hashtable;

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
        _requestedProject = (String) getParameter(request, PROJECT_ATTR);

        // UID to search on
        _tag = getQueryVariable("tag");
        _isAllAccess = Groups.isDataAdmin(getUser()) || Roles.isSiteAdmin(getUser().getUsername());

        if (request.getMethod() == Method.PUT && !_isAllAccess) {
            throw new ClientException(CLIENT_ERROR_FORBIDDEN, "Only site or data administrators can request a rebuild of the prearchive.");
        }
        if (request.getMethod() == GET) {
            if (StringUtils.isNotBlank(_tag) && !_isAllAccess) {
                throw new ClientException(CLIENT_ERROR_FORBIDDEN, "Only site or data administrators can query by tag");
            }
            if (StringUtils.isNotBlank(_requestedProject) && !_isAllAccess && !Permissions.canReadProject(getUser(), _requestedProject)) {
                throw new ClientException(CLIENT_ERROR_FORBIDDEN, "The current user does not have access to the requested project: " + _requestedProject);
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
            logger.error("Unable to refresh sessions", e);
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
        final UserI user = getUser();

        try {
            final XFTTable table;
            if (StringUtils.isNotBlank(getTag())) {
                table = PrearcUtils.convertArrayLtoTable(PrearcDatabase.buildRows(Lists.transform(new ArrayList<>(PrearcDatabase.getSessionByUID(getTag())), new Function<SessionData, SessionDataTriple>() {
                    @Override
                    public SessionDataTriple apply(final SessionData sessionData) {
                        return sessionData.getSessionDataTriple();
                    }
                })));
            } else {
                final String[] projects = StringUtils.isNotBlank(getRequestedProject()) ? getRequestedProject().split("\\s*,\\s*") : getPermissions().getUserReadableProjects(user.getUsername()).toArray(new String[0]);
                table = PrearcUtils.convertArrayLtoTable(PrearcDatabase.buildRows(projects));
            }

            return representTable(table, mediaType, new Hashtable<String, Object>());
        } catch (Exception e) {
            log.error("An error occurred trying to build the table of prearchive entries.");
            getResponse().setStatus(SERVER_ERROR_INTERNAL, e.getMessage());
            return null;
        }
    }

    private static final String PROJECT_ATTR = "PROJECT_ID";

    private final PermissionsServiceImpl    _permissions;
    private final String                    _requestedProject;
    private final String                    _tag;
    private final boolean                   _isAllAccess;
}
