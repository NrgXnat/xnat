package org.nrg.xnat.snapshot.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.XnatImagescandataBean;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xnat.helpers.resource.XnatResourceInfoMap;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnat.snapshot.convert.SnapshotDicomConvertImage;
import org.nrg.xnat.snapshot.services.SnapshotGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author pradeep.d
 */
@Service
@Slf4j
public class SnapshotGenerationServiceImpl implements SnapshotGenerationService {
    @Autowired
    public SnapshotGenerationServiceImpl(final CatalogService catalogService) {
        _catalogService = catalogService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateSnapshot(final String sessionId, final String scanId) {
        return generateSnapshot(sessionId, scanId, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateSnapshot(final String sessionId, final String scanId, final String gridView) {
        log.debug("Starting snapshot generation for scan {} of session {} with {} grid view", sessionId, scanId, StringUtils.defaultIfBlank(gridView, "default"));
        final XnatResourcecatalog snapshotCatalog = createOrGetSnapshotCatalog(sessionId, scanId);
        if (snapshotCatalog != null) {
            final String path = getSnapshotsImage(snapshotCatalog, sessionId, scanId, gridView);
            log.debug("Found or created snapshot for scan {} of session {} with {} grid view at path {}", sessionId, scanId, StringUtils.defaultIfBlank(gridView, "default"), path);
            return path;
        }

        log.error("Snapshot folder for scan {} of session {} does not exist", scanId, sessionId);
        return null;
    }

    /**
     * @param sessionId The ID of the session containing the target scan.
     * @param scanId    The ID of the scan for which snapshot(s) should be verified.
     *
     * @return Returns <b>true</b> if the snapshots folder was verified (i.e. created or located), <b>false</b> otherwise.
     */
    private XnatResourcecatalog createOrGetSnapshotCatalog(final String sessionId, final String scanId) {
        final String parentUri = ROOT_URI + sessionId + "/scans/" + scanId + SNAPSHOTS_RESOURCE;
        log.debug("Creating or retrieving the snapshots folder for scan {} of session {} at URI {}", scanId, sessionId, parentUri);
        try {
            final XnatResourcecatalog catalog = _catalogService.createAndInsertResourceCatalog(XDAT.getUserDetails(), parentUri, 1, SNAPSHOTS, "Snapshots for session " + sessionId + " scan " + scanId, GIF, SNAPSHOTS);
            log.debug("Created or retrieved the snapshots folder for scan {} of session {} at URI {}", scanId, sessionId, UriParserUtils.getArchiveUri(catalog));
            return catalog;
        } catch (Exception e) {
            log.error("An error occurred verifying the snapshots folder for scan {} of session {}", scanId, sessionId, e);
            return null;
        }
    }

    /**
     * @param sessionId The ID of the session containing the target scan.
     * @param scanId    The ID of the scan for which snapshot(s) should be generated.
     * @param gridView  The grid-view specifier.
     *
     * @return The URI path to the snapshot image.
     */
    private String getSnapshotsImage(final XnatResourcecatalog snapshotCatalog, final String sessionId, final String scanId, final String gridView) {
        log.debug("Getting snapshot image for scan {} of session {} with {} grid view", sessionId, scanId, StringUtils.defaultIfBlank(gridView, "default"));
        final String imageName = sessionId + "_" + scanId + StringUtils.defaultIfBlank(gridView, "").toUpperCase() + ".gif";
        final Path   path      = Paths.get(snapshotCatalog.getUri()).getParent();
        try {
            final File    target      = path.resolve(imageName).toFile();
            final boolean imageVerify = target.exists() && target.isFile();
            return imageVerify ? target.getAbsolutePath() : insertSnapshot(snapshotCatalog, sessionId, scanId, gridView, imageName);
        } catch (Exception e) {
            log.error("An error occurred trying to upload snapshot image {} to the URI {}", imageName, path, e);
            return null;
        }
    }

    /**
     * @param sessionId The ID of the session containing the target scan.
     * @param scanId    The ID of the scan for which snapshot(s) should be uploaded.
     * @param gridView  The grid-view specifier.
     *
     * @return The URI to the snapshot.
     */
    private String insertSnapshot(final XnatResourcecatalog snapshotCatalog, final String sessionId, final String scanId, final String gridView, final String imageName) {
        final String parentUri = ROOT_URI + sessionId + "/scans/" + scanId;
        try {
            final XnatResourcecatalog dicomCatalog = _catalogService.getResourceDataFromUri(parentUri + DICOM_RESOURCE, true).getCatalogResource();
            if (dicomCatalog == null) {
                throw new RuntimeException("Expected to get a resource catalog from the URI " + parentUri + DICOM_RESOURCE + " but it's null.");
            }

            final Path                      dicomPath     = Paths.get(dicomCatalog.getUri()).getParent();
            final String                    tempImagePath = dicomPath.getParent().toString();
            final SnapshotDicomConvertImage converter     = new SnapshotDicomConvertImage(dicomPath);
            final XnatImagescandataBean     scan          = new XnatImagescandataBean();
            scan.setId(scanId);
            scan.setImageSessionId(sessionId);

            final Pair<File, File> snapshots = StringUtils.isNotBlank(gridView) ? converter.createSnapshotImage(scan, tempImagePath, true, gridView) : converter.createSnapshotImage(scan, tempImagePath, false, "");
            log.debug("Uploading snapshot {} and thumbnail {} into snapshots folder {}", snapshots.getLeft().getAbsolutePath(), snapshots.getRight().getAbsolutePath(), snapshotCatalog.getUri());
            final File snapshot = snapshots.getKey();
            final File thumbnail = snapshots.getValue();
            _catalogService.insertResources(XDAT.getUserDetails(), snapshotCatalog, XnatResourceInfoMap.builder().resource(snapshot.getName(), snapshot, GIF, ORIGINAL).resource(thumbnail.getName(), thumbnail, GIF, THUMBNAIL).build());
            FileUtils.deleteQuietly(snapshot);
            FileUtils.deleteQuietly(thumbnail);

            final Path snapshotPath = Paths.get(snapshotCatalog.getUri()).getParent();
            final Path imagePath    = snapshotPath.resolve(imageName);
            return imagePath.toFile().exists() ? imagePath.toString() : snapshotPath.toString();
        } catch (Exception e) {
            log.error("An error occurred trying to upload snapshot images to the URI {}", parentUri, e);
            return null;
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
}
