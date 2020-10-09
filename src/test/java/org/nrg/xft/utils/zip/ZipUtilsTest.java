package org.nrg.xft.utils.zip;

import com.google.common.io.Files;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class ZipUtilsTest {

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

    private File getFileResource( String fileName) throws URISyntaxException {
        return new File( getClass().getClassLoader().getResource( fileName).toURI());
    }

}
