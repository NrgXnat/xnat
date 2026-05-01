/*
 * framework: org.nrg.framework.configuration.ConfigPaths
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.tools.ant.DirectoryScanner;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Maintains a list of paths for configuration folders and provides utility methods for locating resources within those
 * folders.
 */
@Slf4j
public class ConfigPaths extends ArrayList<Path> {
    /**
     * Creates a default instance of the class. This contains no paths to start.
     */
    public ConfigPaths() {
        log.debug("Creating default instance of the ConfigPaths class.");
    }

    /**
     * Creates an instance of the class populated with the paths in the input parameter.
     *
     * @param paths The paths with which to populate the new instance
     */
    @SuppressWarnings("unused")
    public ConfigPaths(final Collection<Path> paths) {
        super(paths);
        if (log.isDebugEnabled()) {
            log.debug("Creating instance of the ConfigPaths class with {} paths provided: {}", paths.size(), paths.stream().map(Objects::toString).collect(Collectors.joining(", ")));
        }
    }

    /**
     * Attempts to find the indicated subpaths on the list of paths in this instance. The first instance in which a file
     * from the <b>subpaths</b> parameter list is found in one of the paths contained in the instance is returned. No
     * further searching is done. If you want to find instances of each of the subpaths, try calling the {@link
     * #findFiles(String...)} method instead.
     *
     * @param subpaths The subpaths to search for on the list of configuration paths.
     *
     * @return The first instance found of one of the subpaths on one of the configuration paths.
     */
    @SuppressWarnings("unused")
    public File findFile(final String... subpaths) {
        for (final String subpath : subpaths) {
            for (final Path path : this) {
                final File file = path.resolve(subpath).toFile();
                if (file.exists() && file.isFile()) {
                    return file;
                }
            }
        }
        return null;
    }

    /**
     * Attempts to find the indicated subpaths on the list of paths in this instance. The first instance in which a
     * file
     * from each of the entries in the <b>subpaths</b> parameter list is found in one of the paths contained in the
     * instance is added to the list, but every entry is searched for.  If you want to find just the first instance
     * where one of the subpaths is found on one of the configuration paths, call the {@link #findFile(String...)}
     * method instead. The subpaths can be a regular path to a specific file, an <a
     * href="https://ant.apache.org/manual/dirtasks.html#patterns">Ant-style
     * pattern</a>, or a mix of both.
     *
     * @param subpaths The subpaths to search for on the list of configuration paths.
     *
     * @return The first instance found of each of the subpaths on one of the configuration paths.
     */
    @Nonnull
    public List<File> findFiles(final String... subpaths) {
        if (subpaths.length == 0) {
            log.debug("No subpaths specified, returning empty results");
            return new ArrayList<>();
        }
        if (log.isDebugEnabled()) {
            log.debug("Preparing to search {} configuration paths for the following subpaths: {}", size(), String.join(", ", subpaths));
        }
        final Map<String, Path> located = new HashMap<>();
        final DirectoryScanner  scanner = new DirectoryScanner();
        for (final Path path : this) {
            final String root = path.toString();
            log.debug("Scanning path {} for patterns", root);

            scanner.setBasedir(root);
            scanner.setIncludes(subpaths);
            scanner.scan();

            for (final String file : scanner.getIncludedFiles()) {
                if (!located.containsKey(file)) {
                    log.debug("Found file {} matching pattern under path {}", file, root);
                    located.put(file, path);
                } else {
                    log.debug("Found file {} matching pattern under path {}, but this file was already found on the path {}", file, root, located.get(file));
                }
            }
        }

        return located.entrySet().stream()
                      .map(entry -> entry.getValue().resolve(entry.getKey()).toFile())
                      .collect(Collectors.toList());
    }
}
