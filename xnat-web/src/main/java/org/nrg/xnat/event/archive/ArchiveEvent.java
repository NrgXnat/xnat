/*
 * core: org.nrg.xft.event.XftItemEvent
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.archive;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.archive.Operation;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.tracking.model.ArchiveEventTrackingLog;
import org.nrg.xnat.utils.XnatHttpUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * The Class XftItemEvent.
 */
@Data
@Accessors(prefix = "_")
@AllArgsConstructor
@Builder
@Slf4j
public class ArchiveEvent implements ArchiveEventI {
    public static ArchiveEvent completed(final Integer userId, final Operation operation, final String session, final String listenerId) {
        return completed(userId, operation, null, null, session, listenerId, null);
    }

    public static ArchiveEvent completed(final Integer userId, final Operation operation, final String session, final String listenerId, final String message) {
        return completed(userId, operation, null, null, session, listenerId, message);
    }

    public static ArchiveEvent completed(final Integer userId, final Operation operation, final SessionData session, final String listenerId) {
        return completed(userId, operation, session.getProject(), session.getTimestamp(), session.getName(), listenerId, null);
    }

    public static ArchiveEvent completed(final Integer userId, final Operation operation, final SessionData session, final String listenerId, final String message) {
        return completed(userId, operation, session.getProject(), session.getTimestamp(), session.getName(), listenerId, message);
    }

    public static ArchiveEvent completed(final Integer userId, final Operation operation, final String project, final String timestamp, final String session, final String listenerId) {
        return completed(userId, operation, project, timestamp, session, listenerId, null);
    }

    public static ArchiveEvent completed(final Integer userId, final Operation operation, final String project, final String timestamp, final String session, final String listenerId, final String message) {
        return builder().operation(operation).status(Status.Completed).progress(100).project(project).timestamp(timestamp).session(session).archiveEventId(listenerId).message(message).eventTime(System.currentTimeMillis()).userId(userId).build();
    }

    public static ArchiveEvent failed(final Integer userId, final Operation operation, final String session, final String listenerId) {
        return failed(userId, operation, null, null, session, listenerId, null);
    }

    public static ArchiveEvent failed(final Integer userId, final Operation operation, final String session, final String listenerId, final String message) {
        return failed(userId, operation, null, null, session, listenerId, message);
    }

    public static ArchiveEvent failed(final Integer userId, final Operation operation, final SessionData session, final String listenerId) {
        return failed(userId, operation, session.getProject(), session.getTimestamp(), session.getName(), listenerId, null);
    }

    public static ArchiveEvent failed(final Integer userId, final Operation operation, final SessionData session, final String listenerId, final String message) {
        return failed(userId, operation, session.getProject(), session.getTimestamp(), session.getName(), listenerId, message);
    }

    public static ArchiveEvent failed(final Integer userId, final Operation operation, final String project, final String timestamp, final String session, final String listenerId) {
        return failed(userId, operation, project, timestamp, session, listenerId, null);
    }

    public static ArchiveEvent failed(final Integer userId, final Operation operation, final String project, final String timestamp, final String session, final String listenerId, final String message) {
        return builder().operation(operation).status(Status.Failed).progress(100).project(project).timestamp(timestamp).session(session).archiveEventId(listenerId).message(message).eventTime(System.currentTimeMillis()).userId(userId).build();
    }

    public static ArchiveEvent warn(final Integer userId, final Operation operation, final String session, final String listenerId) {
        return warn(userId, operation, null, null, session, listenerId, null);
    }

    public static ArchiveEvent warn(final Integer userId, final Operation operation, final String session, final String listenerId, final String message) {
        return warn(userId, operation, null, null, session, listenerId, message);
    }
    
    public static ArchiveEvent warn(final Integer userId, final Operation operation, final SessionData session, final String listenerId) {
        return warn(userId, operation, session.getProject(), session.getTimestamp(), session.getName(), listenerId, null);
    }

