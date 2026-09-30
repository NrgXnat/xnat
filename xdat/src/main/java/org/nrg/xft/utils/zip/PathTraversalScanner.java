/*
 * core: org.nrg.xft.utils.zip.PathTraversalScanner
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.utils.zip;

import org.nrg.xft.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The path-traversal ("zip-slip") scan shared by every archive-extraction entry point that needs to reject a
 * malicious archive before extracting anything from it -- currently {@code TarUtils} and {@code Unzipper}.
 * Each caller adapts its own archive format's entry iteration (ZipEntry/ZipInputStream, TarEntry/TarInputStream,
 * ...) to {@link EntryNames}; this class only owns the one thing that shouldn't be duplicated per format: what
 * makes an entry name unsafe.
 */
public final class PathTraversalScanner {
    private PathTraversalScanner() {
    }

    /**
     * Yields successive entry names from an archive stream, in whatever format that archive uses. A caller
     * typically supplies this as a lambda wrapping its own {@code getNextEntry()} call, e.g.
     * {@code () -> { final ZipEntry ze = zis.getNextEntry(); return ze == null ? null : ze.getName(); }}.
     */
    @FunctionalInterface
    public interface EntryNames {
        /**
         * @return the next entry's name, or null once the archive is exhausted.
         */
        String next() throws IOException;
    }

    /**
     * Scans every entry name yielded by {@code entries} and returns the names of any entries whose relative path
     * does not resolve within {@code destination} once resolved against it. Callers must extract only after
     * confirming the returned list is empty: rejecting just the unsafe entries and extracting the rest would
     * still leave a malicious upload's other entries writable to disk one-by-one until the unsafe one is reached,
     * rather than rejecting the whole upload atomically.
     *
     * @param entries     Yields each entry's name in turn, until exhausted.
     * @param destination The directory the archive is intended to be extracted into.
     *
     * @return The (possibly empty) list of entry names found in the archive that do not resolve within
     * destination.
     *
     * @throws IOException When an error occurs reading the archive.
     */
    public static List<String> findPathTraversalEntries(final EntryNames entries, final File destination) throws IOException {
        final List<String> unsafeEntries = new ArrayList<>();
        for (String name = entries.next(); name != null; name = entries.next()) {
            if (!FileUtils.isCanonicalPath(destination, name)) {
                unsafeEntries.add(name);
            }
        }
        return unsafeEntries;
    }
}
