/*
 * PrearcImporter: org.nrg.PrearcImporterUnpackDispatcherTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Exercises {@link PrearcImporter.UnpackDispatcher#unpack(File, File)}, the actual integration point
 * {@link PrearcImporter#run()} relies on to decide whether an uploaded archive may be deleted and its
 * extracted contents queued for import. A dispatcher entry matching by filename suffix alone (regardless of
 * whether the underlying {@link Unpacker} actually succeeded) would let a rejected/partially-extracted upload
 * be treated as a successful import.
 */
public class PrearcImporterUnpackDispatcherTest {
    private final static File workingDir = new File("target/test-data-unpack-dispatcher");

    final Project project = new Project();

    @Before
    public final void createWorkingDir() {
        workingDir.mkdirs();
        assertTrue(workingDir.isDirectory());
    }

    @After
    public final void removeWorkingDir() {
        final Delete delete = new Delete();
        delete.setProject(project);
        delete.setDir(workingDir);
        delete.execute();
        assertFalse(workingDir.exists());
    }

    /**
     * A zip containing a safe entry followed by a path-traversal ("zip-slip") entry must be reported as a
     * failed unpack by the dispatcher, not just by Unzipper in isolation -- otherwise PrearcImporter.run()
     * would delete the original upload and queue the partially-extracted, still-unsafe destination for import.
     */
    @Test
    public final void testDispatcherReportsFailureForMixedSafeAndMaliciousEntries() throws Exception {
        final File zip = new File(workingDir, "mixed.zip");
        try (final FileOutputStream fos = new FileOutputStream(zip);
             final ZipOutputStream zos = new ZipOutputStream(fos)) {
            zos.putNextEntry(new ZipEntry("safe.txt"));
            zos.write("safe content".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("../evil.txt"));
            zos.write("payload".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        final File dest = new File(workingDir, "dest");

        final PrearcImporter.UnpackDispatcher ud = new PrearcImporter.UnpackDispatcher();
        ud.add(".zip", new Unzipper());

        assertFalse("a mix of safe entries and a rejected path-traversal entry must not be reported as success",
                    ud.unpack(zip, dest));
        assertFalse("the traversal entry must never be written outside the destination",
                    new File(workingDir, "evil.txt").exists());
        assertTrue("entries preceding the malicious one are still written before the rejection is detected",
                   new File(dest, "safe.txt").isFile());
    }

    @Test
    public final void testDispatcherReportsSuccessForCleanArchive() throws Exception {
        final File zip = new File(workingDir, "clean.zip");
        try (final FileOutputStream fos = new FileOutputStream(zip);
             final ZipOutputStream zos = new ZipOutputStream(fos)) {
            zos.putNextEntry(new ZipEntry("safe.txt"));
            zos.write("safe content".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        final File dest = new File(workingDir, "dest2");

        final PrearcImporter.UnpackDispatcher ud = new PrearcImporter.UnpackDispatcher();
        ud.add(".zip", new Unzipper());

        assertTrue(ud.unpack(zip, dest));
        assertTrue(new File(dest, "safe.txt").isFile());
    }

    @Test
    public final void testDispatcherReportsFailureForUnmatchedSuffix() {
        final File notAnArchive = new File(workingDir, "notes.txt");

        final PrearcImporter.UnpackDispatcher ud = new PrearcImporter.UnpackDispatcher();
        ud.add(".zip", new Unzipper());

        assertFalse(ud.unpack(notAnArchive, new File(workingDir, "dest3")));
    }
}
