/*
 * web: org.nrg.xnat.restlet.services.CachedUpload
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.services;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * A file in a user's cache that an import reads with {@code src}, such as a zip from the Compressed Uploader. Once the
 * import has run, the file isn't needed, and {@link #delete()} removes it with the folders it leaves empty, up to and
 * including the cache folder named in {@code src}.
 */
@Slf4j
class CachedUpload {
    /**
     * @param cache  The user's cache folder.
     * @param folder The cache folder named in {@code src}.
     * @param file   The uploaded file.
     */
    CachedUpload(final Path cache, final Path folder, final Path file) {
        _cache = cache.toAbsolutePath().normalize();
        _folder = folder.toAbsolutePath().normalize();
        _file = file.toAbsolutePath().normalize();
    }

    /**
     * Deletes the file, if it's still there, and then each folder it leaves empty, up to and including the cache folder
     * named in {@code src}. Some importers move their upload elsewhere, so the file may already be gone. Nothing outside
     * that cache folder is deleted.
     */
    void delete() {
        if (!_folder.startsWith(_cache) || _folder.equals(_cache) || !_file.startsWith(_folder) || _file.equals(_folder)) {
            log.warn("Not deleting the import's upload {}: it isn't in a folder of the user's cache {}", _file, _cache);
            return;
        }
        try {
            Files.deleteIfExists(_file);
            // The image uploader puts several files in one folder and imports each on its own, so the folder goes with
            // the last of them, and two imports that finish together may both find it empty.
            for (Path folder = _file.getParent(); folder.startsWith(_folder) && isEmptyFolder(folder); folder = folder.getParent()) {
                Files.deleteIfExists(folder);
            }
            log.debug("Deleted the import's upload {} from the user's cache", _file);
        } catch (IOException e) {
            log.warn("Couldn't delete the import's upload {} from the user's cache", _file, e);
        }
    }

    private static boolean isEmptyFolder(final Path folder) throws IOException {
        if (!Files.isDirectory(folder)) {
            return false;
        }
        try (final Stream<Path> entries = Files.list(folder)) {
            return entries.findAny().isEmpty();
        }
    }

    private final Path _cache;
    private final Path _folder;
    private final Path _file;
}
