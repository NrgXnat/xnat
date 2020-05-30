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
import org.nrg.action.ServerException;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.status.StatusListenerI;
import org.nrg.framework.status.StatusMessage;
import org.nrg.xdat.XDAT;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.event.archive.ArchiveStatusProducer;
import org.nrg.xnat.event.listeners.ArchiveEventListener;
import org.nrg.xnat.event.listeners.ArchiveOperationListener;
import org.nrg.xnat.helpers.prearchive.PrearcSession;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;
import org.nrg.xnat.status.ListenerUtils;
import org.nrg.xnat.tracking.entities.EventTrackingDataPojo;
import org.nrg.xnat.tracking.services.EventTrackingDataService;
import org.nrg.xnat.utils.XnatHttpUtils;
import org.restlet.data.Status;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PUBLIC;
import static org.nrg.framework.status.StatusMessage.Status.*;
import static org.nrg.xnat.archive.Operation.Archive;
import static org.nrg.xnat.helpers.prearchive.PrearcDatabase.removePrearcVariables;
import static org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest.*;

/**
 * @author Timothy R Olsen
 */
@Getter(PRIVATE)
@Accessors(prefix = "_")
@Slf4j
public class QueueBasedImageCommit extends ArchiveStatusProducer implements Callable<String>, ArchiveOperationListener {
    public QueueBasedImageCommit(final UserI user, final PrearcSession session, final URIManager.DataURIA destination, final boolean overrideExceptions, final boolean allowSessionMerge, final String listenerId) throws Exception {
        this(user, session, destination, overrideExceptions, allowSessionMerge, listenerId, false);
    }

    public QueueBasedImageCommit(final UserI user, final PrearcSession session, final URIManager.DataURIA destination, final boolean overrideExceptions, final boolean allowSessionMerge, final String listenerId, final boolean inline) throws Exception {
        this(user, session, destination,
                session.getSessionData().getProject(), session.getSessionData().getTimestamp(),
                session.getSessionData().getName(), overrideExceptions, allowSessionMerge, listenerId, inline);
    }

    public QueueBasedImageCommit(final UserI user, final PrearcSession session,
                                 final URIManager.DataURIA destination, final String project,
                                 final String timestamp, final String sessionLabel, final boolean overrideExceptions,
                                 final boolean allowSessionMerge, final String listenerId, final boolean inline) throws Exception {

        super(StringUtils.defaultIfBlank(listenerId,
                XnatHttpUtils.buildArchiveEventId(project, timestamp, sessionLabel)));

        _user = user;
        _prearcSession = session;
        _sessionDir = session.getSessionDir();
        _destination = destination;
        _sessionData = session.getSessionData();
        _project = project;
        _timestamp = timestamp;
        _session = sessionLabel;
        _inline = inline;
        _overrideExceptions = overrideExceptions;
        _allowSessionMerge = allowSessionMerge;
        _parameters = new HashMap<>(session.getAdditionalValues());
        _parameters.put(PARAM_ALLOW_SESSION_MERGE, allowSessionMerge);
        _parameters.put(PARAM_OVERRIDE_EXCEPTIONS, overrideExceptions);
        if (destination != null) {
            _parameters.put(PARAM_DESTINATION, destination);
        }
        _archiveOperationId = getControlString();
        _archiveEventListener = XDAT.getContextService().getBean(ArchiveEventListener.class);
        _archiveEventListener.addStatusListener(this);
    }

    @Override
    public String call() throws Exception {
        if (isInline()) {
            //This is being done as part of a parent transaction and should not manage prearc cache state.
            log.debug("Completing inline archive operation to auto-archive session {} to {}", getPrearcSession(), getDestination());
            return ListenerUtils.addListeners(this, new PrearcSessionArchiver(_control, getPrearcSession(),
                                                                              getUser(),
                                                                              removePrearcVariables(_prearcSession.getAdditionalValues()),
                                                                              isOverrideExceptions(),
                                                                              isAllowSessionMerge(),
                                                                              false,
                                                                              _prearcSession.isOverwriteFiles())).call();
        }

        try {
            final EventTrackingDataService eventTrackingDataService = XDAT.getContextService()
                    .getBean(EventTrackingDataService.class);
            eventTrackingDataService.createWithKey(_archiveOperationId);

            log.debug("Queuing archive operation to auto-archive session {} to {}", getPrearcSession(), getDestination());
            final PrearchiveOperationRequest request = new PrearchiveOperationRequest(getUser(), Archive,
                    getSessionData(), getSessionDir(), getParameters(), _archiveOperationId);
            XDAT.sendJmsRequest(request);
            log.trace("{}: Dispatched JMS request: {}", getArchiveOperationId(), request);

            final int       timeout   = XDAT.getIntSiteConfigurationProperty("sessionArchiveTimeoutInterval", 600);
            final StopWatch stopWatch = StopWatch.createStarted();
            EventTrackingDataPojo eventTrackingData = null;
            while (eventTrackingData == null || eventTrackingData.getSucceeded() == null) {
                if (stopWatch.getTime(TimeUnit.SECONDS) > timeout) {
                    String msg = "The session " + getPrearcSession().toString() +
                            " did not return a valid data URI within the timeout interval of " + timeout + " seconds.";
                    throw new TimeoutException(msg);
                }
                log.debug("Checked for message with final status but didn't find it, sleeping for a bit...");
                Thread.sleep(500);
                try {
                    eventTrackingData = eventTrackingDataService.getPojoByKey(_archiveOperationId);
                } catch (NotFoundException e) {
                    // Ignore, eventTrackingData will be null until timeout exception or JMS starts working
                }
            }

            String uriOrMessage = eventTrackingData.getFinalMessage();
            StatusMessage.Status status = eventTrackingData.getSucceeded() ? COMPLETED : FAILED;
            log.debug("Found event tracking data with status {}: {}", status, uriOrMessage);
            notify(new StatusMessage(this, status, uriOrMessage, true));
            if (status == COMPLETED) {
                if (StringUtils.isBlank(uriOrMessage))  {
                    throw new ServerException(Status.SERVER_ERROR_INTERNAL,
                            "No URI returned when trying to archive " + _session);
                } else {
                    return wrapPartialDataURI(uriOrMessage);
                }
            }  else {
                throw new ServerException(Status.SERVER_ERROR_INTERNAL, status + ": " + uriOrMessage);
            }
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

    public void submitAsync() {
        getExecutor().submit(this);
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
    @Getter(PUBLIC)
    private final @NonNull String _archiveOperationId;

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
