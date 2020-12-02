package org.nrg.xnat.snapshot.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementation of SnapshotGenerationService.
 *
 */
@Service
@Slf4j
public class SnapshotGenerationServiceJava8Impl  {
////    public class SnapshotGenerationServiceJava8Impl implements SnapshotGenerationService {
//    @Autowired
////    public SnapshotGenerationServiceImpl(final CatalogService catalogService, final SnapshotResourceGenerator snapshotGenerator) {
//    public SnapshotGenerationServiceJava8Impl(final CatalogService catalogService) throws IOException {
//        _catalogService = catalogService;
//        _snapshotGenerator = new SnapshotResourceGeneratorImpl( _catalogService);
//    }
//
//    @Override
//    public Optional<Pair<File, File>> getSnapshot(final String sessionId, final String scanId, final int rows, final int cols, float scaleRows, float scaleCols) {
//        log.debug("Look for snapshot catalog for scan {} of session {} with {} rows by {} cols", sessionId, scanId, rows, cols);
//        Optional<XnatResourcecatalog> snapshotCatalog = getSnapshotResourceCatalog(sessionId, scanId);
//        Optional<Pair<File, File>> pair = snapshotCatalog
//                .map(sc -> {
//                    getSnapshotFiles( sc, sessionId, scanId, rows, cols)
//                            .orElseGet(() -> {
//                                Optional<Pair<File, File>> newSnapshots = createSnapshots(sessionId, scanId, rows, cols, scaleRows, scaleCols);
//                                newSnapshots.ifPresent(p -> addSnapshotFilesToResource( sc, p.getLeft(), p.getRight()));
//
//                                return getSnapshotFiles(sc, sessionId, scanId, rows, cols);
//                            });
//                }
//                .orElseGet({
//                        createSnapshots(sessionId, scanId, rows, cols, scaleRows, scaleCols)
//                                .ifPresent(shots -> {
//                                createSnapshotCatalog(sessionId, scanId).ifPresent(sc -> {
//                                addSnapshotFilesToResource(sc, shots.getLeft(), shots.getRight());
//                                snapshotCatalog = getSnapshotResourceCatalog(sessionId, scanId);
//                                return getSnapshotFiles(snapshotCatalog.get(), sessionId, scanId, rows, cols);
//                            });
//                        })
//                });
//        return pair;
//    }
//
//    public Optional<XnatResourcecatalog> getSnapshotResourceCatalog(String sessionId, String scanId) {
//        try {
//            return Optional.ofNullable(_catalogService.getResourceCatalog(sessionId, scanId, SNAPSHOTS));
//        }
//        catch( Exception e) {
//            String msg = String.format("Error getting snapshot resource catalog for sessionId %s, scanId %s, rows %d, cols %d.", sessionId, scanId);
//            log.error(msg, e);
//            throw new RuntimeException(e);
//        }
//    }
//
//    public Pair<File, File> getSnapshotFiles( XnatResourcecatalog snapshotCatalog, String sessionId, String scanId, int rows, int cols)  {
//        try {
//            String project = null;
//            Path rootPath = Paths.get(snapshotCatalog.getUri()).getParent();
//            CatalogUtils.CatalogData catalogData = CatalogUtils.CatalogData.getOrCreate(rootPath.toString(), snapshotCatalog, project);
//            CatCatalogBean catalogBean = catalogData.catBean;
//            List<CatEntryI> entries = catalogBean.getEntries_entry();
//
//            List<String> resourceNames = entries.stream().map(CatEntryI::getUri).collect(Collectors.toList());
//
//            Pair<String, String> snaphotFileNames = _snapshotGenerator.getResourceNames(sessionId, scanId, rows, cols);
//
//            Optional<File> snapshotFile = resourceNames.stream()
//                    .filter(f -> snaphotFileNames.getLeft().equals(f))
//                    .map(f -> rootPath.resolve(f).toFile())
//                    .findAny();
//            Optional<File> thumbnailFile = resourceNames.stream()
//                    .filter(f -> snaphotFileNames.getRight().equals(f))
//                    .map(f -> rootPath.resolve(f).toFile())
//                    .findAny();
//            if (snapshotFile.isPresent() && thumbnailFile.isPresent()) {
//                log.debug("Matching resources found. Snapshot {} and thumbnail {}", snapshotFile.get(), thumbnailFile.get());
//                Pair<File, File> pair = ImmutablePair.of(snapshotFile.get(), thumbnailFile.get());
//                return Optional.of(pair);
//            } else {
//                log.debug("Matching resources not found.");
//                return Optional.ofNullable(null);
//            }
//        }
//        catch ( Exception e) {
//            String msg = String.format("Error getting montage and thumbnail for sessionId %s, scanId %s, rows %d, cols %d.", sessionId, scanId, rows, cols);
//            log.error(msg, e);
//            throw new RuntimeException( e);
//        }
//    }
//
//    public Optional<Pair<File, File>> createSnapshots( String sessionId, String scanId, int rows, int cols, float scaleRows, float scaleCols) {
//        try {
//            return Optional.ofNullable( _snapshotGenerator.createMontageAndThumbnail(sessionId, scanId, rows, cols, scaleRows, scaleCols));
//        }
//        catch( Exception e) {
//            String msg = String.format("Error creating montage and thumbnail for sessionId %s, scanId %s, rows %d, cols %d, scaleRows %f, and scaleCols %f.", sessionId, scanId, rows, cols, scaleRows, scaleCols);
//            log.error(msg, e);
//            throw new RuntimeException(e);
//        }
//    }
//
//    public void addSnapshotFilesToResource( XnatResourcecatalog resourcecatalog, File snapshotFile, File thumbnailFile) {
//        try {
//            final XnatResourcecatalog updated = _catalogService.insertResources(XDAT.getUserDetails(), resourcecatalog,
//                    XnatResourceInfoMap.builder()
//                            .resource(snapshotFile.getName(), snapshotFile, GIF, ORIGINAL)
//                            .resource(thumbnailFile.getName(), thumbnailFile, GIF, THUMBNAIL)
//                            .build());
//            FileUtils.deleteQuietly(snapshotFile);
//            FileUtils.deleteQuietly(thumbnailFile);
//        }
//        catch( Exception e) {
//            String msg = String.format("Error adding files %s and %s to snapshots folder %s.", snapshotFile, thumbnailFile, resourcecatalog.getUri());
//            log.error(msg, e);
//            throw new RuntimeException(e);
//        }
//    }
//
//    private Optional<XnatResourcecatalog> createSnapshotCatalog(final String sessionId, final String scanId) {
//        final String parentUri = ROOT_URI + sessionId + "/scans/" + scanId + SNAPSHOTS_RESOURCE;
//        log.debug("Creating the snapshots folder for scan {} of session {} at URI {}", scanId, sessionId, parentUri);
//        try {
//            String description = "Snapshots for session " + sessionId + " scan " + scanId;
//            final XnatResourcecatalog catalog =
//                    _catalogService.createAndInsertResourceCatalog(XDAT.getUserDetails(), parentUri, 1, SNAPSHOTS, description, GIF, SNAPSHOTS);
//            log.debug("Created the snapshots folder for scan {} of session {} at URI {}", scanId, sessionId, UriParserUtils.getArchiveUri(catalog));
//            return Optional.of(catalog);
//        } catch (Exception e) {
//            String msg = String.format("An error occurred creating the snapshots folder for scan %s of session %s", scanId, sessionId);
//            log.error(msg, e);
//            throw new RuntimeException(msg, e);
//        }
//    }
//
//    private static final String DICOM_RESOURCE     = "/resources/DICOM/files";
//    private static final String SNAPSHOTS_RESOURCE = "/resources/SNAPSHOTS/files";
//    private static final String ROOT_URI           = "/archive/experiments/";
//    private static final String SNAPSHOTS          = "SNAPSHOTS";
//    private static final String GIF                = "GIF";
//    private static final String ORIGINAL           = "ORIGINAL";
//    private static final String THUMBNAIL          = "THUMBNAIL";
//
//    private final CatalogService _catalogService;
//    private final SnapshotResourceGenerator _snapshotGenerator;
}
