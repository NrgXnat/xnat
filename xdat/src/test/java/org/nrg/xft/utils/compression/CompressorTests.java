package org.nrg.xft.utils.compression;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CompressorTests {

    @Test
    public void gzipTest() {
        try {
            File f1 = getFileResource("zip/file1.txt");
            File f2 = getFileResource("zip/file2.txt");
            File f3 = getFileResource("zip/file3.txt");

            Path dstDir = Files.createTempDirectory("tmp");
            GZipCompressor compressor = new GZipCompressor();
            GZipDecompressor decompressor = new GZipDecompressor();

            File g1out = dstDir.resolve( "f1.gz").toFile();
            File f1out = dstDir.resolve( "f1.txt").toFile();
            compressor.process( f1, g1out);
            decompressor.process( g1out, f1out);
            assertTrue("The files differ!", FileUtils.contentEquals(f1, f1out));

            File g2out = dstDir.resolve( "f2.gz").toFile();
            File f2out = dstDir.resolve( "f2.txt").toFile();
            compressor.process( f2, g2out);
            decompressor.process( g2out, f2out);
            assertTrue("The files differ!", FileUtils.contentEquals(f2, f2out));

            File g3out = dstDir.resolve( "f3.gz").toFile();
            File f3out = dstDir.resolve( "f3.txt").toFile();
            compressor.process( f3, g3out);
            decompressor.process( g3out, f3out);
            assertTrue("The files differ!", FileUtils.contentEquals(f3, f3out));

            System.out.println( g1out);
        }
        catch( Exception e) {
            fail( "Unexpected execption: " + e);
        }
    }

    private File getFileResource( String fileName) throws URISyntaxException {
        return new File( getClass().getClassLoader().getResource( fileName).toURI());
    }

}
