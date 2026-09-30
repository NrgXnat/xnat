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
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.archive.services.DirectArchiveSessionService;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;

import java.util.Collection;

/**
 * An import that fails partway, a compressed upload or an inbox folder, can leave sessions holding only part of a
 * study. These keep anything from archiving them as though the import had finished.
 */
@Slf4j
public final class ImportFailures {
    private ImportFailures() {
    }

    /**
     * GradualDicomImporter reports every failure as a ClientException, but one caused by a ServerException, such as a
     * failed write or a script or processor error, says nothing about the object: it fails the import even where
     * unparsable objects are skipped.
     */
    public static boolean isUnparsable(final ClientException e) {
        return !(e.getCause() instanceof ServerException);
    }

    /**
     * Marks the sessions an import wrote into before it failed. A prearchive session is marked {@code ERROR}, with the
     * reason in its log. Left receiving, it looks like one still arriving, and unless the upload tool sent it, the
     * idle-timeout rebuild builds it as though the import had finished, then archives it where the project archives
     * automatically. An import of the same study still reopens it: a session in error takes more data. A
     * direct-archive session moves to the prearchive in {@code ERROR}, as it does when its build or archive fails.
     *
     * @param uris  The sessions, as GradualDicomImporter returns them.
     * @param cause Why the import failed.
     */
    public static void markFailed(final Collection<String> uris, final Exception cause) {
        for (final String uri : uris) {
            final String[] elements = uri.split("/");
            try {
                if (StringUtils.startsWith(uri, "/prearchive/")) {
                    // /prearchive/projects/<project>/<timestamp>/<session>
                    final String project   = elements[3];
                    final String timestamp = elements[4];
                    final String folder    = elements[5];
                    PrearcUtils.log(project, timestamp, folder, "An import into this session failed partway, so it may be missing files. "
                                                                + "Importing the study again adds them. The import failed with: " + cause.getMessage());
                    if (PrearcDatabase.setStatus(folder, timestamp, project, PrearcUtils.PrearcStatus.ERROR)) {
                        log.warn("The import failed after writing into {}, which is now marked ERROR: {}", uri, cause.getMessage());
                    } else {
                        log.warn("The import failed after writing into {}, which could not be marked ERROR", uri);
                    }
                } else if (StringUtils.startsWith(uri, "/xapi/direct-archive/")) {
                    // /xapi/direct-archive/<project>/<tag>/<name>
                    final DirectArchiveSessionService directArchive = XDAT.getContextService().getBean(DirectArchiveSessionService.class);
                    directArchive.handleFailedUpload(directArchive.findByProjectTagName(elements[3], elements[4], elements[5]), cause);
                }
            } catch (Exception e) {
                log.warn("The import failed after writing into {}, which could not be marked ERROR", uri, e);
            }
        }
    }
}
