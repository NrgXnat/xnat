package org.nrg.xnat.web.http;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.services.archive.PathResourceMap;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Writes out a map of resources to a zip stream. This can be set as the response body for Spring controller and REST
 * API methods:
 * <p>
 * {@code
 * return ResponseEntity.ok()
 * .header("Content-Type", "application/zip")
 * .header("Content-Disposition", "attachment; filename=\"filename.zip\"")
 * .body((StreamingResponseBody) new AbstractZipStreamingResponseBody(resourceMap))}
 * <p>
 * The map of resources should use the desired path of the resource in the resulting zip file, while the resource should
 * resolve to a retrievable item to be stored in the zip file.
 * <p>
 * You can specify the size of the internal buffer used to transfer data from the resource to the zip stream by calling
 * the constructor with the buffer size parameter. If you call the other constructor, {@link FileUtils#LARGE_DOWNLOAD}
 * is used for the default size.
 */
@Slf4j
public abstract class AbstractZipStreamingResponseBody implements StreamingResponseBody {
    public static final String MEDIA_TYPE          = "application/zip";
    public static final int    DEFAULT_BUFFER_SIZE = FileUtils.LARGE_DOWNLOAD;

    /**
     * Initializes the instance with the path mapper. The map key should be the path to be used in the resulting
     * archive, while the resource should resolve to a retrievable item to be stored in the zip file. This constructor
     * sets the root path to empty (that is, all entries in the zip file are mapped to the path specified in {@link #getResourceMap()
     * resource map entry}) and the transfer buffer size to {@link #DEFAULT_BUFFER_SIZE}.
     */
    @SuppressWarnings("unused")
    public AbstractZipStreamingResponseBody() {
        this(Paths.get(""), DEFAULT_BUFFER_SIZE, null);
    }

    /**
     * Initializes the instance with the path mapper. The map key should be the path to be used in the resulting
     * archive, while the resource should resolve to a retrievable item to be stored in the zip file. This constructor
     * sets the size of the transfer buffer to {@link #DEFAULT_BUFFER_SIZE}.
     *
     * @param rootPath The root path for resolving resources.
     */
    @SuppressWarnings("unused")
    public AbstractZipStreamingResponseBody(final String rootPath) {
        this(Paths.get(rootPath), DEFAULT_BUFFER_SIZE, null);
    }

    /**
     * Initializes the instance with the path mapper. The map key should be the path to be used in the resulting
     * archive, while the resource should resolve to a retrievable item to be stored in the zip file. This constructor
     * sets the size of the transfer buffer to {@link #DEFAULT_BUFFER_SIZE}.
     *
     * @param rootPath The root path for resolving resources.
     */
    @SuppressWarnings("unused")
    public AbstractZipStreamingResponseBody(final Path rootPath) {
        this(rootPath, DEFAULT_BUFFER_SIZE, null);
    }

    /**
     * Initializes the instance with the path mapper. The map key should be the path to be used in the resulting
     * archive, while the resource should resolve to a retrievable item to be stored in the zip file. This constructor
     * sets the size of the transfer buffer to {@link FileUtils#LARGE_DOWNLOAD}.
     *
     * @param rootPath   The root path for resolving resources.
     * @param bufferSize The size of the buffer to use when writing files.
     */
    @SuppressWarnings("unused")
    public AbstractZipStreamingResponseBody(final String rootPath, final int bufferSize) {
        this(Paths.get(rootPath), bufferSize, null);
    }

    /**
     * Initializes the instance with the path mapper. The map key should be the path to be used in the resulting
     * archive, while the resource should resolve to a retrievable item to be stored in the zip file. This constructor
     * sets the size of the transfer buffer to {@link FileUtils#LARGE_DOWNLOAD}. The history is written as a CSV file
     * with the zip entry path, full path of the file written to the zip entry, and the total number of bytes written.
     * If the history file isn't set, the history won't be recorded, but errors and warnings are still logged in the
     * standard log files.
     *
     * @param rootPath   The root path for resolving resources.
     * @param bufferSize The size of the buffer to use when writing files.
     * @param history    The file where the zip write history should recorded.
     */
    public AbstractZipStreamingResponseBody(final String rootPath, final int bufferSize, final File history) {
        this(Paths.get(rootPath), bufferSize, history);
    }

    /**
     * Initializes the instance with the path mapper. The map key should be the path to be used in the resulting
     * archive, while the resource should resolve to a retrievable item to be stored in the zip file. This constructor
     * sets the size of the transfer buffer to {@link FileUtils#LARGE_DOWNLOAD}. The history is written as a CSV file
     * with the zip entry path, full path of the file written to the zip entry, and the total number of bytes written.
     * If the history file isn't set, the history won't be recorded, but errors and warnings are still logged in the
     * standard log files.
     *
     * @param rootPath   The root path for resolving resources.
     * @param bufferSize The size of the buffer to use when writing files.
     * @param history    The file where the zip write history should recorded.
     */
    public AbstractZipStreamingResponseBody(final Path rootPath, final int bufferSize, final File history) {
        _rootPath = rootPath;
        _buffer = new byte[bufferSize];
        try {
            _history = history != null ? new PrintWriter(history) : null;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Couldn't find the specified history file " + history.getAbsolutePath(), e);
        }
    }

    protected abstract PathResourceMap<String, Resource> getResourceMap();

    @Override
    public void writeTo(final OutputStream output) throws IOException {
        startHistory();
        try (final ZipOutputStream zip = new ZipOutputStream(output)) {
            while (getResourceMap().hasNext()) {
                final PathResourceMap.Mapping<String, Resource> map  = getResourceMap().next();
                final String                                    path = _rootPath.resolve(map.getPath()).toString();
                final File                                      file = map.getResource().getFile();
                log.info("Preparing to write zip entry to path {}: {}", path, file.getAbsolutePath());

                final ZipEntry entry = new ZipEntry(path);
                entry.setTime(file.lastModified());

                long total = 0;
                zip.putNextEntry(entry);
                try (final InputStream input = new FileInputStream(file)) {
                    int len;
                    while ((len = input.read(_buffer)) > 0) {
                        zip.write(_buffer, 0, len);
                        total += len;
                    }
                }
                zip.closeEntry();
                writeHistory(path, file, total);
            }
            log.info("Mapper has no more entries, processed {} total entries.", getResourceMap().getProcessedCount());
        } finally {
            output.flush();
            endHistory();
        }
    }

    private void startHistory() {
        if (_history != null) {
            _history.println("Path,File,Size");
        }
    }

    private void writeHistory(final String path, final File file, final long total) {
        if (_history != null) {
            _history.println(path + "," + file.getAbsolutePath() + "," + total);
        }
    }

    private void endHistory() {
        if (_history != null) {
            _history.close();
        }
    }

    private final Path        _rootPath;
    private final byte[]      _buffer;
    private final PrintWriter _history;
}
