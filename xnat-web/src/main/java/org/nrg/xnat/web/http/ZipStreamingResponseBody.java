package org.nrg.xnat.web.http;

import org.nrg.xft.utils.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Writes out a map of resources to a zip stream. This can be set as the response body for Spring controller and REST
 * API methods:
 *
 * {@code
 * return ResponseEntity.ok()
 * .header("Content-Type", "application/zip")
 * .header("Content-Disposition", "attachment; filename=\"filename.zip\"")
 * .body((StreamingResponseBody) new ZipStreamingResponseBody(resourceMap))}
 *
 * The map of resources should use the desired path of the resource in the resulting zip file, while the resource should
 * resolve to a retrievable item to be stored in the zip file.
 *
 * You can specify the size of the internal buffer used to transfer data from the resource to the zip stream by calling
 * the constructor with the buffer size parameter. If you call the other constructor, {@link FileUtils#LARGE_DOWNLOAD}
 * is used for the default size.
 */
public final class ZipStreamingResponseBody implements StreamingResponseBody {
    public static final String MEDIA_TYPE = "application/zip";

    /**
     * Initializes the instance with the map of resources. The map key should be the path to be used in the resulting
     * zip file, while the resource should resolve to a retrievable item to be stored in the zip file. This constructor
     * sets the size of the transfer buffer to {@link FileUtils#LARGE_DOWNLOAD}.
     *
     * @param resources The map of zip paths and corresponding resources.
     */
    public ZipStreamingResponseBody(final Map<String, Resource> resources) {
        this(resources, FileUtils.LARGE_DOWNLOAD);
    }

    /**
     * Initializes the instance with the map of resources. The map key should be the path to be used in the resulting
     * zip file, while the resource should resolve to a retrievable item to be stored in the zip file. This constructor
     * sets the size of the transfer buffer to the value specified for the buffer size parameter.
     *
     * @param resources  The map of zip paths and corresponding resources.
     * @param bufferSize The size to use for the transfer buffer.
     */
    public ZipStreamingResponseBody(final Map<String, Resource> resources, final int bufferSize) {
        _resources = resources;
        _buffer = new byte[bufferSize];
    }

    @Override
    public void writeTo(final OutputStream output) throws IOException {
        try (final ZipOutputStream zip = new ZipOutputStream(output)) {
            for (final String path : _resources.keySet()) {
                final Resource resource = _resources.get(path);
                final ZipEntry entry    = new ZipEntry(path);
                final File     file     = resource.getFile();
                entry.setSize(file.length());
                entry.setCrc(getCrc(file));
                entry.setTime(file.lastModified());

                zip.putNextEntry(entry);
                try (final InputStream input = new FileInputStream(file)) {
                    int len;
                    while ((len = input.read(_buffer)) > 0) {
                        zip.write(_buffer, 0, len);
                    }
                }
                zip.closeEntry();
            }
        }
        output.flush();
    }

    private static long getCrc(final File file) throws IOException {
        final CRC32 crc = new CRC32();
        try (final InputStream input = new BufferedInputStream(new FileInputStream(file))) {
            int count;
            while ((count = input.read()) != -1) {
                crc.update(count);
            }
        }
        return crc.getValue();
    }

    private final Map<String, Resource> _resources;
    private final byte[]                _buffer;
}
