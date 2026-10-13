/*
 * web: org.nrg.xnat.restlet.actions.SessionImportSiteAnonymization
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.actions;

import org.nrg.action.ServerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Runs a session import's site anonymization script. The import unpacks the upload into the prearchive before the
 * script runs, and the script changes no instance unless it succeeds on all of them, so when it fails every instance
 * is still as it arrived. Left in the prearchive, those instances would be visible to project users, and archiving
 * the session by hand would skip the site script. The session is removed instead, as the receive path removes an
 * instance whose site script fails. An import that merged into a session already in the prearchive deletes only the
 * instances it wrote, and leaves the session with the instances that were there before (see {@link #removal}).
 * <p>
 * Kept apart from {@link SessionImporter}, whose superclass needs a Spring context to load, so this can be tested on
 * its own.
 */
final class SessionImportSiteAnonymization {
    private SessionImportSiteAnonymization() {
    }

    /** Removes the session from the prearchive, returning whether it did. */
    @FunctionalInterface
    interface Removal {
        boolean remove() throws Exception;
    }

    /** Deletes files from a prearchive session. */
    @FunctionalInterface
    interface FileDeletion {
        void delete(List<Path> files) throws Exception;
    }

    /** The site script failed and the session was removed from the prearchive. */
    static final class SessionRemovedException extends ServerException {
        SessionRemovedException(final Exception cause) {
            super("The site anonymization script failed, so the upload was removed from the prearchive: " + cause.getMessage(), cause);
        }
    }

    /**
     * @return The site script's results.
     *
     * @throws SessionRemovedException When the script failed and the session was removed.
     * @throws Exception               The script's own failure when the session couldn't be removed, with any failure of
     *                                 the removal attached as suppressed.
     */
    static List<AnonymizationResult> run(final Callable<List<AnonymizationResult>> siteScript, final Removal removal) throws Exception {
        try {
            return siteScript.call();
        } catch (Exception e) {
            boolean removed = false;
            try {
                removed = removal.remove();
            } catch (Exception removalFailure) {
                e.addSuppressed(removalFailure);
            }
            if (removed) {
                throw new SessionRemovedException(e);
            }
            throw e;
        }
    }

    /**
     * What a failed site script removes. When the session didn't exist before the import, the import created it and
     * the whole session is removed. When the import merged into an existing session, only the instance files written
     * since {@code before} are deleted, and the session stays, with the instances that were there before, for the
     * caller to mark: the removal reports that it removed nothing.
     *
     * @param before        the session directory as it was before the import.
     * @param removeSession removes the whole session from the prearchive.
     * @param deleteFiles   deletes the given files from the session.
     */
    static Removal removal(final SessionFiles before, final Removal removeSession, final FileDeletion deleteFiles) {
        if (!before.existed()) {
            return removeSession;
        }
        return () -> {
            deleteFiles.delete(before.writtenSince());
            return false;
        };
    }

    /**
     * The instance files of a prearchive session directory and when each was last written, taken before an import so
     * that the files the import writes can be told from the ones already there. Catalogs and logs are not instance
     * files and are left out.
     */
    static final class SessionFiles {
        /**
         * @param sessionDir the session directory, or null when the import cannot merge into an existing session.
         */
        static SessionFiles of(final File sessionDir) throws IOException {
            final Path dir = sessionDir == null ? null : sessionDir.toPath();
            return new SessionFiles(dir, dir != null && Files.isDirectory(dir) ? instanceFiles(dir) : null);
        }

        private SessionFiles(final Path dir, final Map<Path, FileVersion> before) {
            _dir    = dir;
            _before = before;
        }

        /** Whether the session directory existed when this was taken. */
        boolean existed() {
            return _before != null;
        }

        /** The instance files written since this was taken: the new ones and the ones whose size or time changed. */
        List<Path> writtenSince() throws IOException {
            if (_dir == null || !Files.isDirectory(_dir)) {
                return Collections.emptyList();
            }
            return instanceFiles(_dir).entrySet().stream()
                                      .filter(file -> _before == null || !file.getValue().equals(_before.get(file.getKey())))
                                      .map(Map.Entry::getKey)
                                      .sorted()
                                      .collect(Collectors.toList());
        }

        private static Map<Path, FileVersion> instanceFiles(final Path dir) throws IOException {
            final Map<Path, FileVersion> files = new HashMap<>();
            try (Stream<Path> paths = Files.walk(dir)) {
                for (final Path path : (Iterable<Path>) paths.filter(Files::isRegularFile).filter(SessionFiles::isInstanceFile)::iterator) {
                    final BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
                    files.put(path, new FileVersion(attributes.size(), attributes.lastModifiedTime().toMillis()));
                }
            }
            return files;
        }

        private static boolean isInstanceFile(final Path path) {
            final String name = path.getFileName().toString().toLowerCase();
            return !name.endsWith(".xml") && !name.endsWith(".log");
        }

        private record FileVersion(long size, long modified) {
        }

        private final Path                   _dir;
        private final Map<Path, FileVersion> _before;
    }
}
