package org.nrg.xnat.web.http;

import org.nrg.xdat.model.CatCatalogI;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.services.archive.PathResourceMap;
import org.springframework.core.io.Resource;

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
public final class CatalogZipStreamingResponseBody extends AbstractZipStreamingResponseBody {
    /**
     * Initializes the instance with the path mapper. The map key should be the path to be used in the resulting
     * archive, while the resource should resolve to a retrievable item to be stored in the zip file. This constructor
     * sets the size of the transfer buffer to {@link FileUtils#LARGE_DOWNLOAD}.
     *
     * @param user        The user requesting the catalog download.
     * @param catalog     The catalog to zip and stream.
     * @param archiveRoot The root archive folder.
     */
    @SuppressWarnings("unused")
    public CatalogZipStreamingResponseBody(final UserI user, final CatCatalogI catalog, final String archiveRoot) {
        this(user, catalog, archiveRoot, false);
    }

    /**
     * Initializes the instance with the path mapper. The map key should be the path to be used in the resulting
     * archive, while the resource should resolve to a retrievable item to be stored in the zip file. This constructor
     * sets the size of the transfer buffer to {@link FileUtils#LARGE_DOWNLOAD}.
     *
     * @param user        The user requesting the catalog download.
     * @param catalog     The catalog to zip and stream.
     * @param archiveRoot The root archive folder.
     * @param testMode    Whether the zip should be run in test mode.
     */
    public CatalogZipStreamingResponseBody(final UserI user, final CatCatalogI catalog, final String archiveRoot, final boolean testMode) {
        super(archiveRoot, DEFAULT_BUFFER_SIZE, Users.getUserCacheFile(user, "catalogs", catalog.getId() + ".csv"));
        _mapper = new CatalogPathResourceMap(catalog, archiveRoot, testMode);
    }

    @Override
    protected PathResourceMap<String, Resource> getResourceMap() {
        return _mapper;
    }

    private final PathResourceMap<String, Resource> _mapper;
}
