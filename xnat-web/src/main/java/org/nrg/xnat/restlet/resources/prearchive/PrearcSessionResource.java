/*
 * web: org.nrg.xnat.restlet.resources.prearchive.PrearcSessionResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources.prearchive;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.TurbineException;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.framework.constants.PrearchiveCode;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.QueueBasedImageCommit;
import org.nrg.xnat.helpers.prearchive.*;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase.SyncFailedException;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;
import org.nrg.xnat.restlet.representations.StandardTurbineScreen;
import org.nrg.xnat.restlet.representations.ZipRepresentation;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.FileRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.*;

import static org.restlet.data.MediaType.*;
import static org.restlet.data.Status.*;

@Slf4j
public final class PrearcSessionResource extends SecureResource {
    /**
     * Initializes the restlet.
     *
     * @param context  The restlet context.
     * @param request  The restlet request.
     * @param response The restlet response.
     */
    public PrearcSessionResource(final Context context, final Request request, final Response response) {
        super(context, request, response);

        // Project, timestamp, session are explicit in the request
        final String projectId = (String) getParameter(request, PROJECT_ATTR);
        project = projectId.equalsIgnoreCase(PrearcUtils.COMMON) ? null : projectId;
        timestamp = (String) getParameter(request, SESSION_TIMESTAMP);
        session = (String) getParameter(request, SESSION_LABEL);
        getVariants().addAll(MEDIA_TYPES);
    }

    @Override
    public final boolean allowPost() { return true; }

    @Override
    public final boolean allowDelete() { return true; }

    @SuppressWarnings("RedundantThrows")
    @Override
    public void handleParam(final String key, final Object value) throws ClientException {
        if ("action".equals(key)) {
            action = (String) value;
        } else {
            params.put(key, value);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.restlet.resource.Resource#represent(org.restlet.resource.Variant)
     */
    @SuppressWarnings("serial")
    @Override
    public Representation represent(final Variant variant){
        final File sessionDir;
        final UserI user = getUser();
        try {
            sessionDir = PrearcUtils.getPrearcSessionDir(user, project, timestamp, session, false);
        } catch (InvalidPermissionException e) {
            getResponse().setStatus(CLIENT_ERROR_FORBIDDEN, e.getMessage());
            return null;
        } catch (Exception e) {
            getResponse().setStatus(SERVER_ERROR_INTERNAL, e.getMessage());
            return null;
        }

        final MediaType mediaType = overrideVariant(variant);

        //add GET support for log files
        if (StringUtils.isNotEmpty(filepath)) {
            if (filepath.startsWith("logs/") && filepath.length() > 5) {
                final String logId    = filepath.substring(5);
                final String contents = logId.equals("last") ? PrearcUtils.getLastLog(project, timestamp, session) : PrearcUtils.getLog(project, timestamp, session, logId);
                return new StringRepresentation(contents, mediaType);
            }
            if (filepath.equals("logs")) {
                final XFTTable tb=new XFTTable();
                if(getQueryVariable("template")==null || getQueryVariable("template").equals("details")){
                    tb.initTable(new String[]{"id","date","entry"});

                    try {
                        final Collection<File> logs = PrearcUtils.getLogs(project, timestamp, session);
                        if (logs != null) {
                            for(final File log : logs){
                                final Date timestamp=new Date(log.lastModified());
                                final String id=log.getName().substring(0,log.getName().indexOf(".log"));
                                tb.insertRow(new Object[]{id, timestamp, FileUtils.readFileToString(log, Charset.defaultCharset())});
                            }
                        }
                        tb.sort("date","ASC");
                        tb.resetRowCursor();
                    } catch (IOException e) {
                        getResponse().setStatus(SERVER_ERROR_INTERNAL, e.getMessage());
                        return null;
                    }
                }else{
                    tb.initTable(new String[]{"id"});

                    final Collection<String> logIds = PrearcUtils.getLogIds(project, timestamp, session);
                    if (logIds != null) {
                        for(final String id: logIds){
                            tb.rows().add(new Object[]{id});
                        }
                    }
                }
                return representTable(tb, mediaType, new Hashtable<String, Object>());
            }
        }

        final String screen = getQueryVariable("screen");
        if (TEXT_HTML.equals(mediaType) || StringUtils.isNotEmpty(screen)) {
            // Return the session XML, if it exists
            if (screen.equals("XDATScreen_uploaded_xnat_imageSessionData.vm")){
                if(project==null){
                    getResponse().setStatus(CLIENT_ERROR_FORBIDDEN, "Projects in the unassigned folder cannot be archived.");
                    return null;
                }
                getResponse().redirectSeeOther(getContextPath() + String.format("/app/action/LoadImageData/project/%s/timestamp/%s/folder/%s/popup/%s", project, timestamp, session, StringUtils.equalsIgnoreCase(getQueryVariable("popup"), "true") ? "true" : "false"));
                return null;
            }

            try {
                final Map<String,Object> params = new HashMap<>();
                for(final String key: getQueryVariableKeys()){
                    params.put(key, getQueryVariable(key));
                }
                params.put("project",project);
                params.put("timestamp",timestamp);
                params.put("folder",session);
                return new StandardTurbineScreen(TEXT_HTML, getRequest(), user, StringUtils.defaultIfBlank(screen, "XDATScreen_brief_xnat_imageSessionData.vm"), params);
            } catch (TurbineException e) {
                getResponse().setStatus(SERVER_ERROR_INTERNAL, e.getMessage());
                return null;
            }

        } else if (TEXT_XML.equals(mediaType)) {
            // Return the session XML, if it exists
            final File sessionXML = new File(sessionDir.getPath() + ".xml");
            if (!sessionXML.isFile()) {
                getResponse().setStatus(CLIENT_ERROR_NOT_FOUND, "The named session exists, but its XNAT session document is not available. The session is likely invalid or incomplete.");
                return null;
            }
            return new FileRepresentation(sessionXML, variant.getMediaType(), 0);
        } else if (APPLICATION_JSON.equals(mediaType)) {
            final List<SessionDataTriple> triples = new ArrayList<>();
            triples.add(new SessionDataTriple(sessionDir.getName(), timestamp, project));
            XFTTable table = null;
            try {
                table = PrearcUtils.convertArrayLtoTable(PrearcDatabase.buildRows(triples));
            } catch (Exception e) {
                getResponse().setStatus(SERVER_ERROR_INTERNAL, e.getMessage());
            }
            return representTable(table, APPLICATION_JSON, new Hashtable<String,Object>());
        }
        else if (APPLICATION_GNU_ZIP.equals(mediaType) || APPLICATION_ZIP.equals(mediaType)) {
            final ZipRepresentation zip;
            try{
                zip = new ZipRepresentation(mediaType, sessionDir.getName(),identifyCompression(null));
            } catch (ActionException e) {
                log.error("", e);
                setResponseStatus(e);
                return null;
            }
            zip.addFolder(sessionDir.getName(), sessionDir);
            return zip;
        } else {
            getResponse().setStatus(CLIENT_ERROR_BAD_REQUEST, "Requested type " + mediaType + " is not supported");
            return null;
        }
    }

    @Override
    public void handlePost(){
        try {
            loadBodyVariables();
            loadQueryVariables();
            
            final Representation entity=getRequest().getEntity();
            if(entity!=null){
                final String json = entity.getText();
                if (!Strings.isNullOrEmpty(json)) {
                    loadParams(json);
                }
            }
        } catch (ClientException e1) {
            log.error("", e1);
            getResponse().setStatus(e1.getStatus(), e1);
        } catch (IOException e) {
            log.error("", e);
            getResponse().setStatus(SERVER_ERROR_INTERNAL, e.getMessage());
        }

        final File sessionDir;
        final UserI user = getUser();
        try {
            sessionDir = PrearcUtils.getPrearcSessionDir(user, project, timestamp, session, true);
        } catch (InvalidPermissionException e) {
            log.error("", e);
            getResponse().setStatus(CLIENT_ERROR_FORBIDDEN, e.getMessage());
            return;
        } catch (Exception e) {
            log.error("", e);
            getResponse().setStatus(SERVER_ERROR_INTERNAL, e.getMessage());
            return;
        }

        switch (action) {
            case POST_ACTION_BUILD:
                postActionBuild(sessionDir, user);
                break;

            case POST_ACTION_RESET:
                postActionReset(user);
                break;

            case POST_ACTION_MOVE:
                postActionMove(user);
                break;

            case POST_ACTION_COMMIT:
                postActionCommit(sessionDir, user);
                break;

            case POST_ACTION_SET:
                postActionSet();
                break;

            default:
                final String message;
                if (StringUtils.equals("uninitialized", action)) {
                    message = "No action parameter was specified for the request by user " + getUser().getUsername();
                } else {
                    message = "User " + getUser().getUsername() + " requested an unsupported action on prearchive session: " + action;
                }
                getResponse().setStatus(CLIENT_ERROR_BAD_REQUEST, message);
        }
    }

    @Override
    public void handleDelete() {
    	if(StringUtils.isNotEmpty(filepath)){
    		getResponse().setStatus(CLIENT_ERROR_BAD_REQUEST, "");
    		return;
    	}

        final UserI user = getUser();
        try {
            //checks if the user can access this session
            PrearcUtils.getPrearcSessionDir(user, project, timestamp, session, false);
        } catch (InvalidPermissionException e) {
            log.error("", e);
            getResponse().setStatus(CLIENT_ERROR_FORBIDDEN, e.getMessage());
            return;
        } catch (Exception e) {
            log.error("", e);
            getResponse().setStatus(SERVER_ERROR_INTERNAL, e.getMessage());
            return;
        }

        try {
            if(!PrearcUtils.canModify(user, project)){
                getResponse().setStatus(CLIENT_ERROR_FORBIDDEN, "Unable to modify session data for destination project.");
                return;
            }

            PrearcDatabase.deleteSession(session, timestamp, project);
        } catch (SessionException e) {
            log.warn("An error occurred trying to access the prearchive session {}/{}/{}: [{}] {}", project, timestamp, session, e.getError(), e.getMessage());
            getResponse().setStatus(getStatusForSessionException(e.getError()), e.getMessage());
        } catch (Exception e) {
            log.error("", e);
            getResponse().setStatus(SERVER_ERROR_INTERNAL, e.getMessage());
        }
    }

    private void postActionBuild(final File sessionDir, final UserI user) {
        try {
            if (PrearcDatabase.setStatus(session, timestamp, project, PrearcStatus.BUILDING)) {
                PrearcDatabase.buildSession(sessionDir, session, timestamp, project, (String) params.get(VISIT), (String) params.get(PROTOCOL), (String) params.get(TIMEZONE), (String) params.get(SOURCE));
                PrearcUtils.resetStatus(user, project, timestamp, session, true);
                returnString(wrapPartialDataURI(PrearcUtils.buildURI(project,timestamp,session)), TEXT_URI_LIST, SUCCESS_OK);
            } else {
                getResponse().setStatus(CLIENT_ERROR_CONFLICT, "session document locked");
            }
        } catch (InvalidPermissionException e) {
            log.error("", e);
            PrearcUtils.log(project, timestamp, session, e);
            getResponse().setStatus(CLIENT_ERROR_FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            log.error("", e);
            PrearcUtils.log(project, timestamp, session, e);
            getResponse().setStatus(SERVER_ERROR_INTERNAL, e);
        }
    }

    private void postActionReset(final UserI user) {
        try {
            final String tag= getQueryVariable("tag");
            PrearcUtils.resetStatus(user, project, timestamp, session, tag, true);
            returnString(wrapPartialDataURI(PrearcUtils.buildURI(project,timestamp,session)), TEXT_URI_LIST, SUCCESS_OK);
        } catch (InvalidPermissionException e) {
            log.error("", e);
            PrearcUtils.log(project, timestamp, session, e);
            getResponse().setStatus(CLIENT_ERROR_FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            log.error("", e);
            PrearcUtils.log(project, timestamp, session, e);
            getResponse().setStatus(SERVER_ERROR_INTERNAL, e);
        }
    }

    private void postActionMove(final UserI user) {
        String newProj=getQueryVariable("newProject");

        // if(StringUtils.isNotEmpty(newProj)){
        //TODO: convert ALIAS to project ID (if necessary)
        // }

        try {
            if(!PrearcUtils.canModify(user, newProj)){
                getResponse().setStatus(CLIENT_ERROR_FORBIDDEN, "Unable to modify session data for destination project.");
                return;
            }
            if(PrearcDatabase.setStatus(session, timestamp, project, PrearcStatus.MOVING)){
                PrearcDatabase.moveToProject(session, timestamp, (project==null)?"Unassigned":project, newProj);
                returnString(wrapPartialDataURI(PrearcUtils.buildURI(newProj,timestamp,session)), TEXT_URI_LIST, REDIRECTION_PERMANENT);
            }
        } catch (SyncFailedException e) {
            log.error("An error occurred trying to sync the prearchive session {}/{}/{}", project, timestamp, session, e);
            PrearcUtils.log(project, timestamp, session, e);
            getResponse().setStatus(SERVER_ERROR_INTERNAL, e);
        } catch (SQLException e) {
            log.error("An error occurred trying to read from or write to the database when processing the prearchive session {}/{}/{}", project, timestamp, session, e);
            PrearcUtils.log(project, timestamp, session, e);
            getResponse().setStatus(SERVER_ERROR_INTERNAL, e);
        } catch (SessionException e) {
            log.warn("An error occurred trying to access the prearchive session {}/{}/{}: [{}] {}", project, timestamp, session, e.getError(), e.getMessage());
            PrearcUtils.log(project, timestamp, session, e);
            getResponse().setStatus(getStatusForSessionException(e.getError()), e);
        } catch (Exception e) {
            log.error("", e);
            PrearcUtils.log(project, timestamp, session, e);
            getResponse().setStatus(SERVER_ERROR_INTERNAL, e);
        }
    }

    private void postActionCommit(final File sessionDir, final UserI user) {
        try {
            if (PrearcDatabase.setStatus(session, timestamp, project, PrearcStatus.BUILDING)) {
                final SessionData sessionData = PrearcDatabase.getSession(session, timestamp, project);
                if (null == sessionData.getAutoArchive() && !Strings.isNullOrEmpty(project)) {
                    PrearcDatabase.setAutoArchive(session, timestamp, project, PrearchiveCode.code(XnatProjectdata.getProjectByIDorAlias(project, user, false).getArcSpecification().getPrearchiveCode()));
                }
                PrearcDatabase.buildSession(sessionDir, session, timestamp, project, (String) params.get(VISIT), (String) params.get(PROTOCOL), (String) params.get(TIMEZONE), (String) params.get(SOURCE));
                PrearcUtils.resetStatus(user, project, timestamp, session, true);

                final PrearcSession prearcSession = new PrearcSession(project, timestamp, session, params, user);
                if (prearcSession.isAutoArchive()) {
                    try (final QueueBasedImageCommit uploader = new QueueBasedImageCommit(null, user, prearcSession, null, false, true)) {
                        final String result = uploader.submitSync();
                        final String uri    = wrapPartialDataURI(result);
                        if (StringUtils.isBlank(uri)) {
                            getResponse().setStatus(SERVER_ERROR_INTERNAL, "The session " + prearcSession.toString() + " did not return a valid data URI.");
                        } else {
                            returnString(uri, REDIRECTION_PERMANENT);
                        }
                    }
                } else {
                    prearcSession.populateAdditionalFields(user);
                    returnString(wrapPartialDataURI(prearcSession.getUrl()), MediaType.TEXT_URI_LIST, Status.SUCCESS_OK);
                }
            } else {
                getResponse().setStatus(CLIENT_ERROR_CONFLICT, "session document locked");
            }
        } catch (SessionException e) {
            log.warn("An error occurred trying to access the prearchive session {}/{}/{}: [{}] {}", project, timestamp, session, e.getError(), e.getMessage());
            getResponse().setStatus(getStatusForSessionException(e.getError()));
        } catch (ActionException e) {
            log.error("", e);
            setResponseStatus(e);
        } catch (InvalidPermissionException e) {
            log.error("", e);
            getResponse().setStatus(CLIENT_ERROR_FORBIDDEN, e.getMessage());
        } catch (SyncFailedException e) {
            log.error("", e);
            if(e.getCause()!=null && e.getCause() instanceof ActionException){
                setResponseStatus((ActionException)e.getCause());
            }else{
                getResponse().setStatus(SERVER_ERROR_INTERNAL, e);
            }
        } catch (Exception e) {
            log.error("", e);
            getResponse().setStatus(SERVER_ERROR_INTERNAL, e);
        }
    }

    private void postActionSet() {
        try {
            PrearcDatabase.setStatus(session, timestamp, project, (String) params.get("status"));
        }
        catch (Exception e) {
            getResponse().setStatus(CLIENT_ERROR_BAD_REQUEST, e.getMessage());
        }
    }

    private static Status getStatusForSessionException(final SessionException.Error error) {
        switch (error) {
            case DoesntExist:
                return CLIENT_ERROR_NOT_FOUND;

            case AlreadyExists:
                return CLIENT_ERROR_CONFLICT;

            case InvalidStatus:
            case InvalidSession:
            case NoProjectSpecified:
                return CLIENT_ERROR_BAD_REQUEST;

            default:
                return SERVER_ERROR_INTERNAL;
        }
    }

    private static final List<Variant> MEDIA_TYPES = Arrays.asList(new Variant(TEXT_XML), new Variant(APPLICATION_ZIP), new Variant(APPLICATION_GNU_ZIP), new Variant(TEXT_HTML));

    private static final String PROJECT_ATTR       = "PROJECT_ID";
    private static final String SESSION_TIMESTAMP  = "SESSION_TIMESTAMP";
    private static final String SESSION_LABEL      = "SESSION_LABEL";
    private static final String VISIT              = "VISIT";
    private static final String PROTOCOL           = "PROTOCOL";
    private static final String TIMEZONE           = "TIMEZONE";
    private static final String SOURCE             = "SOURCE";
    private static final String POST_ACTION_SET    = "set-status";
    private static final String POST_ACTION_RESET  = "reset-status";
    private static final String POST_ACTION_BUILD  = "build";
    private static final String POST_ACTION_MOVE   = "move";
    private static final String POST_ACTION_COMMIT = "commit";

    private final String project;
    private final String timestamp;
    private final String session;

    private final Map<String, Object> params= new HashMap<>();

    private String action = "uninitialized";
}
