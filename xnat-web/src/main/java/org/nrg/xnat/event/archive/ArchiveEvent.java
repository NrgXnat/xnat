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
import org.nrg.xnat.archive.Operation;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.utils.XnatHttpUtils;

/**
 * The Class XftItemEvent.
 */
@Data
@Accessors(prefix = "_")
@AllArgsConstructor
@Builder
@Slf4j
public class ArchiveEvent implements ArchiveEventI {
    public static ArchiveEvent completed(final Operation operation, final String session, final String listenerId) {
        return completed(operation, null, null, session, listenerId, null);
    }

    public static ArchiveEvent completed(final Operation operation, final String session, final String listenerId, final String message) {
        return completed(operation, null, null, session, listenerId, message);
    }

    public static ArchiveEvent completed(final Operation operation, final SessionData session, final String listenerId) {
        return completed(operation, session.getProject(), session.getTimestamp(), session.getName(), listenerId, null);
    }

    public static ArchiveEvent completed(final Operation operation, final SessionData session, final String listenerId, final String message) {
        return completed(operation, session.getProject(), session.getTimestamp(), session.getName(), listenerId, message);
    }

    public static ArchiveEvent completed(final Operation operation, final String project, final String timestamp, final String session, final String listenerId) {
        return completed(operation, project, timestamp, session, listenerId, null);
    }

    public static ArchiveEvent completed(final Operation operation, final String project, final String timestamp, final String session, final String listenerId, final String message) {
        return builder().operation(operation).status(Status.Completed).progress(100).project(project).timestamp(timestamp).session(session).archiveEventId(listenerId).message(message).eventTime(System.currentTimeMillis()).build();
    }

    public static ArchiveEvent failed(final Operation operation, final String session, final String listenerId) {
        return failed(operation, null, null, session, listenerId, null);
    }

    public static ArchiveEvent failed(final Operation operation, final String session, final String listenerId, final String message) {
        return failed(operation, null, null, session, listenerId, message);
    }

    public static ArchiveEvent failed(final Operation operation, final SessionData session, final String listenerId) {
        return failed(operation, session.getProject(), session.getTimestamp(), session.getName(), listenerId, null);
    }

    public static ArchiveEvent failed(final Operation operation, final SessionData session, final String message, final String listenerId) {
        return failed(operation, session.getProject(), session.getTimestamp(), session.getName(), listenerId, message);
    }

    public static ArchiveEvent failed(final Operation operation, final String project, final String timestamp, final String session, final String listenerId) {
        return failed(operation, project, timestamp, session, listenerId, null);
    }

    public static ArchiveEvent failed(final Operation operation, final String project, final String timestamp, final String session, final String listenerId, final String message) {
        return builder().operation(operation).status(Status.Failed).progress(100).project(project).timestamp(timestamp).session(session).archiveEventId(listenerId).message(message).eventTime(System.currentTimeMillis()).build();
    }

    public static ArchiveEvent warn(final Operation operation, final String session, final String listenerId) {
        return warn(operation, null, null, session, listenerId, null);
    }

    public static ArchiveEvent warn(final Operation operation, final String session, final String listenerId, final String message) {
        return warn(operation, null, null, session, listenerId, message);
    }
    
    public static ArchiveEvent warn(final Operation operation, final SessionData session, final String listenerId) {
        return warn(operation, session.getProject(), session.getTimestamp(), session.getName(), listenerId, null);
    }

    public static ArchiveEvent warn(final Operation operation, final SessionData session, final String message, final String listenerId) {
        return warn(operation, session.getProject(), session.getTimestamp(), session.getName(), listenerId, message);
    }

    public static ArchiveEvent warn(final Operation operation, final String project, final String timestamp, final String session, final String listenerId) {
        return warn(operation, project, timestamp, session, listenerId, null);
    }

    public static ArchiveEvent warn(final Operation operation, final String project, final String timestamp, final String session, final String listenerId, final String message) {
        return builder().operation(operation).status(Status.Warning).progress(100).project(project).timestamp(timestamp).session(session).archiveEventId(listenerId).message(message).eventTime(System.currentTimeMillis()).build();
    }

    public static ArchiveEvent progress(final Operation operation, final int progress, final String session, final String listenerId) {
        return progress(operation, progress, null, null, session, listenerId, null);
    }

    public static ArchiveEvent progress(final Operation operation, final int progress, final String session, final String listenerId, final String message) {
        return progress(operation, progress, null, null, session, listenerId, message);
    }

    public static ArchiveEvent progress(final Operation operation, final int progress, final SessionData session, final String listenerId) {
        return progress(operation, progress, session.getProject(), session.getTimestamp(), session.getName(), listenerId, null);
    }

    public static ArchiveEvent progress(final Operation operation, final int progress, final SessionData session, final String listenerId, final String message) {
        return progress(operation, progress, session.getProject(), session.getTimestamp(), session.getName(), listenerId, message);
    }

    public static ArchiveEvent progress(final Operation operation, final int progress, final String project, final String timestamp, final String session, final String listenerId) {
        return progress(operation, progress, project, timestamp, session, listenerId, null);
    }

    public static ArchiveEvent progress(final Operation operation, final int progress, final String project, final String timestamp, final String session, final String listenerId, final String message) {
        return builder().operation(operation).status(Status.InProgress).progress(progress).project(project).timestamp(timestamp).session(session).archiveEventId(listenerId).message(message).eventTime(System.currentTimeMillis()).build();
    }

    @Override
    public String toString() {
        return getArchiveEventId() + ":" + _operation.toString() + ":" + _status.toString() + " (" + _progress + ")";
    }

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
}
