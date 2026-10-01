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
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.bzip2.CBZip2OutputStream;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarOutputStream;

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

    /**
     * A regular file with rwxr-xr-x permissions.
     */
    private final static int EXECUTABLE_FILE_MODE = 0100755;

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
     * Unzipper scans the whole archive before extracting anything (the same atomic scan-then-extract guarantee
     * ZipUtils/TarUtils already provide elsewhere in this project), so the safe entry must not be extracted
     * either -- not just the malicious one rejected.
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
        assertFalse("no entry may be extracted when the archive also contains a path-traversal entry -- the "
                    + "whole upload is rejected atomically, not truncated at the malicious entry",
                    new File(dest, "safe.txt").exists());
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

    /**
     * Ant's Untar (1.9.12+/1.10.4+) refuses to write a path-traversal entry itself, but skips it silently and
     * still reports success. Untarrer must instead scan first and reject the whole upload, exactly as Unzipper
     * does -- for every tar compression the dispatcher routes to it.
     */
    @Test
    public final void testDispatcherReportsFailureForMaliciousTar() throws Exception {
        assertTarRejected("mixed.tar");
    }

    @Test
    public final void testDispatcherReportsFailureForMaliciousTarGz() throws Exception {
        assertTarRejected("mixed.tar.gz");
    }

    @Test
    public final void testDispatcherReportsFailureForMaliciousTgz() throws Exception {
        assertTarRejected("mixed.tgz");
    }

    @Test
    public final void testDispatcherReportsFailureForMaliciousTarBz2() throws Exception {
        assertTarRejected("mixed.tar.bz2");
    }

    /**
     * The scan must read clean archives of every compression correctly too, or it would reject (or fail on)
     * legitimate uploads.
     */
    @Test
    public final void testDispatcherReportsSuccessForCleanTars() throws Exception {
        for (final String name : new String[]{"clean.tar", "clean.tar.gz", "clean.tgz", "clean.tar.bz2"}) {
            final File tar  = writeTar(name, "safe.txt", "subdir/safe2.txt");
            final File dest = new File(workingDir, "dest-" + name);

            assertTrue(name + " should unpack successfully", createDispatcher().unpack(tar, dest));
            assertTrue(name + ": safe.txt should be extracted", new File(dest, "safe.txt").isFile());
            assertTrue(name + ": subdir/safe2.txt should be extracted", new File(dest, "subdir/safe2.txt").isFile());
        }
    }

    /**
     * An extracted file must never inherit an executable mode recorded in the tar, the same as every other
     * extraction entry point (Unzipper, ZipUtils, TarUtils).
     */
    @Test
    public final void testTarExtractionClearsExecutableBit() throws Exception {
        for (final String name : new String[]{"exec.tar", "exec.tar.gz", "exec.tgz", "exec.tar.bz2"}) {
            final File tar  = writeTar(name, EXECUTABLE_FILE_MODE, "script.sh", "subdir/script2.sh");
            final File dest = new File(workingDir, "dest-" + name);

            assertTrue(name + " should unpack successfully", createDispatcher().unpack(tar, dest));
            for (final String entryName : new String[]{"script.sh", "subdir/script2.sh"}) {
                final File extracted = new File(dest, entryName);
                assertTrue(name + ": " + entryName + " should be extracted", extracted.isFile());
                assertFalse(name + ": " + entryName + " must not be executable", extracted.canExecute());
            }
        }
    }

    @Test
    public final void testDispatcherReportsFailureForUnmatchedSuffix() {
        final File notAnArchive = new File(workingDir, "notes.txt");

        final PrearcImporter.UnpackDispatcher ud = new PrearcImporter.UnpackDispatcher();
        ud.add(".zip", new Unzipper());

        assertFalse(ud.unpack(notAnArchive, new File(workingDir, "dest3")));
    }

    private void assertTarRejected(final String name) throws Exception {
        final File tar  = writeTar(name, "safe.txt", "../evil.txt", "a/../../evil2.txt");
        final File dest = new File(workingDir, "dest");

        assertFalse(name + ": a mix of safe entries and path-traversal entries must not be reported as success",
                    createDispatcher().unpack(tar, dest));
        assertFalse(name + ": ../evil.txt must never be written outside the destination",
                    new File(workingDir, "evil.txt").exists());
        assertFalse(name + ": a/../../evil2.txt must never be written outside the destination",
                    new File(workingDir, "evil2.txt").exists());
        assertFalse(name + ": no entry may be extracted when the archive also contains a path-traversal entry",
                    new File(dest, "safe.txt").exists());
        assertTrue(name + ": a rejected upload must not be deleted as though it had been unpacked", tar.isFile());
    }

    /**
     * Builds the same suffix-to-unpacker mapping {@link PrearcImporter#run()} uses for archives.
     */
    private PrearcImporter.UnpackDispatcher createDispatcher() {
        final PrearcImporter.UnpackDispatcher ud = new PrearcImporter.UnpackDispatcher();
        ud.add(".tar", new Untarrer("none", project));
        ud.add(".tar.gz", new Untarrer("gzip", project));
        ud.add(".tgz", new Untarrer("gzip", project));
        ud.add(".tar.bz2", new Untarrer("bzip2", project));
        ud.add(".zip", new Unzipper());
        return ud;
    }

    /**
     * Writes a tar, compressed according to its suffix, containing one small text entry per name, in order.
     */
    private File writeTar(final String name, final String... entryNames) throws Exception {
        return writeTar(name, TarEntry.DEFAULT_FILE_MODE, entryNames);
    }

    /**
     * Writes a tar as {@link #writeTar(String, String...)} does, giving every entry the specified Unix mode.
     */
    private File writeTar(final String name, final int mode, final String... entryNames) throws Exception {
        final File tar = new File(workingDir, name);
        try (final OutputStream fos = new FileOutputStream(tar);
             final TarOutputStream tos = new TarOutputStream(compress(name, fos))) {
            for (final String entryName : entryNames) {
                final byte[]   content = ("content of " + entryName).getBytes(StandardCharsets.UTF_8);
                final TarEntry entry   = new TarEntry(entryName);
                entry.setSize(content.length);
                entry.setMode(mode);
                tos.putNextEntry(entry);
                tos.write(content);
                tos.closeEntry();
            }
        }
        return tar;
    }

    private static OutputStream compress(final String name, final OutputStream out) throws Exception {
        if (name.endsWith(".tar.gz") || name.endsWith(".tgz")) {
            return new GZIPOutputStream(out);
        }
        if (name.endsWith(".tar.bz2")) {
            // Ant's bzip2 streams omit the "BZ" magic number, which Untar expects to read (and skip) itself.
            out.write('B');
            out.write('Z');
            return new CBZip2OutputStream(out);
        }
        return out;
    }
}
