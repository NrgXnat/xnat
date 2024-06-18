package org.nrg.xft.utils.compression;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.io.*;
import java.nio.file.Path;

/**
 * File modulator/demodulator: Copy input stream to output stream with possible modifications on the streams.
 * Specify Input and Output Strategy that act to change a stream on copy.
 *
 * Main motivation is for compression and decompression.
 * Create a GZIP compressor with FileModem gzipCompressor = new FileModem( null, GZIPOutputStream::new). This creates
 * a FileModem that does not change input but decorates the output stream with a GZIPOutputStream.
 * Create a GZIP decompressor with FileModem gzipCompressor = new FileModem( GZIPInputStream::new, null). This creates
 * a FileModem that decompresses the input stream but uses the undecorated outputStream to write the file.
 * In fact, there are convenience classes GZipCompressor and GzipDecompressor that do this.
 */
public class FileModem {
    private final InputStrategy  _inputStrategy;
    private final OutputStrategy _outputStrategy;

    /**
     * Construct FileModem.
     * Specify functional interfaces that decorate IO streams.
     *
     * @param inputStrategy  InputStrategy to decorate input stream. Null if input stream is to be unchanged.
     * @param outputStrategy OutputStrategy to decorate output stream. Null if output stream is to be unchanged.
     */
    public FileModem(final InputStrategy inputStrategy, final OutputStrategy outputStrategy) {
        _inputStrategy = ObjectUtils.defaultIfNull(inputStrategy, is -> is);
        _outputStrategy = ObjectUtils.defaultIfNull(outputStrategy, os -> os);
    }

    public void process(final File inFile, final File outFile) throws IOException {
        if (!outFile.getParentFile().exists()) {
            outFile.getParentFile().mkdirs();
        }
        process(new FileInputStream(inFile), new FileOutputStream(outFile));
    }

    public void process(final Path sourcePath, final Path destinationPath) throws IOException {
        process(sourcePath.toFile(), destinationPath.toFile());
    }

    public void process(final InputStream inputStream, final File outFile) throws IOException {
        if (!outFile.getParentFile().exists()) {
            outFile.getParentFile().mkdirs();
        }
        process(inputStream, new FileOutputStream(outFile));
    }

    public void process(final InputStream inputStream, final OutputStream outputStream) throws IOException {
        // Converting to reader/writer means channel is automatically flushed on close.
        try (final Reader dis = new InputStreamReader(_inputStrategy.decorate(inputStream));
             final Writer dos = new OutputStreamWriter(_outputStrategy.decorate(outputStream))) {
            IOUtils.copy(dis, dos);
        }
    }

    @FunctionalInterface
    public interface InputStrategy {
        InputStream decorate(InputStream is) throws IOException;
    }

    @FunctionalInterface
    public interface OutputStrategy {
        OutputStream decorate(OutputStream os) throws IOException;
    }
}



