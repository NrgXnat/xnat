/*
 * web: org.nrg.xnat.restlet.services.Archiver
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.services;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.status.StatusListenerI;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.archive.QueueBasedImageCommit;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcSession;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionDataTriple;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.restlet.services.prearchive.BatchPrearchiveActionsA;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static lombok.AccessLevel.PROTECTED;
import static org.nrg.xft.event.EventUtils.EVENT_REASON;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.APPEND;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.DELETE;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus.ARCHIVING;
import static org.nrg.xnat.restlet.util.RequestUtil.TRUE;
import static org.restlet.data.MediaType.TEXT_URI_LIST;
import static org.restlet.data.Status.*;

@Getter(PROTECTED)
@Setter(PROTECTED)
@Accessors(prefix = "_")
@Slf4j
public class Archiver extends BatchPrearchiveActionsA {
    private static final String REDIRECT2       = "redirect";
    private static final String FOLDER          = "folder";
    private static final String OVERWRITE       = "overwrite";
    private static final String OVERWRITE_FILES = "overwrite_files";
    private static final String PROJECT         = "project";
    private static final String CRLF            = "\r\n";
    private static final String DEST            = "dest";

    public Archiver(final Context context, final Request request, final Response response) {
        super(context, request, response);
    }

    @Override
    public void handleParam(final String key, final Object value) {
        switch (key) {
            case PROJECT:
                getAdditionalValues().put("project", value);
                break;
            case PrearcUtils.PREARC_TIMESTAMP:
                setTimestamp((String) value);
                break;
            case PrearcUtils.PREARC_SESSION_FOLDER:
                getSessionFolder().add((String) value);
                break;
            case OVERWRITE_FILES:
                _overwriteFiles = StringUtils.equalsIgnoreCase((String) value, TRUE);
                break;
            case OVERWRITE:
                _overwriteV = (String) value;
                break;
            case DEST:
                setDestination((String) value);
                break;
            case REDIRECT2:
                _redirect = StringUtils.equalsIgnoreCase((String) value, TRUE);
                break;
            case SRC:
                super.handleParam(key, value);
                break;
            default:
                getAdditionalValues().put(key, value);
                break;
        }
    }

    protected void initialize() {
        switch (StringUtils.lowerCase(StringUtils.defaultIfBlank(_overwriteV, ""))) {
            case APPEND:
                _allowDataDeletion = false;
                _overwrite = true;
                break;

            case DELETE:
                _allowDataDeletion = true;
                _overwrite = true;
                break;

            default:
                _allowDataDeletion = false;
                _overwrite = false;
        }
    }

    protected void finishSingleSessionArchive(final PrearcSession session) throws Exception {
        if (!setSessionArchiveStatus(session)) {
            return;
        }
        session.setArchiveReason(false);
        try (final QueueBasedImageCommit uploader = new QueueBasedImageCommit(null, getUser(), session, UriParserUtils.parseURI(getDestination()), _overwrite, true)) {
            final String uri = uploader.submitSync();
            if (StringUtils.isBlank(uri)) {
                getResponse().setStatus(SERVER_ERROR_INTERNAL, "The session " + session.toString() + " did not return a valid data URI.");
                return;
            }
            if (_redirect || session.isAutoArchive()) {
                getResponse().redirectSeeOther(XDAT.getSiteUrl() + uri);
            } else {
                getResponse().setEntity(uri + CRLF, TEXT_URI_LIST);
                getResponse().setStatus(SUCCESS_OK);
            }
        } catch (TimeoutException e) {
            getResponse().setStatus(SERVER_ERROR_INTERNAL, "The session " + session.toString() + " did not complete within the configured timeout interval.");
        }
    }

    protected void finishNonSingleSessionUpload(final List<PrearcSession> sessions) throws Exception {
        if (!PrearcUtils.canModify(getUser(), sessions.get(0).getProject())) {
            getResponse().setStatus(CLIENT_ERROR_FORBIDDEN, "Invalid permissions for new project.");
            return;
        }

        final List<PrearcSession> prearcSessions = new ArrayList<>();
        for (final PrearcSession prearcSession : sessions) {
            if (setSessionArchiveStatus(prearcSession)) {
                prearcSessions.add(prearcSession);
                if (!prearcSession.getAdditionalValues().containsKey(EVENT_REASON)) {
                    prearcSession.getAdditionalValues().put(EVENT_REASON, "Batch archive");
                }
            }
        }

        final Map<SessionDataTriple, Boolean> archivedSessions = PrearcDatabase.archive(prearcSessions, _allowDataDeletion, _overwrite, _overwriteFiles, getUser(), Collections.<StatusListenerI>emptySet());
        getResponse().setEntity(updatedStatusRepresentation(archivedSessions.keySet(), overrideVariant(getPreferredVariant())));
    }

    private boolean setSessionArchiveStatus(final PrearcSession session) throws Exception {
        if (!PrearcDatabase.setStatus(session.getFolderName(), session.getTimestamp(), session.getProject(), ARCHIVING)) {
            getResponse().setStatus(CLIENT_ERROR_FORBIDDEN, "Operation already in progress on this prearchive entry.");
            return false;
        }
        return true;
    }

    private String  _overwriteV = null;
    private boolean _overwriteFiles;
    private boolean _allowDataDeletion;
    private boolean _overwrite;
    private boolean _redirect;
}
