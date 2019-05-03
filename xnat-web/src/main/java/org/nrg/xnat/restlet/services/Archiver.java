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
import org.nrg.xft.event.EventUtils;
import org.nrg.xnat.archive.QueueBasedImageCommit;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcSession;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionDataTriple;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.restlet.services.prearchive.BatchPrearchiveActionsA;
import org.nrg.xnat.restlet.util.RequestUtil;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static lombok.AccessLevel.PROTECTED;

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
                _overwriteFiles = StringUtils.equalsIgnoreCase((String) value, RequestUtil.TRUE);
                break;
            case OVERWRITE:
                _overwriteV = (String) value;
                break;
            case DEST:
                setDestination((String) value);
                break;
            case REDIRECT2:
                _redirect = StringUtils.equalsIgnoreCase((String) value, RequestUtil.TRUE);
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
        if (StringUtils.equalsIgnoreCase(_overwriteV, PrearcUtils.APPEND)) {
            _allowDataDeletion = false;
            _overwrite = true;
        } else if (StringUtils.equalsIgnoreCase(_overwriteV, PrearcUtils.DELETE)) {
            _allowDataDeletion = true;
            _overwrite = true;
        } else {
            _allowDataDeletion = false;
            _overwrite = false;
        }
    }

    protected void finishSingleSessionArchive(final PrearcSession session) throws Exception {
        session.setArchiveReason(false);
        try (final QueueBasedImageCommit uploader = new QueueBasedImageCommit(null, getUser(), session, UriParserUtils.parseURI(getDestination()), false, true)) {
            final String uri = uploader.submitSync();
            if (StringUtils.isBlank(uri)) {
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "The session " + session.toString() + " did not return a valid data URI.");
                return;
            }
            if (_redirect || session.isAutoArchive()) {
                getResponse().redirectSeeOther(getContextPath() + uri);
            } else {
                getResponse().setEntity(uri + CRLF, MediaType.TEXT_URI_LIST);
                getResponse().setStatus(Status.SUCCESS_OK);
            }
        } catch (TimeoutException e) {
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "The session " + session.toString() + " did not complete within the configured timeout interval.");
        }
    }

    protected void finishNonSingleSessionUpload(final List<PrearcSession> sessions) throws Exception {
        if (!PrearcUtils.canModify(getUser(), sessions.get(0).getProject())) {
            getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Invalid permissions for new project.");
            return;
        }

        for (final PrearcSession prearcSession : sessions) {
            if (!prearcSession.getAdditionalValues().containsKey(EventUtils.EVENT_REASON)) {
                prearcSession.getAdditionalValues().put(EventUtils.EVENT_REASON, "Batch archive");
            }
        }

        final Map<SessionDataTriple, Boolean> archivedSessions = PrearcDatabase.archive(sessions, _allowDataDeletion, _overwrite, _overwriteFiles, getUser(), Collections.<StatusListenerI>emptySet());
        getResponse().setEntity(updatedStatusRepresentation(archivedSessions.keySet(), overrideVariant(getPreferredVariant())));
    }

    private String  _overwriteV = null;
    private boolean _overwriteFiles;
    private boolean _allowDataDeletion;
    private boolean _overwrite;
    private boolean _redirect;
}
