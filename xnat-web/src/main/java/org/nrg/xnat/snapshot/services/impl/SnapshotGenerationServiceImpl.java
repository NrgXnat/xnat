package org.nrg.xnat.snapshot.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xnat.helpers.resource.XnatResourceInfoMap;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnat.snapshot.generator.SnapshotResourceGenerator;
import org.nrg.xnat.snapshot.generator.impl.SnapshotResourceGeneratorImpl;
import org.nrg.xnat.snapshot.services.SnapshotGenerationService;
import org.nrg.xnat.utils.CatalogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of SnapshotGenerationService.
 *
 */
@Service
@Slf4j
public class SnapshotGenerationServiceImpl implements SnapshotGenerationService {
    @Autowired
//    public SnapshotGenerationServiceImpl(final CatalogService catalogService, final SnapshotResourceGenerator snapshotGenerator) {
    public SnapshotGenerationServiceImpl(final CatalogService catalogService) throws IOException {
        _catalogService = catalogService;
        _snapshotGenerator = new SnapshotResourceGeneratorImpl( _catalogService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pair<File, File> getSnapshot(final String sessionId, final String scanId, final int rows, final int cols) throws Exception {
        log.debug("Look for snapshot catalog for scan {} of session {} with {} rows by {} cols", sessionId, scanId, rows, cols);
        Optional<XnatResourcecatalog> snapshotCatalog = getSnapshotResourceCatalog( sessionId, scanId);
        if( snapshotCatalog.isPresent()) {
            Optional< Pair<File,File>> snapshots = getSnapshotFiles( snapshotCatalog.get(), sessionId, scanId, rows, cols);
            if( snapshots.isPresent()) {
                return snapshots.get();
            }
            else {
                snapshots = createSnapshots( sessionId, scanId, rows, cols);
                if( snapshots.isPresent()) {
                    addSnapshotFilesToResource( snapshotCatalog.get(), snapshots.get().getLeft(), snapshots.get().getRight());
                    snapshots = getSnapshotFiles( snapshotCatalog.get(), sessionId, scanId, rows, cols);
                    if( snapshots.isPresent()) {
                        return snapshots.get();
                    }
                }
            }
        }
        else {
            Optional< Pair<File,File>> snapshots = createSnapshots( sessionId, scanId, rows, cols);
            if( snapshots.isPresent()) {
                snapshotCatalog = createSnapshotCatalog( sessionId, scanId);
                if( snapshotCatalog.isPresent()) {
                    addSnapshotFilesToResource( snapshotCatalog.get(), snapshots.get().getLeft(), snapshots.get().getRight());
                    snapshotCatalog = getSnapshotResourceCatalog( sessionId, scanId);
                    snapshots = getSnapshotFiles( snapshotCatalog.get(), sessionId, scanId, rows, cols);
                    if( snapshots.isPresent()) {
                        return snapshots.get();
                    }
                }
            }
        }
        return null;
    }

    public Optional<XnatResourcecatalog> getSnapshotResourceCatalog(String sessionId, String scanId) throws Exception {
        return Optional.ofNullable( _catalogService.getResourceCatalog( sessionId, scanId, SNAPSHOTS));
    }

    public Optional< Pair<File, File>> getSnapshotFiles( XnatResourcecatalog snapshotCatalog, String sessionId, String scanId, int rows, int cols) throws Exception {
        String project = null;
        Path rootPath = Paths.get( snapshotCatalog.getUri()).getParent();
        CatalogUtils.CatalogData catalogData = CatalogUtils.CatalogData.getOrCreate( rootPath.toString(), snapshotCatalog, project);
        CatCatalogBean catalogBean = catalogData.catBean;
        List<CatEntryI> entries = catalogBean.getEntries_entry();

        List<String> resourceNames = entries.stream().map( CatEntryI::getUri).collect(Collectors.toList());
        
        Pair<String, String> snaphotFileNames = _snapshotGenerator.getResourceNames( sessionId, scanId, rows, cols);

        Optional<File> snapshotFile = resourceNames.stream()
                .filter( f -> snaphotFileNames.getLeft().equals(f))
                .map( f -> rootPath.resolve( f).toFile())
                .findAny();
        Optional<File> thumbnailFile = resourceNames.stream()
                .filter( f -> snaphotFileNames.getRight().equals(f))
                .map( f -> rootPath.resolve( f).toFile())
                .findAny();
        if( snapshotFile.isPresent() && thumbnailFile.isPresent()) {
            log.debug("Matching resources found. Snapshot {} and thumbnail {}", snapshotFile.get(), thumbnailFile.get());
            Pair<File, File> pair = ImmutablePair.of( snapshotFile.get(), thumbnailFile.get());
            return Optional.of( pair);
        }
        else {
            log.debug("Matching resources not found.");
            return Optional.ofNullable( null);
        }
    }

    public Optional<Pair<File, File>> createSnapshots( String sessionId, String scanId, int rows, int cols) throws Exception {
        return Optional.ofNullable( _snapshotGenerator.setScan( sessionId, scanId).createMontageAndThumbnail( rows, cols, 0.5f, 0.5f));
    }

    public void addSnapshotFilesToResource( XnatResourcecatalog resourcecatalog, File snapshotFile, File thumbnailFile) throws Exception {
        final XnatResourcecatalog updated = _catalogService.insertResources(XDAT.getUserDetails(), resourcecatalog,
                XnatResourceInfoMap.builder()
                        .resource(snapshotFile.getName(), snapshotFile, GIF, ORIGINAL)
                        .resource(thumbnailFile.getName(), thumbnailFile, GIF, THUMBNAIL)
                        .build());
        FileUtils.deleteQuietly(snapshotFile);
        FileUtils.deleteQuietly(thumbnailFile);
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
