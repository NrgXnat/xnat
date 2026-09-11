package org.nrg.xft.utils.zip;

import com.google.common.io.Files;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.junit.Assume;
import org.junit.Test;
import org.nrg.xft.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;

public class ZipUtilsTest {
    private static final IOFileFilter NOT_FILE1_FILTER = new NotFileFilter(new NameFileFilter("file1.txt"));

    @Test
    public void extractZip() {
        try {
            File f = getFileResource("zip/files.zip");
            File f1 = getFileResource("zip/file1.txt");
            File f2 = getFileResource("zip/file2.txt");
            File f3 = getFileResource("zip/file3.txt");

            File dstDir = Files.createTempDir();
            ZipUtils.extractFile( f, dstDir.toPath());
            assertEquals( f1.length(), dstDir.toPath().resolve( "file1.txt").toFile().length());
            assertEquals( f2.length(), dstDir.toPath().resolve( "file2.txt").toFile().length());
            assertEquals( f3.length(), dstDir.toPath().resolve( "file3.txt").toFile().length());
            System.out.println( f);
        }
        catch( Exception e) {
            fail( "Unexpected execption: " + e);
        }
    }

    @Test
    public void extractZipWithFilter() {
        try {
            File f  = getFileResource("zip/files.zip");
            File f1 = getFileResource("zip/file1.txt");
            File f2 = getFileResource("zip/file2.txt");
            File f3 = getFileResource("zip/file3.txt");

            File dstDir = Files.createTempDir();
            try (final FileInputStream inputStream = new FileInputStream(f)) {
                final ZipUtils zip = new ZipUtils();
                zip.extract(inputStream, dstDir.getCanonicalPath(), true, null, NOT_FILE1_FILTER);
                assertFalse(dstDir.toPath().resolve("file1.txt").toFile().exists());
                assertEquals(f2.length(), dstDir.toPath().resolve("file2.txt").toFile().length());
                assertEquals(f3.length(), dstDir.toPath().resolve("file3.txt").toFile().length());
            }
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    @Test
    public void extractTar() {
        try {
            File f = getFileResource("zip/files.tar");
            File f1 = getFileResource("zip/file1.txt");
            File f2 = getFileResource("zip/file2.txt");
            File f3 = getFileResource("zip/file3.txt");

            File dstDir = Files.createTempDir();
            ZipUtils.extractFile( f, dstDir.toPath());
            assertEquals( f1.length(), dstDir.toPath().resolve( "file1.txt").toFile().length());
            assertEquals( f2.length(), dstDir.toPath().resolve( "file2.txt").toFile().length());
            assertEquals( f3.length(), dstDir.toPath().resolve( "file3.txt").toFile().length());
            System.out.println( f);
        }
        catch( Exception e) {
            fail( "Unexpected execption: " + e);
        }
    }

    @Test
    public void extractGzipTar() {
        try {
            File f = getFileResource("zip/files.tar.gz");
            File f1 = getFileResource("zip/file1.txt");
            File f2 = getFileResource("zip/file2.txt");
            File f3 = getFileResource("zip/file3.txt");

            File dstDir = Files.createTempDir();
            ZipUtils.extractFile( f, dstDir.toPath());
            assertEquals( f1.length(), dstDir.toPath().resolve( "file1.txt").toFile().length());
            assertEquals( f2.length(), dstDir.toPath().resolve( "file2.txt").toFile().length());
            assertEquals( f3.length(), dstDir.toPath().resolve( "file3.txt").toFile().length());
            System.out.println( f);
        }
        catch( Exception e) {
            fail( "Unexpected execption: " + e);
        }
    }

    /**
     * An archive built on a Unix box can record an entry as executable (e.g. via {@code chmod +x}) in the archive's
     * own metadata. Extraction must never let a written-out file inherit that bit -- see {@link FileUtils#clearExecutable}.
     */
    @Test
    public void extractZipClearsExecutableBit() throws Exception {
        final File dir        = Files.createTempDir();
        final File sourceFile = buildExecutableSourceFile(dir);

        final File zip = new File(dir, "archive.zip");
        try (final FileOutputStream fos = new FileOutputStream(zip)) {
            final ZipUtils writer = new ZipUtils();
            writer.setOutputStream(fos);
            writer.write("script.sh", sourceFile);
            writer.close();
        }

        final File dest = new File(dir, "dest");
        try (final FileInputStream fis = new FileInputStream(zip)) {
            new ZipUtils().extract(fis, dest.getAbsolutePath());
        }

        final File extracted = new File(dest, "script.sh");
        assertTrue(extracted.exists());
        assertFalse("extracted file must not be executable", extracted.canExecute());
    }

    @Test
    public void extractTarClearsExecutableBit() throws Exception {
        final File dir        = Files.createTempDir();
        final File sourceFile = buildExecutableSourceFile(dir);

        final File tar = new File(dir, "archive.tar");
        try (final FileOutputStream fos = new FileOutputStream(tar)) {
            final TarUtils writer = new TarUtils();
            writer.setOutputStream(fos);
            writer.write("script.sh", sourceFile);
            writer.close();
        }

        final File dest = new File(dir, "dest");
        try (final FileInputStream fis = new FileInputStream(tar)) {
            new TarUtils().extract(fis, dest.getAbsolutePath());
        }

        final File extracted = new File(dest, "script.sh");
        assertTrue(extracted.exists());
        assertFalse("extracted file must not be executable", extracted.canExecute());
    }

    /** Builds a file under {@code dir} and marks it executable, skipping the test if the file system can't do that. */
    private File buildExecutableSourceFile(final File dir) throws Exception {
        final File sourceFile = new File(dir, "script.sh");
        org.apache.commons.io.FileUtils.writeStringToFile(sourceFile, "#!/bin/sh\necho hi\n", StandardCharsets.UTF_8);
        Assume.assumeTrue("test requires a file system that supports the executable permission bit", sourceFile.setExecutable(true, false));
        return sourceFile;
    }

    /**
     * A zip containing an entry whose name resolves outside of the destination directory must be rejected outright
     * -- via {@link ZipUtils#extract(File, String, boolean)}, the entry point that used to delegate to Ant's Expand
     * task and, unlike every other extraction entry point, wasn't validated at all before this PR.
     */
    @Test
    public void extractFileRejectsPathTraversalEntry() throws Exception {
        final File dir = Files.createTempDir();
        final File maliciousZip = buildZipWithEntry(dir, "../evil.txt");
        final File dest = new File(dir, "dest");
        dest.mkdirs();

        try {
            new ZipUtils().extract(maliciousZip, dest.getAbsolutePath(), false);
            fail("Expected an UnsafeArchiveException");
        } catch (final UnsafeArchiveException expected) {
            // expected
        }

        assertFalse("the traversal entry must never be written outside the destination",
                    new File(dir, "evil.txt").exists());
    }

    /**
     * {@link ZipUtils#Unzip(File)} extracts into the zip file's own parent directory and, like {@link #extractFileRejectsPathTraversalEntry},
     * used to go through Ant's Expand task entirely unvalidated.
     */
    @Test
    public void unzipRejectsPathTraversalEntry() throws Exception {
        final File dir = Files.createTempDir();
        final File maliciousZip = buildZipWithEntry(dir, "../evil.txt");

        try {
            ZipUtils.Unzip(maliciousZip);
            fail("Expected an UnsafeArchiveException");
        } catch (final UnsafeArchiveException expected) {
            // expected
        }

        assertFalse("the traversal entry must never be written outside the destination",
                    new File(dir.getParentFile(), "evil.txt").exists());
    }

    /**
     * The buffered InputStream extraction path (used by, e.g., REST file uploads) must reject the same way.
     */
    @Test
    public void extractInputStreamRejectsPathTraversalEntry() throws Exception {
        final File dir = Files.createTempDir();
        final File maliciousZip = buildZipWithEntry(dir, "../evil.txt");
        final File dest = new File(dir, "dest");

        try (final FileInputStream fis = new FileInputStream(maliciousZip)) {
            new ZipUtils().extract(fis, dest.getAbsolutePath());
            fail("Expected an UnsafeArchiveException");
        } catch (final UnsafeArchiveException expected) {
            // expected
        }

        final String[] destContents = dest.list();
        assertTrue("nothing should be extracted once a traversal entry is found",
                   destContents == null || destContents.length == 0);
        assertFalse("the traversal entry must never be written outside the destination",
                    new File(dir, "evil.txt").exists());
    }

    /**
     * {@link TarUtils#extract(File, String, boolean)} (the direct File-based entry point used by e.g.
     * DicomInboxImporter) must keep its original plain-overwrite behaviour: extracting the same archive into the
     * same destination twice must simply overwrite the previous content in place, without throwing. Before this was
     * fixed, overwriting a pre-existing file here routed through FileUtils.MoveToHistory -- a side effect this entry
     * point never had before path-traversal validation was added -- which NullPointerExceptions outside of a Spring
     * context (see FileUtilsTest's isCanonicalPath/BuildRootHistoryPath coverage).
     */
    @Test
    public void extractFileTwiceOverwritesInPlace() throws Exception {
        final File dir = Files.createTempDir();
        final File tar = new File(dir, "archive.tar");
        try (final FileOutputStream fos = new FileOutputStream(tar)) {
            final TarUtils writer = new TarUtils();
            writer.setOutputStream(fos);
            final File source = new File(dir, "file1.txt");
            org.apache.commons.io.FileUtils.writeStringToFile(source, "second run", StandardCharsets.UTF_8);
            writer.write("file1.txt", source);
            writer.close();
        }

        final File dest = new File(dir, "dest");
        new TarUtils().extract(tar, dest.getAbsolutePath(), false);
        new TarUtils().extract(tar, dest.getAbsolutePath(), false);

        assertEquals("second run", org.apache.commons.io.FileUtils.readFileToString(new File(dest, "file1.txt"), StandardCharsets.UTF_8));
    }

    /**
     * A rejection must leave a written report describing why the archive was rejected (and, best-effort, attempt an
     * admin notification -- which just fails silently here, since there's no Spring context to send mail through)
     * in addition to throwing.
     */
    @Test
    public void rejectionWritesAReportBeforeThrowing() throws Exception {
        final File dir = Files.createTempDir();
        final File maliciousZip = buildZipWithEntry(dir, "../evil.txt");
        final File dest = new File(dir, "dest");

        final File rejectedUploadsRoot = new File(System.getProperty("java.io.tmpdir", "/tmp"), "RejectedUploads");
        final long before = System.currentTimeMillis();

        UnsafeArchiveException thrown = null;
        try (final FileInputStream fis = new FileInputStream(maliciousZip)) {
            new ZipUtils().extract(fis, dest.getAbsolutePath());
        } catch (final UnsafeArchiveException e) {
            thrown = e;
        }
        assertNotNull("extraction must throw when the archive is rejected", thrown);
        assertTrue(thrown.getMessage().contains("rejected"));

        final File[] rejectionFolders = rejectedUploadsRoot.listFiles();
        assertNotNull("rejectArchiveUpload must create " + rejectedUploadsRoot, rejectionFolders);
        final File newestReport = Arrays.stream(rejectionFolders)
                .filter(f -> f.lastModified() >= before)
                .map(f -> new File(f, "upload_report.txt"))
                .filter(File::isFile)
                .findFirst()
                .orElse(null);
        assertNotNull("a report describing the rejection must be written", newestReport);
        final String report = org.apache.commons.io.FileUtils.readFileToString(newestReport, StandardCharsets.UTF_8);
        assertTrue(report.contains("evil.txt"));
        assertTrue(report.contains("zip-slip"));
    }

    /** Builds a zip file under {@code dir} containing a single entry with the given (possibly unsafe) name. */
    private File buildZipWithEntry(final File dir, final String entryName) throws Exception {
        final File zip = new File(dir, "malicious.zip");
        try (final FileOutputStream fos = new FileOutputStream(zip);
             final ZipOutputStream zos = new ZipOutputStream(fos)) {
            zos.putNextEntry(new ZipEntry(entryName));
            zos.write("payload".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        return zip;
    }

    private File getFileResource( String fileName) throws URISyntaxException {
        return new File( getClass().getClassLoader().getResource( fileName).toURI());
    }

}
