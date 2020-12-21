package org.nrg.xnat.snapshot.services.impl;

import static org.nrg.xnat.snapshot.generator.SnapshotResourceGenerator.getSnapshotContentName;
import static org.nrg.xnat.snapshot.generator.SnapshotResourceGenerator.getThumbnailContentName;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.resource.XnatResourceInfoMap;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnat.snapshot.FileResource;
import org.nrg.xnat.snapshot.generator.SnapshotResourceGenerator;
import org.nrg.xnat.snapshot.services.SnapshotGenerationService;
import org.nrg.xnat.utils.CatalogUtils;
import org.restlet.data.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of SnapshotGenerationService.
 */
@Service
@Slf4j
public class SnapshotGenerationServiceImpl implements SnapshotGenerationService {
    @Autowired
    public SnapshotGenerationServiceImpl(final CatalogService catalogService, final SnapshotResourceGenerator snapshotGenerator, final XnatUserProvider primaryAdminUserProvider) {
        _catalogService = catalogService;
        _snapshotGenerator = snapshotGenerator;
        _provider = primaryAdminUserProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<FileResource> getSnapshot(final String sessionId, final String scanId, final int rows, final int cols) throws DataFormatException, NotFoundException, InitializationException, IOException {
        log.debug("Look for snapshot catalog for scan {} of session {} with {} rows by {} cols", sessionId, scanId, rows, cols);
        return createOrGetSnapshotOrThumbnail(sessionId, scanId, rows, cols, -1, -1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<FileResource> getThumbnail(final String sessionId, final String scanId, final int rows, final int cols, float scaleRows, float scaleCols) throws DataFormatException, NotFoundException, InitializationException, IOException {
        log.debug("Look for snapshot catalog for scan {} of session {} with {} rows by {} cols, scaling rows by {} and columns by {}", sessionId, scanId, rows, cols, scaleRows, scaleCols);
        return createOrGetSnapshotOrThumbnail(sessionId, scanId, rows, cols, scaleRows, scaleCols);
    }

    private Optional<FileResource> createOrGetSnapshotOrThumbnail(final String sessionId, final String scanId, final int rows, final int cols, float scaleRows, float scaleCols) throws DataFormatException, NotFoundException, InitializationException, IOException {
        final String lockCode = getLockCode(sessionId, scanId, rows, cols, scaleRows, scaleCols);
        synchronized (_locks) {
            if (!_locks.containsKey(lockCode)) {
                _locks.put(lockCode, new SnapshotGetter());
            }
        }
        final SnapshotGetter getter = _locks.get(lockCode);
        try {
            return getter.createOrGetSnapshotOrThumbnail(sessionId, scanId, rows, cols, scaleRows, scaleCols);
        } finally {
            if (!getter.isReferenced()) {
                _locks.remove(lockCode);
            }
        }
    }

    // TODO: For now this just returns the admin user so all resources will be created by admin until this can be better implemented.
    private UserI getResourceOwner(final String sessionId) {
        log.debug("Requested owner of image session {} but I'm just returning the admin user for now", sessionId);
        return getValidUser(XnatExperimentdata.getXnatExperimentdatasById(sessionId, _provider.get(), false));
    }

    // TODO: For now this just returns the admin user so all resources will be created by admin until this can be better implemented.
    private UserI getResourceOwner(final XnatResourcecatalog catalog) {
        log.debug("Requested owner of resource catalog {} but I'm just returning the admin user for now", catalog.getXnatAbstractresourceId());
        return getValidUser(catalog);
    }

    private UserI getValidUser(final BaseElement element) {
        final UserI insertUser = element.getInsertUser();
        if (insertUser != null) {
            return insertUser;
        }
        final UserI activateUser = element.getUser();
        if (activateUser != null) {
            return activateUser;
        }
        return _provider.get();
    }

    private static String getLockCode(final String sessionId, final String scanId, final int rows, final int cols, float scaleRows, float scaleCols) {
        return String.join(":", sessionId, scanId, Integer.toString(rows), Integer.toString(cols), Float.toString(scaleRows), Float.toString(scaleCols));
    }

    private class SnapshotGetter {
        boolean isReferenced() {
            return _references.get() > 0;
        }

        Optional<FileResource> createOrGetSnapshotOrThumbnail(final String sessionId, final String scanId, final int rows, final int cols, float scaleRows, float scaleCols) throws DataFormatException, NotFoundException, InitializationException, IOException {
            try {
                _references.incrementAndGet();
                final Optional<XnatResourcecatalog> catalog = getSnapshotResourceCatalog(sessionId, scanId);
                return !catalog.isPresent() ? Optional.empty() : getSnapshot(catalog.get(), sessionId, scanId, rows, cols, scaleRows, scaleCols);
            } finally {
                _references.decrementAndGet();
            }
        }

        Optional<XnatResourcecatalog> getSnapshotResourceCatalog(final String sessionId, final String scanId) throws DataFormatException {
            try {
                synchronized (this) {
                    final XnatResourcecatalog catalog = _catalogService.getResourceCatalog(sessionId, scanId, SNAPSHOTS);
                    if (catalog != null) {
                        return Optional.of(catalog);
                    }
                    final String parentUri = ROOT_URI + sessionId + "/scans/" + scanId + SNAPSHOTS_RESOURCE;
                    log.debug("Creating the snapshots folder for scan {} of session {} at URI {}", scanId, sessionId, parentUri);
                    try {
                        final XnatResourcecatalog created = _catalogService.createAndInsertResourceCatalog(getResourceOwner(sessionId), parentUri, 1, SNAPSHOTS, "Snapshots for session " + sessionId + " scan " + scanId, GIF, SNAPSHOTS);
                        log.debug("Created the snapshots folder for scan {} of session {} at URI {}", scanId, sessionId, UriParserUtils.getArchiveUri(created));
                        return Optional.of(created);
                    } catch (Exception e) {
                        log.error("An error occurred verifying the snapshots folder for scan {} of session {}", scanId, sessionId, e);
                        return Optional.empty();
                    }
                }
            } catch (ClientException e) {
                throw new DataFormatException("An error occurred trying to get the SNAPSHOTS resource catalog for session " + sessionId + " scan " + scanId, e);
            }
        }

        Optional<FileResource> getSnapshot(final XnatResourcecatalog catalog, final String sessionId, final String scanId, final int rows, final int cols, final float scaleRows, final float scaleCols) throws NotFoundException, InitializationException, IOException {
            synchronized (this) {
                final boolean isSnapshot = scaleRows < 0 && scaleCols < 0;
                final String  content    = isSnapshot ? getSnapshotContentName(rows, cols) : getThumbnailContentName(rows, cols);

                final Optional<FileResource> existing = getResourceFile(catalog, content);
                if (existing.isPresent()) {
                    return existing;
                }
                final Optional<FileResource> created = isSnapshot ? createSnapshot(sessionId, scanId, rows, cols) : createThumbnail(sessionId, scanId, rows, cols, scaleRows, scaleCols);
                if (!created.isPresent()) {
                    return Optional.empty();
                }
                addFileToResource(catalog, created.get());
                return getResourceFile(catalog, content);
            }
        }

        Optional<FileResource> getResourceFile(final XnatResourcecatalog snapshotCatalog, final String content) throws NotFoundException, InitializationException {
            try {
                final Path                     rootPath    = Paths.get(snapshotCatalog.getUri()).getParent();
                final CatalogUtils.CatalogData catalogData = CatalogUtils.CatalogData.getOrCreate(rootPath.toString(), snapshotCatalog, null);
                final CatCatalogBean           catalogBean = catalogData.catBean;
                final List<CatEntryI>          entries     = catalogBean.getEntries_entry();

                final Optional<FileResource> snapshotFile = entries.stream().filter(e -> StringUtils.equals(content, e.getContent())).map(e -> new FileResource(rootPath.resolve(e.getUri()), e.getContent(), e.getFormat())).findAny();
                snapshotFile.ifPresent(fileResource -> log.debug("Matching resources found. Snapshot {}", fileResource));
                return snapshotFile;
            } catch (ServerException e) {
                final Status status = e.getStatus();
                if (status == null) {
                    throw new NotFoundException(XnatResourcecatalog.SCHEMA_ELEMENT_NAME, snapshotCatalog.getXnatAbstractresourceId());
                }
                throw new InitializationException(e);
            }
        }

        Optional<FileResource> createSnapshot(String sessionId, String scanId, int rows, int cols) throws IOException, InitializationException {
            return _snapshotGenerator.createSnapshot(sessionId, scanId, rows, cols);
        }

        Optional<FileResource> createThumbnail(String sessionId, String scanId, int rows, int cols, float scaleRows, float scaleCols) throws IOException, InitializationException {
            return _snapshotGenerator.createThumbnail(sessionId, scanId, rows, cols, scaleRows, scaleCols);
        }

        void addFileToResource(final XnatResourcecatalog catalog, final FileResource resource) throws InitializationException {
            try {
                log.debug("Adding file {} to the catalog {}", resource, catalog.getUri());
                final XnatResourceInfoMap resourceInfoMap = XnatResourceInfoMap.builder().resource(resource.getName(), resource.getFile(), resource.getFormat(), resource.getContent()).build();
                _catalogService.insertResources(getResourceOwner(catalog), catalog, resourceInfoMap);
                FileUtils.deleteQuietly(resource.getFile());
            } catch (Exception e) {
                throw new InitializationException("An error occurred trying to add the file " + resource.getFile().getAbsolutePath(), e);
            }
        }

        private final AtomicInteger _references = new AtomicInteger();
    }

    private static final String SNAPSHOTS_RESOURCE = "/resources/SNAPSHOTS/files";
    private static final String ROOT_URI           = "/archive/experiments/";
    private static final String SNAPSHOTS          = "SNAPSHOTS";
    private static final String GIF                = "GIF";

    private final Map<String, SnapshotGetter> _locks = new HashMap<>();

    private final CatalogService            _catalogService;
    private final SnapshotResourceGenerator _snapshotGenerator;
    private final XnatUserProvider          _provider;
}
