package org.nrg.xnat.snapshot.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xnat.helpers.resource.XnatResourceInfoMap;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnat.snapshot.FileResource;
import org.nrg.xnat.snapshot.generator.SnapshotResourceGenerator;
import org.nrg.xnat.snapshot.services.SnapshotGenerationService;
import org.nrg.xnat.utils.CatalogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of SnapshotGenerationService.
 *
 */
@Service
@Slf4j
public class SnapshotGenerationServiceImpl implements SnapshotGenerationService {
    @Autowired
    public SnapshotGenerationServiceImpl(final CatalogService catalogService, final SnapshotResourceGenerator snapshotGenerator) {
        _catalogService = catalogService;
        _snapshotGenerator = snapshotGenerator;
    }

    @Override
    public Optional< FileResource> getSnapshot(final String sessionId, final String scanId, final int rows, final int cols) throws Exception {
        log.debug("Look for snapshot catalog for scan {} of session {} with {} rows by {} cols", sessionId, scanId, rows, cols);
        Optional<XnatResourcecatalog> snapshotCatalog = getSnapshotResourceCatalog( sessionId, scanId);
        String content = _snapshotGenerator.getSnapshotContentName( sessionId, scanId, rows, cols);
        if( snapshotCatalog.isPresent()) {
            Optional< FileResource> snapshot = getResourceFile( snapshotCatalog.get(), content);
            if( snapshot.isPresent()) {
                return snapshot;
            }
            else {
                snapshot = createSnapshot( sessionId, scanId, rows, cols);
                if( snapshot.isPresent()) {
                    addFileToResource( snapshotCatalog.get(), snapshot.get());
                    return getResourceFile( snapshotCatalog.get(), content);
                }
            }
        }
        else {
            Optional< FileResource> snapshot = createSnapshot( sessionId, scanId, rows, cols);
            if( snapshot.isPresent()) {
                snapshotCatalog = createSnapshotCatalog( sessionId, scanId);
                if( snapshotCatalog.isPresent()) {
                    addFileToResource( snapshotCatalog.get(), snapshot.get());
                    snapshotCatalog = getSnapshotResourceCatalog( sessionId, scanId);
                    return getResourceFile( snapshotCatalog.get(), content);
                }
            }
        }
        return Optional.ofNullable( null);
    }

    @Override
    public Optional< FileResource> getThumbnail(final String sessionId, final String scanId, final int rows, final int cols, float scaleRows, float scaleCols) throws Exception {
        log.debug("Look for snapshot catalog for scan {} of session {} with {} rows by {} cols", sessionId, scanId, rows, cols);
        Optional<XnatResourcecatalog> snapshotCatalog = getSnapshotResourceCatalog( sessionId, scanId);
        String content = _snapshotGenerator.getThumbnailContentName( sessionId, scanId, rows, cols);
        if( snapshotCatalog.isPresent()) {
            Optional< FileResource> thumbnail = getResourceFile( snapshotCatalog.get(), content);
            if( thumbnail.isPresent()) {
                return thumbnail;
            }
            else {
                thumbnail = createThumbnail( sessionId, scanId, rows, cols, scaleRows, scaleCols);
                if( thumbnail.isPresent()) {
                    addFileToResource( snapshotCatalog.get(), thumbnail.get());
                    return getResourceFile( snapshotCatalog.get(), content);
                }
            }
        }
        else {
            Optional< FileResource> thumbnail = createThumbnail( sessionId, scanId, rows, cols, scaleRows, scaleCols);
            if( thumbnail.isPresent()) {
                snapshotCatalog = createSnapshotCatalog( sessionId, scanId);
                if( snapshotCatalog.isPresent()) {
                    addFileToResource( snapshotCatalog.get(), thumbnail.get());
                    snapshotCatalog = getSnapshotResourceCatalog( sessionId, scanId);
                    return getResourceFile( snapshotCatalog.get(), content);
                }
            }
        }
        return Optional.ofNullable( null);
    }

    public Optional<XnatResourcecatalog> getSnapshotResourceCatalog(String sessionId, String scanId) throws Exception {
        return Optional.ofNullable( _catalogService.getResourceCatalog( sessionId, scanId, SNAPSHOTS));
    }

    public Optional<  FileResource> getResourceFile( XnatResourcecatalog snapshotCatalog, String content) throws Exception {
        String project = null;
        Path rootPath = Paths.get( snapshotCatalog.getUri()).getParent();
        CatalogUtils.CatalogData catalogData = CatalogUtils.CatalogData.getOrCreate( rootPath.toString(), snapshotCatalog, project);
        CatCatalogBean catalogBean = catalogData.catBean;
        List<CatEntryI> entries = catalogBean.getEntries_entry();

        Optional<FileResource> snapshotFile = entries.stream()
                .filter( e -> e.getContent().equals( content))
                .map( e -> new FileResource( rootPath.resolve( e.getUri()), e.getContent(), e.getFormat()))
                .findAny();

        if( snapshotFile.isPresent()) {
            log.debug("Matching resources found. Snapshot {}", snapshotFile.get());
            return Optional.of(snapshotFile.get());
        }
        else {
            log.debug("Matching resources not found.");
            return Optional.ofNullable( null);
        }
    }

    public Optional< FileResource> createSnapshot(String sessionId, String scanId, int rows, int cols) throws Exception {
        return _snapshotGenerator.createSnaphot( sessionId, scanId, rows, cols);
    }

    public Optional< FileResource> createThumbnail(String sessionId, String scanId, int rows, int cols, float scaleRows, float scaleCols) throws Exception {
        return _snapshotGenerator.createThumbnail( sessionId, scanId, rows, cols, scaleRows, scaleCols);
    }

    public void addFileToResource( XnatResourcecatalog resourcecatalog, FileResource fileResource) throws Exception {
        final XnatResourcecatalog updated = _catalogService.insertResources(XDAT.getUserDetails(), resourcecatalog,
                XnatResourceInfoMap.builder()
                        .resource(fileResource.getName(), fileResource.getFile(), fileResource.getFormat(), fileResource.getContent())
                        .build());
        FileUtils.deleteQuietly( fileResource.getFile());
    }

    private Optional<XnatResourcecatalog> createSnapshotCatalog(final String sessionId, final String scanId) {
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

    private static final String DICOM_RESOURCE     = "/resources/DICOM/files";
    private static final String SNAPSHOTS_RESOURCE = "/resources/SNAPSHOTS/files";
    private static final String ROOT_URI           = "/archive/experiments/";
    private static final String SNAPSHOTS          = "SNAPSHOTS";
    private static final String GIF                = "GIF";
    private static final String ORIGINAL           = "ORIGINAL";
    private static final String THUMBNAIL          = "THUMBNAIL";

    private final CatalogService _catalogService;
    private final SnapshotResourceGenerator _snapshotGenerator;
}