    public static ArchiveEvent warn(final Integer userId, final Operation operation, final SessionData session, final String listenerId, final String message) {
        return warn(userId, operation, session.getProject(), session.getTimestamp(), session.getName(), listenerId, message);
    }

    public static ArchiveEvent warn(final Integer userId, final Operation operation, final String project, final String timestamp, final String session, final String listenerId) {
        return warn(userId, operation, project, timestamp, session, listenerId, null);
    }

    public static ArchiveEvent warn(final Integer userId, final Operation operation, final String project, final String timestamp, final String session, final String listenerId, final String message) {
        return builder().operation(operation).status(Status.Warning).progress(100).project(project).timestamp(timestamp).session(session).archiveEventId(listenerId).message(message).eventTime(System.currentTimeMillis()).userId(userId).build();
    }

    public static ArchiveEvent progress(final Integer userId, final Operation operation, final int progress, final String session, final String listenerId) {
        return progress(userId, operation, progress, null, null, session, listenerId, null);
    }

    public static ArchiveEvent progress(final Integer userId, final Operation operation, final int progress, final String session, final String listenerId, final String message) {
        return progress(userId, operation, progress, null, null, session, listenerId, message);
    }

    public static ArchiveEvent progress(final Integer userId, final Operation operation, final int progress, final SessionData session, final String listenerId) {
        return progress(userId, operation, progress, session.getProject(), session.getTimestamp(), session.getName(), listenerId, null);
    }

    public static ArchiveEvent progress(final Integer userId, final Operation operation, final int progress, final SessionData session, final String listenerId, final String message) {
        return progress(userId, operation, progress, session.getProject(), session.getTimestamp(), session.getName(), listenerId, message);
    }

    public static ArchiveEvent progress(final Integer userId, final Operation operation, final int progress, final String project, final String timestamp, final String session, final String listenerId) {
        return progress(userId, operation, progress, project, timestamp, session, listenerId, null);
    }

    public static ArchiveEvent progress(final Integer userId, final Operation operation, final int progress, final String project, final String timestamp, final String session, final String listenerId, final String message) {
        return builder().operation(operation).status(Status.InProgress).progress(progress).project(project).timestamp(timestamp).session(session).archiveEventId(listenerId).message(message).eventTime(System.currentTimeMillis()).userId(userId).build();
    }

    @Override
    public String toString() {
        return getArchiveEventId() + ":" + _operation.toString() + ":" + _status.toString() + " (" + _progress + ")";
    }

    private final          Integer   _userId;
    private final          String    _project;
    private final          String    _timestamp;
    private final @NonNull String    _session;
    private final @NonNull Operation _operation;
    private final @NonNull Status    _status;
    private final          int       _progress;
    private final          String    _message;
    private final          String    _archiveEventId;
    private final          long      _eventTime;

    public String getArchiveEventId() {
        return StringUtils.defaultIfBlank(_archiveEventId, XnatHttpUtils.buildArchiveEventId(_project, _timestamp, _session));
    }

    @Override
    @Nonnull
    public String getTrackingId() {
        return getArchiveEventId();
    }

    @Override
    public boolean isSuccess() {
        return _status != Status.Failed;
    }

    @Override
    public boolean isCompleted() {
        return _progress == 100;
    }

    @Override
    public String updateTrackingPayload(@Nullable String currentPayload) throws IOException {
        ArchiveEventTrackingLog statusLog;
        if (currentPayload != null) {
            statusLog = XDAT.getSerializerService().getObjectMapper()
                    .readValue(currentPayload, ArchiveEventTrackingLog.class);
        } else {
            statusLog = new ArchiveEventTrackingLog();
        }
        statusLog.addToEntryList(new ArchiveEventTrackingLog.MessageEntry(_status, _eventTime, _message));
        statusLog.sortEntryList();
        return XDAT.getSerializerService().getObjectMapper().writeValueAsString(statusLog);
    }
}
