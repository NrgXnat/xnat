package org.nrg.xft.utils.compression;

import java.io.*;
import java.nio.file.Path;
import java.util.zip.InflaterInputStream;

/**
 * File modulator/demodulator: Copy input stream to output stream with possible modifications on the streams.
 * Specify Input and Output Strategy that act to change a stream on copy.
 *
 * Main motivation is for compression and decompression.
 * Create a GZIP compressor with FileModem gzipCompressor = new FileModem( null, GZIPOutputStream::new). This creates
 * a FileModem that does not change input but decorates the output stream with a GZIPOutputStream.
 * Create a GZIP decompressor with FileModem gzipCompressor = new FileModem( GZIPInputStream::new, null). This creates
 * a FileModem that decompresses the inputstream but uses the undecorated outputStream to write the file.
 * In fact, there are convenience classes GZipCompressor and GzipDecompressor that do this.
 *
 */
public class FileModem {
    private final InputStrategy inputStrategy;
    private final OutputStrategy outputStategy;

    /**
     * Construct FileModem.
     * Specify functional interfaces that decorate IO streams.
     *
     * @param inputStrategy InputStrategy to decorate input stream. Null if inputstream is to be unchanged.
     * @param outputStrategy OutputStrategy to decorate output stream. Null if outputstream is to be unchanged.
     */
    public FileModem(final InputStrategy inputStrategy, final OutputStrategy outputStrategy) {
        this.inputStrategy = (inputStrategy == null)? new NullInputStrategy(): inputStrategy;
        this.outputStategy = (outputStrategy == null)? new NullOutputStategy(): outputStrategy;
    }

    public void process(final File inFile, final File outFile) throws IOException {
        if( ! outFile.getParentFile().exists()) {
            outFile.getParentFile().mkdirs();
        }
        process( new FileInputStream(inFile), new FileOutputStream( outFile));
    }

    public void process(final Path sourcePath, final Path destinationPath) throws IOException {
        process( sourcePath.toFile(), destinationPath.toFile());
    }

    public void process( final InputStream inputStream, final File outFile) throws IOException {
        if( ! outFile.getParentFile().exists()) {
            outFile.getParentFile().mkdirs();
        }
        process( inputStream, new FileOutputStream( outFile));
    }

    public void process( final InputStream inputStream, final OutputStream outputStream) throws IOException {
        try (InputStream dis = this.inputStrategy.decorate( inputStream);
             OutputStream dos = this.outputStategy.decorate( outputStream)) {
            copy(dis, dos);
            dos.flush();
        }
    }

    private void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buf = new byte[8192];
        int length;
        while ((length = inputStream.read(buf)) > 0) {
            outputStream.write(buf, 0, length);
        }
    }

    @FunctionalInterface
    public interface InputStrategy {
        InputStream decorate( InputStream is) throws IOException;
    }

    @FunctionalInterface
    public interface OutputStrategy {
        OutputStream decorate( OutputStream os) throws IOException;
    }

    private class NullInputStrategy implements InputStrategy {
        public InputStream decorate( InputStream is) { return is;}
    }

    private class NullOutputStategy implements OutputStrategy {
        public OutputStream decorate( OutputStream os) { return os;}
    }

}



