/*
 * web: org.nrg.xnat.restlet.extensions.StudyRoutingRestlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.extensions;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.services.StudyRoutingService;
import org.nrg.xft.XFTTable;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.restlet.XnatRestlet;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XnatRestlet(value = {"/routing", "/routing/{STUDY_INSTANCE_UID}"})
public class StudyRoutingRestlet extends SecureResource {
    private static final Logger _log = LoggerFactory.getLogger(StudyRoutingRestlet.class);
    private static final String STUDY_INSTANCE_UID = "STUDY_INSTANCE_UID";
    private static final String[] COLUMNS = new String[] { "studyInstanceUID", StudyRoutingService.PROJECT, StudyRoutingService.SUBJECT, StudyRoutingService.USER, StudyRoutingService.CREATED, StudyRoutingService.ACCESSED, StudyRoutingService.LABEL };

    private final StudyRoutingService _routingService;
    private final String _studyInstanceUid;
    private final String _projectId;

    public StudyRoutingRestlet(Context context, Request request, Response response) throws ResourceException {
        super(context, request, response);

        getVariants().add(new Variant(MediaType.ALL));

        if (hasBodyVariable(StudyRoutingService.SUBJECT) || hasBodyVariable(StudyRoutingService.LABEL)) {
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "This restlet currently only supports assigning to project, not subject or label.");
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "This restlet currently only supports assigning to project, not subject or label.");
        }

        _routingService = XDAT.getContextService().getBean(StudyRoutingService.class);
        _studyInstanceUid = (String) request.getAttributes().get(STUDY_INSTANCE_UID);
        _projectId = hasBodyVariable(StudyRoutingService.PROJECT) ? getBodyVariable(StudyRoutingService.PROJECT) : null;

        if (request.getMethod().equals(Method.PUT)) {
            if (StringUtils.isBlank(_studyInstanceUid) || StringUtils.isBlank(_projectId)) {
                response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "You must specify both a study instance UID and the project ID that you wish to assign.");
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "You must specify both a study instance UID and the project ID that you wish to assign.");
            }
            final MediaType mediaType = getMediaType();
            if (mediaType == null || !mediaType.equals(MediaType.APPLICATION_WWW_FORM)) {
                throw new ResourceException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, "This function currently only supports " + MediaType.APPLICATION_WWW_FORM + " PUTs.");
            }
        }

        if (_log.isDebugEnabled()) {
            final StringBuilder message = new StringBuilder("Processing a ").append(request.getMethod().toString()).append(" request");
            if (StringUtils.isBlank(_studyInstanceUid)) {
                message.append(", no study instance UID specified");
            } else {
                message.append(" for study instance UID ").append(_studyInstanceUid);
            }
            if (StringUtils.isBlank(_projectId)) {
                message.append(", no project ID specified in the form body");
            } else {
                message.append(", project ID set to ").append(_projectId);
            }
            _log.debug(message.toString());
        }
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public Representation represent(final Variant variant) {
        final MediaType mediaType = overrideVariant(variant);
        final UserI     user      = getUser();
        if (StringUtils.isNotBlank(_studyInstanceUid)) {
            final Map<String, String> routing = _routingService.findStudyRouting(_studyInstanceUid);
            if (routing == null || routing.size() == 0) {
                getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Couldn't find a routing for the study instance UID: " + _studyInstanceUid);
                _log.info("Request made for routing for study instance UID {}, but nothing was found for that value.", _studyInstanceUid);
                return null;
            } else {
                try {
                    if (!UserHelper.getUserHelperService(user).hasEditAccessToSessionDataByTag(getRoutingAttribute(routing, StudyRoutingService.PROJECT))) {
                        getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "You do not have sufficient privileges to view the routing for this study instance UID.");
                        return null;
                    }
                } catch (Exception e) {
                    final String message = "An error occurred trying to resolve privileges on the study routing for study instance UID: " + _studyInstanceUid;
                    _log.error(message, e);
                    getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e, message);
                    return null;
                }
                final XFTTable table = new XFTTable();
                table.initTable(COLUMNS);
                table.insertRowItems(_studyInstanceUid, getRoutingAttribute(routing, StudyRoutingService.PROJECT), getRoutingAttribute(routing, StudyRoutingService.SUBJECT), getRoutingAttribute(routing, StudyRoutingService.USER), getRoutingAttribute(routing, StudyRoutingService.CREATED), getRoutingAttribute(routing, StudyRoutingService.ACCESSED), getRoutingAttribute(routing, StudyRoutingService.LABEL));
                if (_log.isDebugEnabled()) {
                    _log.debug("Request made for routing for study instance UID {}, found routing: {}", _studyInstanceUid, routing.toString());
                }
                return representTable(table, mediaType, null);
            }
        } else {
            final XFTTable table = new XFTTable();
            table.initTable(COLUMNS);
            Map<String, Map<String, String>> routings = _routingService.findAllRoutings();
            if (routings != null && routings.size() > 0) {
                if (_log.isDebugEnabled()) {
                    _log.debug("Request made for all system routings, found {} results", routings.size());
                }
                for (final String studyInstanceUid : routings.keySet()) {
                    final Map<String, String> routing = routings.get(studyInstanceUid);
                    try {
                        if (UserHelper.getUserHelperService(user).hasEditAccessToSessionDataByTag(getRoutingAttribute(routing, StudyRoutingService.PROJECT))) {
                            table.insertRowItems(studyInstanceUid, getRoutingAttribute(routing, StudyRoutingService.PROJECT), getRoutingAttribute(routing, StudyRoutingService.SUBJECT), getRoutingAttribute(routing, StudyRoutingService.USER), getRoutingAttribute(routing, StudyRoutingService.CREATED), getRoutingAttribute(routing, StudyRoutingService.ACCESSED), getRoutingAttribute(routing, StudyRoutingService.LABEL));
                        }
                    } catch (Exception e) {
                        final String message = "An error occurred trying to resolve privileges on the study routing for study instance UID: " + _studyInstanceUid;
                        _log.error(message, e);
                        getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e, message);
                        return null;
                    }
                }
            } else if (_log.isDebugEnabled()) {
                _log.debug("Request made for all system routings, found no results!");
            }
            return representTable(table, mediaType, null);
        }
    }

    @Override
    public void handlePut() {
        try {
            final UserI user = getUser();
            if (!Permissions.can(user,"xnat:mrSessionData/project", _projectId, SecurityManager.EDIT)) {
                getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "You do not have sufficient privileges to modify study routings for this project.");
                return;
            }
            final Map<String, String> routing = _routingService.findStudyRouting(_studyInstanceUid);
            if (routing == null) {
                if (_log.isDebugEnabled()) {
                    _log.debug("Creating new study routing assignment for study instance UID {} to project {}, created by user {}", _studyInstanceUid, _projectId, user.getLogin());
                }
                _routingService.assign(_studyInstanceUid, _projectId, user.getLogin());
            } else if (!StringUtils.equals(routing.get(StudyRoutingService.PROJECT), _projectId)) {
                final String existing = routing.get(StudyRoutingService.PROJECT);
                if (!Permissions.can(user,"xnat:mrSessionData/project", existing, SecurityManager.EDIT)) {
                    getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "You are trying to reassign study instance UID " + _studyInstanceUid + " from project " + existing + " but do not have sufficient privileges to modify study routings for this project.");
                    return;
                }
                if (_log.isDebugEnabled()) {
                    _log.debug("Updating routing assignment for study instance UID {} to project {}, updated by user {}", _studyInstanceUid, _projectId, user.getLogin());
                }
                routing.put(StudyRoutingService.PROJECT, _projectId);
                _routingService.update(_studyInstanceUid, routing);
            }
        } catch (Exception e) {
            _log.error("An error occurred checking user permissions", e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e, "An error occurred checking user permissions");
        }
    }

    @Override
    public void handleDelete() {
        final UserI user = getUser();
        if (StringUtils.isBlank(_studyInstanceUid)) {
            if (!Roles.isSiteAdmin(user)) {
                getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "You must be a site administrator to delete all study routings for this site.");
                return;
            }
            _log.warn("Closing all study instance UID assignments, per user {}", user.getLogin());
            _routingService.closeAll();
        } else {
            try {
                Map<String, String> routing = _routingService.findStudyRouting(_studyInstanceUid);
                if (routing == null) {
                    getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Couldn't find a routing for the study instance UID: " + _studyInstanceUid);
                    return;
                }
                if (!Permissions.can(user,"xnat:mrSessionData/project", routing.get(StudyRoutingService.PROJECT), SecurityManager.EDIT)) {
                    getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "You do not have sufficient privileges to delete study routings for this project.");
                    return;
                }
                _log.info("Closing study instance UID {} assignment, per user {}", _studyInstanceUid, user.getLogin());
                _routingService.close(_studyInstanceUid);
            } catch (Exception e) {
                _log.error("An error occurred checking user permissions", e);
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e, "An error occurred checking user permissions");
            }
        }
    }

    private String getRoutingAttribute(final Map<String, String> routing, final String attribute) {
        return routing.containsKey(attribute) ? routing.get(attribute) : "";
    }
}
