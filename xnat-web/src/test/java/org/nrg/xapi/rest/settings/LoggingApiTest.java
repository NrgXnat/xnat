/*
 * web: org.nrg.xapi.rest.settings.LoggingApiTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Verifies that the log-download endpoint confines the requested {@code path} to the XNAT logs directory,
 * checking containment against the real (symlink-resolved) path so a link inside the logs directory cannot
 * escape. In particular, files that live under the XNAT home but outside the logs directory (such as the
 * configuration files) must not be reachable.
 */
public class LoggingApiTest {

    private Path home;      // XNAT home (real path)
    private Path logs;      // home/logs -- the permitted root
    private Path config;    // home/config -- a sibling of logs holding sensitive files
    private Path outside;   // a directory outside the XNAT home

    @Before
    public void setUp() throws IOException {
        home    = Files.createTempDirectory("xnat-home").toRealPath();
        logs    = Files.createDirectories(home.resolve("logs"));
        config  = Files.createDirectories(home.resolve("config"));
        Files.write(config.resolve("xnat-conf.properties"), "datasource.password=secret".getBytes());
        outside = Files.createTempDirectory("xnat-outside").toRealPath();
        Files.write(outside.resolve("secret.txt"), "top secret".getBytes());
    }

    @After
    public void tearDown() throws IOException {
        for (final Path root : new Path[]{home, outside}) {
            if (root != null && Files.exists(root)) {
                Files.walk(root).sorted(Comparator.reverseOrder()).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (IOException ignored) { }
                });
            }
        }
    }

    @Test
    public void blankPathDefaultsToLogsFolder() {
        assertEquals(logs, LoggingApi.resolveLogPathWithinLogsFolder(logs, null));
        assertEquals(logs, LoggingApi.resolveLogPathWithinLogsFolder(logs, "   "));
    }

    @Test
    public void allowsPathsInsideLogsFolder() throws IOException {
        final Path nested = Files.createDirectories(logs.resolve("nested"));
        assertEquals(logs, LoggingApi.resolveLogPathWithinLogsFolder(logs, logs.toString()));
        assertEquals(nested, LoggingApi.resolveLogPathWithinLogsFolder(logs, nested.toString()));
    }

    @Test
    public void allowsNonExistentPathInsideLogsFolder() {
        // Preserves the downstream "no files found" (404) behaviour for a valid but empty location.
        final Path missing = logs.resolve("does-not-exist");
        assertEquals(missing, LoggingApi.resolveLogPathWithinLogsFolder(logs, missing.toString()));
    }

    @Test
    public void rejectsSiblingConfigDirectory() {
        // The configuration directory sits next to logs under XNAT home; its files (e.g. database
        // credentials) must not be reachable through the log-download endpoint.
        assertThrows(IllegalArgumentException.class,
                     () -> LoggingApi.resolveLogPathWithinLogsFolder(logs, config.toString()));
        assertThrows(IllegalArgumentException.class,
                     () -> LoggingApi.resolveLogPathWithinLogsFolder(logs, config.resolve("xnat-conf.properties").toString()));
    }

    @Test
    public void rejectsAbsolutePathOutsideLogsFolder() {
        assertThrows(IllegalArgumentException.class,
                     () -> LoggingApi.resolveLogPathWithinLogsFolder(logs, outside.resolve("secret.txt").toString()));
    }

    @Test
    public void rejectsTraversalEscapingLogsFolder() {
        assertThrows(IllegalArgumentException.class,
                     () -> LoggingApi.resolveLogPathWithinLogsFolder(logs, logs.resolve("../config").toString()));
    }

    @Test
    public void rejectsSiblingDirectoryPrefixCollision() throws IOException {
        // "logs-evil" must not be treated as inside "logs".
        final Path evil = Files.createDirectories(home.resolve("logs-evil"));
        assertThrows(IllegalArgumentException.class,
                     () -> LoggingApi.resolveLogPathWithinLogsFolder(logs, evil.toString()));
    }

    @Test
    public void rejectsSymlinkInsideLogsPointingOutside() throws IOException {
        // A symlink located inside the logs directory but pointing outside it must not allow escape.
        final Path link = Files.createSymbolicLink(logs.resolve("escape"), outside);
        assertThrows(IllegalArgumentException.class,
                     () -> LoggingApi.resolveLogPathWithinLogsFolder(logs, link.toString()));
        assertThrows(IllegalArgumentException.class,
                     () -> LoggingApi.resolveLogPathWithinLogsFolder(logs, link.resolve("secret.txt").toString()));
    }
}
