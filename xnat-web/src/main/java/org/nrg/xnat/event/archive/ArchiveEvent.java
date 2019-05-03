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
    public static ArchiveEvent completed(final Operation operation, final String session) {
        return completed(operation, null, null, session, null);
    }

    public static ArchiveEvent completed(final Operation operation, final String session, final String message) {
        return completed(operation, null, null, session, message);
    }

    public static ArchiveEvent completed(final Operation operation, final SessionData session) {
        return completed(operation, session.getProject(), session.getTimestamp(), session.getName(), null);
    }

    public static ArchiveEvent completed(final Operation operation, final SessionData session, final String message) {
        return completed(operation, session.getProject(), session.getTimestamp(), session.getName(), message);
    }

    public static ArchiveEvent completed(final Operation operation, final String project, final String timestamp, final String session) {
        return completed(operation, project, timestamp, session, null);
    }

    public static ArchiveEvent completed(final Operation operation, final String project, final String timestamp, final String session, final String message) {
        return builder().operation(operation).status(Status.Completed).progress(100).project(project).timestamp(timestamp).session(session).message(message).build();
    }

    public static ArchiveEvent failed(final Operation operation, final String session) {
        return failed(operation, null, null, session, null);
    }

    public static ArchiveEvent failed(final Operation operation, final String session, final String message) {
        return failed(operation, null, null, session, message);
    }

    public static ArchiveEvent failed(final Operation operation, final SessionData session) {
        return failed(operation, session.getProject(), session.getTimestamp(), session.getName(), null);
    }

    public static ArchiveEvent failed(final Operation operation, final SessionData session, final String message) {
        return failed(operation, session.getProject(), session.getTimestamp(), session.getName(), message);
    }

    public static ArchiveEvent failed(final Operation operation, final String project, final String timestamp, final String session) {
        return failed(operation, project, timestamp, session, null);
    }

    public static ArchiveEvent failed(final Operation operation, final String project, final String timestamp, final String session, final String message) {
        return builder().operation(operation).status(Status.Failed).progress(100).project(project).timestamp(timestamp).session(session).message(message).build();
    }

    public static ArchiveEvent progress(final Operation operation, final int progress, final String session) {
        return progress(operation, progress, null, null, session, null);
    }

    public static ArchiveEvent progress(final Operation operation, final int progress, final String session, final String message) {
        return progress(operation, progress, null, null, session, message);
    }

    public static ArchiveEvent progress(final Operation operation, final int progress, final SessionData session) {
        return progress(operation, progress, session.getProject(), session.getTimestamp(), session.getName(), null);
    }

    public static ArchiveEvent progress(final Operation operation, final int progress, final SessionData session, final String message) {
        return progress(operation, progress, session.getProject(), session.getTimestamp(), session.getName(), message);
    }

    public static ArchiveEvent progress(final Operation operation, final int progress, final String project, final String timestamp, final String session) {
        return progress(operation, progress, project, timestamp, session, null);
    }

    public static ArchiveEvent progress(final Operation operation, final int progress, final String project, final String timestamp, final String session, final String message) {
        return builder().operation(operation).status(Status.InProgress).progress(progress).project(project).timestamp(timestamp).session(session).message(message).build();
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

    @Getter(lazy = true)
    private final @NonNull String    _archiveEventId = XnatHttpUtils.buildArchiveEventId(_project, _timestamp, _session);
}
