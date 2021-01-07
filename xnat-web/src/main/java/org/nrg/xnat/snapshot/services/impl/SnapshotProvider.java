package org.nrg.xnat.snapshot.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.XDAT;
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
import org.nrg.xnat.utils.CatalogUtils;
import org.restlet.data.Status;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SnapshotProvider: return existing snapshot or create it as necessary.
 *
 * This implementation can be called from multiple threads and tracks the number of requests it is serving.
 *
 */
@Slf4j
public class SnapshotProvider {
    private CatalogService _catalogService;
    private SnapshotResourceGenerator _snapshotResourceGenerator;
    private XnatUserProvider _provider;

    private final AtomicInteger _references = new AtomicInteger();

    private static final String SNAPSHOTS_RESOURCE = "/resources/SNAPSHOTS/files";
    private static final String ROOT_URI           = "/archive/experiments/";
    private static final String SNAPSHOTS          = "SNAPSHOTS";
    private static final String GIF                = "GIF";

    public SnapshotProvider(CatalogService catalogService, SnapshotResourceGenerator snapshotResourceGenerator, XnatUserProvider provider) {
        this._catalogService = catalogService;
        this._snapshotResourceGenerator = snapshotResourceGenerator;
        this._provider = provider;
    }

    public boolean isReferenced() {
        return _references.get() > 0;
    }

    /*
     * Do not create the resource catalog until there is something to put in it.
     */
    public Optional<FileResource> provideSnapshotOrThumbnail(final String sessionId, final String scanId, final int rows, final int cols, float scaleRows, float scaleCols) throws DataFormatException, NotFoundException, InitializationException, IOException {
        try {
            log.debug("Look for snapshot catalog for scan {} of session {} with {} rows by {} cols", sessionId, scanId, rows, cols);
            _references.incrementAndGet();
            Optional<XnatResourcecatalog> snapshotCatalog = getSnapshotResourceCatalog(sessionId, scanId);
            String content = SnapshotResourceGenerator.getContentName(rows, cols, scaleRows, scaleCols);
            if (snapshotCatalog.isPresent()) {
                Optional<FileResource> snapshot = getResourceFile(snapshotCatalog.get(), content);
                if (snapshot.isPresent()) {
                    return snapshot;
                } else {
                    snapshot = createSnapshot(sessionId, scanId, rows, cols, scaleRows, scaleCols);
                    if (snapshot.isPresent()) {
                        addFileToResource(snapshotCatalog.get(), snapshot.get());
                        return getResourceFile(snapshotCatalog.get(), content);
                    }
                }
            } else {
                Optional<FileResource> snapshot = createSnapshot(sessionId, scanId, rows, cols, scaleRows, scaleCols);
                if (snapshot.isPresent()) {
                    snapshotCatalog = createSnapshotResourceCatalog(sessionId, scanId);
                    if (snapshotCatalog.isPresent()) {
                        addFileToResource(snapshotCatalog.get(), snapshot.get());
                        snapshotCatalog = getSnapshotResourceCatalog(sessionId, scanId);
                        return getResourceFile(snapshotCatalog.get(), content);
                    }
                }
            }
            return Optional.ofNullable(null);
        }
        finally {
            _references.decrementAndGet();
        }
    }

    private Optional<FileResource> createSnapshot( final String sessionId, final String scanId, final int rows, final int cols, float scaleRows, float scaleCols) throws InitializationException, IOException {
        return (scaleRows < 0.0 || scaleCols < 0.0)?
                _snapshotResourceGenerator.createSnapshot(sessionId, scanId, rows, cols):
                _snapshotResourceGenerator.createThumbnail(sessionId, scanId, rows, cols, scaleRows, scaleCols);
    }

    private Optional<XnatResourcecatalog> createSnapshotResourceCatalog(final String sessionId, final String scanId) {
        final String parentUri = ROOT_URI + sessionId + "/scans/" + scanId + SNAPSHOTS_RESOURCE;
        log.debug("Creating the snapshots folder for scan {} of session {} at URI {}", scanId, sessionId, parentUri);
        try {
            String description = "Snapshots for session " + sessionId + " scan " + scanId;
            final XnatResourcecatalog catalog =
                    _catalogService.createAndInsertResourceCatalog(XDAT.getUserDetails(), parentUri, 1, SNAPSHOTS, description, GIF, SNAPSHOTS);
            log.debug("Created the snapshots folder for scan {} of session {} at URI {}", scanId, sessionId, UriParserUtils.getArchiveUri(catalog));
            return Optional.of(catalog);
        } catch (Exception e) {
            log.error("An error occurred verifying the snapshots folder for scan {} of session {}", scanId, sessionId, e);
            return Optional.empty();
        }
    }

    private Optional<XnatResourcecatalog> getSnapshotResourceCatalog(final String sessionId, final String scanId) throws DataFormatException {
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

    private Optional<FileResource> getResourceFile(final XnatResourcecatalog snapshotCatalog, final String content) throws NotFoundException, InitializationException {
        try {
            final Path rootPath    = Paths.get(snapshotCatalog.getUri()).getParent();
            final CatalogUtils.CatalogData catalogData = CatalogUtils.CatalogData.getOrCreate(rootPath.toString(), snapshotCatalog, null);
            final CatCatalogBean catalogBean = catalogData.catBean;
            final List<CatEntryI> entries     = catalogBean.getEntries_entry();

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

    private void addFileToResource(final XnatResourcecatalog catalog, final FileResource resource) throws InitializationException {
        try {
            log.debug("Adding file {} to the catalog {}", resource, catalog.getUri());
            final XnatResourceInfoMap resourceInfoMap = XnatResourceInfoMap.builder().resource(resource.getName(), resource.getFile(), resource.getFormat(), resource.getContent()).build();
            _catalogService.insertResources(getResourceOwner(catalog), catalog, resourceInfoMap);
            FileUtils.deleteQuietly(resource.getFile());
        } catch (Exception e) {
            throw new InitializationException("An error occurred trying to add the file " + resource.getFile().getAbsolutePath(), e);
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
        if (isValidUser(element, insertUser)) {
            return insertUser;
        }
        final UserI activateUser = element.getUser();
        if (isValidUser(element, activateUser)) {
            return activateUser;
        }
        return _provider.get();
    }

    private boolean isValidUser(final BaseElement element, final UserI user) {
        try {
            return user != null && element.canEdit(user);
        } catch (Exception ignored) {
            // Let's just assume the exception is permissions related...
            return false;
        }
    }
}
