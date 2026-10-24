/*
 * web: org.nrg.xnat.archive.ImportFailures
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.archive.services.DirectArchiveSessionService;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.SessionException;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;
import org.nrg.xnat.helpers.uri.URIManager;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * An import that fails partway, a compressed upload or an inbox folder, can leave sessions holding only part of a
 * study. These keep anything from archiving them as though the import had finished.
 */
@Slf4j
public final class ImportFailures {
    private ImportFailures() {
    }

    /** What a failed import marks ERROR: a session still receiving, or at rest. */
    private static final Set<PrearcStatus> MARKABLE = EnumSet.of(PrearcStatus.RECEIVING, PrearcStatus.RECEIVING_INTERRUPT,
                                                                 PrearcStatus.READY, PrearcStatus.CONFLICT, PrearcStatus.ERROR);

    /**
     * Whether the instance ends before its last value does: a Part 10 file cut short, such as one still being copied.
     * GradualDicomImporter reports it as unreadable like any other instance it can't read, and the zip importer
     * skips it as such under Ignore-Unparsable, but it is DICOM, so the inbox stops at it and keeps the file.
     */
    public static boolean endsEarly(final Throwable e) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof ReceivedDicomObject.TruncatedObjectException) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether creating the session failed because another session is already at its prearchive folder: the database's
     * unique constraint refused the row (PostgreSQL reports SQLState 23505 rather than an
     * {@link SQLIntegrityConstraintViolationException}), or, for a session with no project, the prearchive's own check
     * for duplicates gave up.
     *
     * <p>Lives here rather than on the importer so that callers outside the import path -- the C-STORE responder, which
     * turns this into its own DICOM status -- can ask without loading {@link GradualDicomImporter} and the Spring
     * lookup its class initialization performs.
     */
    public static boolean isDestinationTaken(final Throwable e) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLIntegrityConstraintViolationException
                || cause instanceof SQLException sql && "23505".equals(sql.getSQLState())
                || cause instanceof SessionException session && session.getError() == SessionException.Error.AlreadyExists) {
                return true;
            }
        }
        return false;
    }

    /**
     * Marks the sessions an import wrote into before it failed. A prearchive session is marked {@code ERROR}, with the
     * reason in its log. Left receiving, it looks like one still arriving, and unless the upload tool sent it, the
     * idle-timeout rebuild builds it as though the import had finished, then archives it where the project archives
     * automatically. An import of the same study still reopens it: a session in error takes more data. A session
     * already queued for, or in the middle of, a build, archive, delete or move is left to that operation, which would
     * run anyway. A direct-archive session is marked instead and keeps receiving; once idle, it moves to the prearchive in
     * {@code ERROR}, as it does when its build or archive fails.
     *
     * @param uris  The sessions, as GradualDicomImporter returns them.
     * @param cause Why the import failed.
     */
    public static void markFailed(final Collection<String> uris, final Exception cause) {
        for (final String uri : uris) {
            try {
                if (StringUtils.startsWith(uri, "/prearchive/")) {
                    final Map<String, Object> properties = PrearcUtils.parseURI(uri);
                    final String              project    = (String) properties.get(URIManager.PROJECT_ID);
                    final String              timestamp  = (String) properties.get(PrearcUtils.PREARC_TIMESTAMP);
                    final String              folder     = (String) properties.get(PrearcUtils.PREARC_SESSION_FOLDER);
                    final PrearcStatus status = PrearcDatabase.getSession(folder, timestamp, project).getStatus();
                    if (!MARKABLE.contains(status)) {
                        log.warn("The import failed after writing into {}, which is {} and is left as it is: {}", uri, status, cause.getMessage());
                        continue;
                    }
                    PrearcUtils.log(project, timestamp, folder, "An import into this session failed partway, so it may be missing files. "
                                                                + "Importing the study again adds them. The import failed with: " + cause.getMessage());
                    if (PrearcDatabase.setStatus(folder, timestamp, project, PrearcUtils.PrearcStatus.ERROR)) {
                        log.warn("The import failed after writing into {}, which is now marked ERROR: {}", uri, cause.getMessage());
                    } else {
                        log.warn("The import failed after writing into {}, which could not be marked ERROR", uri);
                    }
                } else if (StringUtils.startsWith(uri, "/xapi/direct-archive/")) {
                    // /xapi/direct-archive/<project>/<tag>/<name>, built by GradualDicomImporter. No parser exists for these.
                    final String[] elements = uri.split("/");
                    final DirectArchiveSessionService directArchive = XDAT.getContextService().getBean(DirectArchiveSessionService.class);
                    directArchive.handleFailedUpload(directArchive.findByProjectTagName(elements[3], elements[4], elements[5]), cause);
                }
            } catch (Exception e) {
                log.warn("The import failed after writing into {}, which could not be marked ERROR", uri, e);
            }
        }
    }
}
