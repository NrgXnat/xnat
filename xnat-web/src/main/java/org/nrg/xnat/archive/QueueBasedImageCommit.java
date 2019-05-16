/*
 * web: org.nrg.xnat.archive.QueueBasedImageCommit
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.nrg.framework.status.StatusListenerI;
import org.nrg.framework.status.StatusMessage;
import org.nrg.framework.status.StatusProducer;
import org.nrg.xdat.XDAT;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.event.listeners.ArchiveEventListener;
import org.nrg.xnat.event.listeners.ArchiveOperationListener;
import org.nrg.xnat.helpers.prearchive.PrearcSession;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;
import org.nrg.xnat.status.ListenerUtils;
import org.nrg.xnat.utils.XnatHttpUtils;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PUBLIC;
import static org.nrg.framework.status.StatusMessage.Status.PROCESSING;
import static org.nrg.xnat.archive.Operation.Archive;
import static org.nrg.xnat.helpers.prearchive.PrearcDatabase.removePrearcVariables;
import static org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest.*;

/**
 * @author Timothy R Olsen
 */
@Getter(PRIVATE)
@Accessors(prefix = "_")
@Slf4j
public class QueueBasedImageCommit extends StatusProducer implements Callable<String>, ArchiveOperationListener {
    public QueueBasedImageCommit(final Object control, final UserI user, final PrearcSession session, final URIManager.DataURIA destination, final boolean overrideExceptions, final boolean allowSessionMerge) throws Exception {
        this(control, user, session, destination, overrideExceptions, allowSessionMerge, false);
    }

    public QueueBasedImageCommit(final Object control, final UserI user, final PrearcSession session, final URIManager.DataURIA destination, final boolean overrideExceptions, final boolean allowSessionMerge, final boolean inline) throws Exception {
        super(control);

        _user = user;
        _prearcSession = session;
        _sessionDir = session.getSessionDir();
        _destination = destination;
        _sessionData = session.getSessionData();
        _project = session.getSessionData().getProject();
        _timestamp = session.getSessionData().getTimestamp();
        _session = session.getSessionData().getName();
        _inline = inline;
        _overrideExceptions = overrideExceptions;
        _allowSessionMerge = allowSessionMerge;
        _parameters = new HashMap<>(session.getAdditionalValues());
        _parameters.put(PARAM_ALLOW_SESSION_MERGE, allowSessionMerge);
        _parameters.put(PARAM_OVERRIDE_EXCEPTIONS, overrideExceptions);
        if (destination != null) {
            _parameters.put(PARAM_DESTINATION, destination);
        }
        _archiveEventListener = XDAT.getContextService().getBean(ArchiveEventListener.class);
        _archiveEventListener.addStatusListener(this);

        log.debug("Created QueueBasedImageCommit object to complete archiving session {} to {}, status producer is: {}", session, destination, control);
    }

    @Override
    public String call() throws Exception {
        if (isInline()) {
            //This is being done as part of a parent transaction and should not manage prearc cache state.
            log.debug("Completing inline archive operation to auto-archive session {} to {}", getPrearcSession(), getDestination());
            return ListenerUtils.addListeners(this, new PrearcSessionArchiver(getPrearcSession(),
                                                                              getUser(),
                                                                              removePrearcVariables(_prearcSession.getAdditionalValues()),
                                                                              isOverrideExceptions(),
                                                                              isAllowSessionMerge(),
                                                                              false,
                                                                              _prearcSession.isOverwriteFiles())).call();
        }

        try {
            log.debug("Queuing archive operation to auto-archive session {} to {}", getPrearcSession(), getDestination());
            final PrearchiveOperationRequest request = new PrearchiveOperationRequest(getUser(), Archive, getSessionData(), getSessionDir(), getParameters());
            XDAT.sendJmsRequest(request);
            log.trace("{}: Dispatched JMS request: {}", getArchiveOperationId(), request);

            final int       timeout   = XDAT.getIntSiteConfigurationProperty("sessionArchiveTimeoutInterval", 600);
            final StopWatch stopWatch = StopWatch.createStarted();
            while (_message == null || _message.getStatus() == PROCESSING) {
                if (stopWatch.getTime(TimeUnit.SECONDS) > timeout) {
                    log.warn("Checked for message with final status but didn't find it, however this operation has exceeded the configured timeout of {} seconds for image commit operations, bailing on it.", timeout);
                    return null;
                }
                log.debug("Checked for message with final status but didn't find it, sleeping for a bit...");
                Thread.sleep(500);
            }

            log.debug("Found a message with status {}: {}", _message.getStatus(), _message.getMessage());
            return StringUtils.defaultIfBlank(_message.getMessage(), _message.getStatus().toString());
        } finally {
            log.debug("Removing the QueueBasedImageCommit instance {} from the archive event listener: {}", getArchiveOperationId(), this);
            _archiveEventListener.removeStatusListener(this);
        }
    }

    @Override
    public void notify(final StatusMessage message) {
        _message = message;
        for (final StatusListenerI listener : getListeners()) {
            listener.notify(message);
        }
    }

    @Override
    public List<Operation> getOperations() {
        return Collections.singletonList(Archive);
    }

    @Override
    public String toString() {
        return getArchiveOperationId() + ":" + (_message != null ? _message.getStatus() + ":" + _message.getMessage() : "<null>");
    }

    public Future<String> submit() {
        return getExecutor().submit(this);
    }

    public String submitSync() throws TimeoutException {
        // try (final QueueBasedImageCommit uploader = new QueueBasedImageCommit(null, getUser(), session, UriParserUtils.parseURI(getDestination()), false, true)) {
        final Future<String> future  = submit();
        final int            timeout = XDAT.getIntSiteConfigurationProperty("sessionArchiveTimeoutInterval", 600);
        try {
            final String result = future.get(timeout, TimeUnit.SECONDS);
            final String uri    = wrapPartialDataURI(result);
            if (StringUtils.isBlank(uri)) {
                throw new TimeoutException("The session " + getPrearcSession().toString() + " did not return a valid data URI within the timeout interval of " + timeout + " seconds.");
            }
            return uri;
        } catch (InterruptedException | ExecutionException e) {
            log.error("An error occurred while trying to archive the session " + getPrearcSession().toString());
            return null;
        }
    }

    private String wrapPartialDataURI(final String uri) {
        return StringUtils.prependIfMissing(uri, "/data");
    }

    private static ExecutorService getExecutor() {
        if (_executorService == null) {
            _executorService = XDAT.getContextService().getBean("executorService", ExecutorService.class);
        }
        return _executorService;
    }

    private static ExecutorService _executorService = null;

    @Getter(PUBLIC)
    private final String _project;
    @Getter(PUBLIC)
    private final String _timestamp;
    @Getter(PUBLIC)
    private final String _session;
    @Getter(value = PUBLIC, lazy = true)
    private final @NonNull String _archiveOperationId = XnatHttpUtils.buildArchiveEventId(_project, _timestamp, _session);

    private final UserI                _user;
    private final PrearcSession        _prearcSession;
    private final URIManager.DataURIA  _destination;
    private final Map<String, Object>  _parameters;
    private final ArchiveEventListener _archiveEventListener;
    private final boolean              _inline;
    private final boolean              _overrideExceptions;
    private final boolean              _allowSessionMerge;
    private       SessionData          _sessionData;
    private       File                 _sessionDir;
    private       StatusMessage        _message;
}
