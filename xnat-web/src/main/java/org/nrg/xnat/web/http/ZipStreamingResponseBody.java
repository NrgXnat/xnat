package org.nrg.xnat.web.http;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.model.CatCatalogI;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.services.archive.PathResourceMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.nio.file.Paths;
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
     * Initializes the instance with the path mapper. The map key should be the path to be used in the resulting
     * archive, while the resource should resolve to a retrievable item to be stored in the zip file. This constructor
     * sets the size of the transfer buffer to {@link FileUtils#LARGE_DOWNLOAD}.
     *
     * @param catalog     The catalog to zip and stream.
     * @param archiveRoot The root archive folder.
     */
    public ZipStreamingResponseBody(final UserI user, final CatCatalogI catalog, final String archiveRoot) {
        this(user, catalog, archiveRoot, FileUtils.LARGE_DOWNLOAD, false);
    }

    /**
     * Initializes the instance with the path mapper. The map key should be the path to be used in the resulting
     * archive, while the resource should resolve to a retrievable item to be stored in the zip file. This constructor
     * sets the size of the transfer buffer to {@link FileUtils#LARGE_DOWNLOAD}.
     *
     * @param catalog     The catalog to zip and stream.
     * @param archiveRoot The root archive folder.
     * @param testMode    Whether the zip should be run in test mode.
     */
    public ZipStreamingResponseBody(final UserI user, final CatCatalogI catalog, final String archiveRoot, final boolean testMode) {
        this(user, catalog, archiveRoot, FileUtils.SMALL_DOWNLOAD, testMode);
    }

    /**
     * Initializes the instance with the path mapper. The map key should be the path to be used in the resulting
     * archive, while the resource should resolve to a retrievable item to be stored in the zip file. This constructor
     * sets the size of the transfer buffer to {@link FileUtils#LARGE_DOWNLOAD}.
     *
     * @param catalog     The catalog to zip and stream.
     * @param archiveRoot The root archive folder.
     * @param bufferSize  The size of the buffer to use when writing files.
     * @param testMode    Whether the zip should be run in test mode.
     */
    public ZipStreamingResponseBody(final UserI user, final CatCatalogI catalog, final String archiveRoot, final int bufferSize, final boolean testMode) {
        _rootPath = StringUtils.defaultIfBlank(catalog.getId(), "");
        _mapper = new CatalogPathResourceMap(catalog, archiveRoot, testMode);
        _buffer = new byte[bufferSize];
        _history = Users.getUserCacheFile(user, "catalogs", catalog.getId() + ".csv");
    }

    @Override
    public void writeTo(final OutputStream output) throws IOException {
        try (final ZipOutputStream zip = new ZipOutputStream(output);
             final PrintWriter writer = new PrintWriter(_history)) {
            writer.println("Path,File,Size");
            while (_mapper.hasNext()) {
                final PathResourceMap.Mapping<String, Resource> map = _mapper.next();
                final String path = Paths.get(_rootPath, map.getPath()).toString();
                final File file = map.getResource().getFile();
                _log.info("Preparing to write zip entry to path {}: {}", path, file.getAbsolutePath());

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
                writer.println(path + "," + file.getAbsolutePath() + "," + total);
            }
            _log.info("Mapper has no more entries, processed {} total entries.", _mapper.getProcessedCount());
        }
        output.flush();
    }

    private static final Logger _log = LoggerFactory.getLogger(ZipStreamingResponseBody.class);

    private final String                            _rootPath;
    private final PathResourceMap<String, Resource> _mapper;
    private final byte[]                            _buffer;
    private final File                              _history;
}
