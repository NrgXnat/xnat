/*
 * web: org.nrg.xnat.helpers.prearchive.handlers.PrearchiveRebuildHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive.handlers;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.nrg.action.ServerException;
import org.nrg.framework.services.NrgEventServiceI;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcSession;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;

import static lombok.AccessLevel.PRIVATE;
import static org.nrg.xnat.archive.Operation.Archive;

@Handles(Archive)
@Getter(PRIVATE)
@Accessors(prefix = "_")
@Slf4j
public class PrearchiveArchiveHandler extends AbstractPrearchiveOperationHandler {
    public PrearchiveArchiveHandler(final PrearchiveOperationRequest request, final NrgEventServiceI eventService, final XnatUserProvider userProvider) throws Exception {
        super(request, eventService, userProvider);
        _session = new PrearcSession(request, getUser());
        _destination = UriParserUtils.parseURI((String) getParameters().get(PrearchiveOperationRequest.PARAM_DESTINATION));
        _overrideExceptions = isBooleanParameter(PrearchiveOperationRequest.PARAM_OVERRIDE_EXCEPTIONS);
        _allowSessionMerge = isBooleanParameter(PrearchiveOperationRequest.PARAM_ALLOW_SESSION_MERGE);
    }

    @Override
    public void execute() throws Exception {
        final String result = commitSessionToArchive();
        log.info("Completed uploading the session at {}/{}/{} with result {}", _session.getProject(), _session.getTimestamp(), _session.getFolderName(), result);
        completed(result);
    }

    private String commitSessionToArchive() throws Exception {
        if (PrearcDatabase.setStatus(getSession().getFolderName(), getSession().getTimestamp(), getSession().getProject(), PrearcUtils.PrearcStatus.ARCHIVING)) {
            final boolean        override;
            final boolean        append;
            if (getSession().getSessionData() != null && getSession().getSessionData().getAutoArchive() != null) {
                switch (getSession().getSessionData().getAutoArchive()) {
                    case AutoArchive:
                    case AutoArchiveOverwrite:
                        override = false;
                        append = true;
                        break;

                    default:
                        override = false;
                        append = false;
                        break;
                }
            } else {
                override = isOverrideExceptions();
                append = isAllowSessionMerge();
            }
            return PrearcDatabase.archive(getSession(), override, append, getSession().isOverwriteFiles(), getUser(), null);
        } else {
            throw new ServerException("Unable to lock session for archiving.");
        }
    }

    private boolean isBooleanParameter(final String parameterName) {
        if (!getParameters().containsKey(parameterName)) {
            return false;
        }
        final Object value = getParameters().get(parameterName);
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }

    private final PrearcSession       _session;
    private final URIManager.DataURIA _destination;
    private final boolean             _overrideExceptions;
    private final boolean             _allowSessionMerge;
}
