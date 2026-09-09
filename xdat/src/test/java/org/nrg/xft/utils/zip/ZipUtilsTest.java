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

    private File getFileResource( String fileName) throws URISyntaxException {
        return new File( getClass().getClassLoader().getResource( fileName).toURI());
    }

}
