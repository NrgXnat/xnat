package org.nrg.xft.utils.fileExtraction;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.nrg.xft.utils.compression.GZipCompressor;

import java.io.File;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FileExtractorTests {

    @Test
    public void gzTest() {

        try {
            File f1 = getFileResource("zip/file1.txt");
            File f2 = getFileResource("zip/file2.txt");
            File f3 = getFileResource("zip/file3.txt");

            Path dstDir = Files.createTempDirectory("tmp");
            GZipCompressor compressor = new GZipCompressor();
            FileExtractor extractor = new FileExtractor();

            Path g1out = dstDir.resolve( "f1.gz");
            Path f1out = dstDir.resolve( "foo/bar/f1");
            compressor.process( f1, g1out.toFile());
            extractor.extract( g1out, f1out.getParent(), true, null);
            assertTrue("The files are different.", FileUtils.contentEquals(f1, f1out.toFile()));

            Path g2out = dstDir.resolve( "f2.gz");
            Path f2out = dstDir.resolve( "foo/baz/f2");
            compressor.process( f2, g2out.toFile());
            extractor.extract( g2out, f2out.getParent(), true, null);
            assertTrue("The files are different.", FileUtils.contentEquals(f2, f2out.toFile()));

            Path g3out = dstDir.resolve( "f3.gz");
            Path f3out = dstDir.resolve( "f3");
            compressor.process( f3, g3out.toFile());
            extractor.extract( g3out, f3out.getParent(), true, null);
            assertTrue("The files are different.", FileUtils.contentEquals(f3, f3out.toFile()));

            System.out.println( g1out);
        }
        catch( Exception e) {
            fail( "Unexpected execption: " + e);
        }
    }

    @Test
    public void gzTest2() {

        try {
            File f1 = getFileResource("zip/file1.txt");
            File f2 = getFileResource("zip/file2.txt");
            File f3 = getFileResource("zip/file3.txt");

            Path dstDir = Files.createTempDirectory("tmp");
            GZipCompressor compressor = new GZipCompressor();
            FileExtractor extractor = new FileExtractor();

            Path g1out = dstDir.resolve( "f1.gz");
            Path f1out = dstDir.resolve( "foo/bar/f1");
            compressor.process( f1, g1out.toFile());
            extractor.extract( "f1.gz", new FileInputStream( g1out.toFile()), f1out.getParent(), true, null);
            assertTrue("The files are different.", FileUtils.contentEquals(f1, f1out.toFile()));

            Path g2out = dstDir.resolve( "f2.gz");
            Path f2out = dstDir.resolve( "foo/baz/f2");
            compressor.process( f2, g2out.toFile());
            extractor.extract( "f2.gz", new FileInputStream( g2out.toFile()), f2out.getParent(), true, null);
            assertTrue("The files are different.", FileUtils.contentEquals(f2, f2out.toFile()));

            Path g3out = dstDir.resolve( "f3.gz");
            Path f3out = dstDir.resolve( "f3");
            compressor.process( f3, g3out.toFile());
            extractor.extract( "f3.gz", new FileInputStream( g3out.toFile()), f3out.getParent(), true, null);
            assertTrue("The files are different.", FileUtils.contentEquals(f3, f3out.toFile()));

            System.out.println( g1out);
        }
        catch( Exception e) {
            fail( "Unexpected execption: " + e);
        }
    }

    @Test
    public void tarGzTest() {

        try {
            File tgz = getFileResource("zip/files.tar.gz");
            File f1 = getFileResource("zip/file1.txt");
            File f2 = getFileResource("zip/file2.txt");
            File f3 = getFileResource("zip/file3.txt");

            Path dstDir = Files.createTempDirectory("tmp");
            FileExtractor extractor = new FileExtractor();

            Path f1out = dstDir.resolve( "foo/bar/file1.txt");
            Path f2out = dstDir.resolve( "foo/bar/file2.txt");
            Path f3out = dstDir.resolve( "foo/bar/file3.txt");
            extractor.extract( "files.tar.gz", new FileInputStream( tgz), f1out.getParent(), true, null);
            assertTrue("The files are different.", FileUtils.contentEquals(f1, f1out.toFile()));
            assertTrue("The files are different.", FileUtils.contentEquals(f2, f2out.toFile()));
            assertTrue("The files are different.", FileUtils.contentEquals(f3, f3out.toFile()));
        }
        catch( Exception e) {
            fail( "Unexpected execption: " + e);
        }
    }

    @Test
    public void tarTest() {

        try {
            File tgz = getFileResource("zip/files.tar");
            File f1 = getFileResource("zip/file1.txt");
            File f2 = getFileResource("zip/file2.txt");
            File f3 = getFileResource("zip/file3.txt");

            Path dstDir = Files.createTempDirectory("tmp");
            FileExtractor extractor = new FileExtractor();

            Path f1out = dstDir.resolve( "foo/file1.txt");
            Path f2out = dstDir.resolve( "foo/file2.txt");
            Path f3out = dstDir.resolve( "foo/file3.txt");
            extractor.extract( "files.tar", new FileInputStream( tgz), f1out.getParent(), true, null);
            assertTrue("The files are different.", FileUtils.contentEquals(f1, f1out.toFile()));
            assertTrue("The files are different.", FileUtils.contentEquals(f2, f2out.toFile()));
            assertTrue("The files are different.", FileUtils.contentEquals(f3, f3out.toFile()));
        }
        catch( Exception e) {
            fail( "Unexpected execption: " + e);
        }
    }

    @Test
    public void zipTest() {

        try {
            File tgz = getFileResource("zip/files.zip");
            File f1 = getFileResource("zip/file1.txt");
            File f2 = getFileResource("zip/file2.txt");
            File f3 = getFileResource("zip/file3.txt");

            Path dstDir = Files.createTempDirectory("tmp");
            FileExtractor extractor = new FileExtractor();

            Path f1out = dstDir.resolve( "foo/file1.txt");
            Path f2out = dstDir.resolve( "foo/file2.txt");
            Path f3out = dstDir.resolve( "foo/file3.txt");
            extractor.extract( "files.zip", new FileInputStream( tgz), f1out.getParent(), true, null);
            assertTrue("The files are different.", FileUtils.contentEquals(f1, f1out.toFile()));
            assertTrue("The files are different.", FileUtils.contentEquals(f2, f2out.toFile()));
            assertTrue("The files are different.", FileUtils.contentEquals(f3, f3out.toFile()));
        }
        catch( Exception e) {
            fail( "Unexpected execption: " + e);
        }
    }

    private File getFileResource( String fileName) throws URISyntaxException {
        return new File( getClass().getClassLoader().getResource( fileName).toURI());
    }

}
