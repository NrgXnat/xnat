/*
 * web: org.nrg.xnat.helpers.merge.anonymize.SameVolumeStagingDirectoryResolver
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.merge.anonymize;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.dicom.mizer.service.StagingDirectoryResolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * Stages the anonymized version of a file on the data volume the file lives on, so that putting
 * it in place is a rename rather than a copy.
 * <p>
 * A file under one of the configured data roots -- the archive and the prearchive -- is staged in
 * a {@value #STAGING_DIRECTORY_NAME} directory at that root. The directory sits above every
 * project, timestamp, session and resource directory, so nothing that lists those ever sees a
 * staged file, and the prearchive trawler skips dotted names at the root. That matters because a
 * staged file left behind by a crash would otherwise be catalogued as a resource or parsed as a
 * DICOM object, which is what happened when lock files lived beside the data.
 * <p>
 * A file under none of the roots is staged wherever the fallback says, which by default is
 * {@code java.io.tmpdir}, and is replaced by a copy as it always was. So is a file under a root
 * where the staging directory can't be created. The same happens, one level down, when a root
 * turns out to span more than one mount: the rename fails and the caller copies.
 */
@Slf4j
public class SameVolumeStagingDirectoryResolver implements StagingDirectoryResolver {
    public static final String STAGING_DIRECTORY_NAME = ".xnat-tmp";

    /**
     * @param dataRoots supplies the data root paths, consulted on every call so a change to the
     *                  site configuration takes effect without a restart. Blank entries are ignored.
     */
    public SameVolumeStagingDirectoryResolver(final Supplier<? extends Collection<String>> dataRoots) {
        this(dataRoots, StagingDirectoryResolver.JAVA_IO_TMPDIR);
    }

    public SameVolumeStagingDirectoryResolver(final Supplier<? extends Collection<String>> dataRoots, final StagingDirectoryResolver fallback) {
        _dataRoots = dataRoots;
        _fallback  = fallback;
    }

    @Override
    public File resolve(final File dicomFile) throws IOException {
        final Path target     = dicomFile.toPath().toAbsolutePath().normalize();
        final Path realTarget = realPathOrNull(target);
        for (final String dataRoot : _dataRoots.get()) {
            if (StringUtils.isBlank(dataRoot)) {
                continue;
            }
            final Path root = Paths.get(dataRoot).toAbsolutePath().normalize();
            if (isUnder(target, realTarget, root)) {
                final Path staging = root.resolve(STAGING_DIRECTORY_NAME);
                try {
                    Files.createDirectories(staging);
                } catch (IOException e) {
                    // A root this process can't write to (a read-only root over per-project mounts,
                    // say) staged in the temp directory before this class existed, and still can;
                    // only the rename is lost.
                    log.warn("Unable to create {}, staging {} under the fallback location instead: {}", staging, dicomFile, e.toString());
                    return _fallback.resolve(dicomFile);
                }
                return staging.toFile();
            }
        }
        log.debug("{} is under none of the data roots, staging it under the fallback location", dicomFile);
        return _fallback.resolve(dicomFile);
    }

    /**
     * Compares as given first, then through symlinks: a root configured through a link and a file
     * addressed through the real path, or the other way round, still belong together.
     */
    private static boolean isUnder(final Path target, final Path realTarget, final Path root) {
        if (target.startsWith(root)) {
            return true;
        }
        final Path realRoot = realPathOrNull(root);
        return realTarget != null && realRoot != null && realTarget.startsWith(realRoot);
    }

    private static Path realPathOrNull(final Path path) {
        try {
            return path.toRealPath();
        } catch (IOException e) {
            return null;
        }
    }

    private final Supplier<? extends Collection<String>> _dataRoots;
    private final StagingDirectoryResolver               _fallback;
}
