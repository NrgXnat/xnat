package org.nrg.xnat.snapshot.generator.impl;

import static org.nrg.xnat.snapshot.generator.SnapshotResourceGenerator.*;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.bean.CatDcmcatalogBean;
import org.nrg.xdat.bean.CatDcmentryBean;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnat.snapshot.FileResource;
import org.nrg.xnat.snapshot.generator.SnapshotResourceGenerator;
import org.nrg.xnat.utils.CatalogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of SnapshotResourceGenerator.
 *
 * This version creates the files in a temporary directory. Make sure to delete them when done.
 *
 * Assumes the scan catalog contains a z-dimension attribute and gives up if it is absent.
 */
@Service
@Slf4j
public class SnapshotResourceGeneratorImpl extends DicomImageRenderer implements SnapshotResourceGenerator {
    @Autowired
    public SnapshotResourceGeneratorImpl(final CatalogService catalogService) throws IOException {
        super();
        _catalogService = catalogService;
        _tmpRoot = Files.createTempDirectory("snapshotGen");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<FileResource> createSnapshot(final String sessionId, final String scanId, final int nRows, final int nCols) throws InitializationException, IOException {
        if (isUnsnapshottable(sessionId, scanId)) {
            return Optional.empty();
        }

        log.debug("Creating snapshot for session {} scan {} with {} rows and {} columns", sessionId, scanId, nRows, nCols);

        final SnapshotAttributes attributes   = getAttributes(sessionId, scanId);
        final Path               montageFile  = _tmpRoot.resolve(getSnapshotResourceName(sessionId, scanId, nRows, nCols, getFormat()));
        final BufferedImage      montageImage = new MontageGenerator().generate(attributes.getFiles(), attributes.getNSlices(), nRows, nCols);

        writeImage(montageFile.toFile(), montageImage);
        return Optional.of(new FileResource(montageFile, getSnapshotContentName(nRows, nCols), getFormat()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<FileResource> createThumbnail(final String sessionId, final String scanId, final int nRows, final int nCols, final float scaleRows, final float scaleCols) throws InitializationException, IOException {
        if (isUnsnapshottable(sessionId, scanId)) {
            return Optional.empty();
        }

        log.debug("Creating thumbnail for session {} scan {} with {} rows and {} columns, scaling rows by {} and columns by {}", sessionId, scanId, nRows, nCols, scaleRows, scaleCols);

        final SnapshotAttributes attributes     = getAttributes(sessionId, scanId);
        final Path               thumbnailFile  = _tmpRoot.resolve(getThumbnailResourceName(sessionId, scanId, nRows, nCols, getFormat()));
        final BufferedImage      montageImage   = new MontageGenerator().generate(attributes.getFiles(), attributes.getNSlices(), nRows, nCols);
        final BufferedImage      thumbnailImage = new ThumbnailGenerator().rescale(montageImage, scaleRows, scaleCols);

        writeImage(thumbnailFile.toFile(), thumbnailImage);
        return Optional.of(new FileResource(thumbnailFile, getThumbnailContentName(nRows, nCols), getFormat()));
    }

    /**
     * Returns true if this generator can create snapshots for this scan and initializes the generator if necessary.
     *
     * @param sessionId The ID of the session to check
     * @param scanId    The ID of the scan to check
     *
     * @return Returns true if the generated can create snapshots for this scan
     */
    private boolean isUnsnapshottable(final String sessionId, final String scanId) {
        final SnapshotAttributes attributes;
        if (!isKnownScan(sessionId, scanId)) {
            attributes = initGenerator(sessionId, scanId);
            setAttributes(sessionId, scanId, attributes);
            log.debug("Created new attributes object for session {} scan {}: {}", sessionId, scanId, attributes);
        } else {
            attributes = getAttributes(sessionId, scanId);
            log.debug("Retrieved existing attributes object for session {} scan {}: {}", sessionId, scanId, attributes);
        }
        return !attributes.isHasSnapshot();
    }

    private String getFormat() {
        return DEFAULT_FORMAT;
    }

    private SnapshotAttributes getAttributes(final String sessionId, final String scanId) {
        return resources.get(getKey(sessionId, scanId));
    }

    private SnapshotAttributes setAttributes(final String sessionId, final String scanId, final SnapshotAttributes attributes) {
        log.trace("Setting attributes for session {} scan {}", sessionId, scanId);
        final String key = getKey(sessionId, scanId);
        resources.put(key, attributes);
        log.debug("Inserted attributes object for session {} scan {} with key \"{}\": {}", sessionId, scanId, key, attributes);
        return attributes;
    }

    private SnapshotAttributes markBroken(final String sessionId, final String scanId) {
        log.debug("Marking session {} scan {} as \"broken\" (e.g. can't generate snapshot for some reason)", sessionId, scanId);
        return setAttributes(sessionId, scanId, SnapshotAttributes.broken(sessionId, scanId));
    }

    /**
     * Returns true if this generator has encountered this scan before.
     *
     * @param sessionId The ID of the session to check
     * @param scanId    The ID of the scan to check
     *
     * @return Returns true if the generator has encountered this scan before.
     */
    private boolean isKnownScan(final String sessionId, final String scanId) {
        return resources.containsKey(getKey(sessionId, scanId));
    }

    /**
     * Determine if this generator can handle the scan and prepare the generator as needed.
     *
     * @param sessionId The ID of the session to initialize
     * @param scanId    The ID of the scan to initialize
     *
     * @return Returns true if the generator can handle the scan
     */
    private SnapshotAttributes initGenerator(final String sessionId, final String scanId) {
        log.debug("Initialize snapshot generator for sessionId {}, scanId {}.", sessionId, scanId);
        final XnatResourcecatalog catalog = getDicomResourceCatalog(sessionId, scanId);
        if (catalog == null) {
            return SnapshotAttributes.broken(sessionId, scanId);
        }
        log.debug("DICOM resource catalog found at {}", catalog.getUri());
        try {
            // project can be null.  CatalogUtils will sort it out.
            final Path                     dicomRootPath = Paths.get(catalog.getUri()).getParent();
            final CatalogUtils.CatalogData catalogData   = CatalogUtils.CatalogData.getOrCreate(dicomRootPath.toString(), catalog, null);
            return init(sessionId, scanId, dicomRootPath, catalogData.catBean);
        } catch (ServerException e) {
            log.error("An error occurred trying to load the catalog for session {} scan {}, can't initialize generator for this scan", sessionId, scanId, e);
            return SnapshotAttributes.broken(sessionId, scanId);
        }
    }

    private XnatResourcecatalog getDicomResourceCatalog(final String sessionId, final String scanId) {
        try {
            final XnatResourcecatalog catalog = _catalogService.getDicomResourceCatalog(sessionId, scanId);
            if (catalog != null) {
                log.debug("Retrieved DICOM catalog for session {} scan {} for catalog file {}", sessionId, scanId, catalog.getUri());
                return catalog;
            }
            log.warn("The scan {} in session {} does not contain a DICOM resource catalog. Creating snapshots here is above my pay grade. Get a smarter SnapshotResourceGenerator", scanId, sessionId);
        } catch (ClientException e) {
            log.error("An error occurred trying to retrieve the catalog for session {} scan {}, can't initialize generator for this scan", sessionId, scanId, e);
        }
        return null;
    }

    /**
     * Initialize the scan generator with the contents of the catalog in the given location.
     *
     * Find all files in the catalog that have instance numbers and sort them by instance number.
     * Find the number of slices recorded in the Dicom catalog. Number of slices will not equal number of files
     * when files are multi-frame.
     *
     * @param sessionId     The ID of the session to initialize
     * @param scanId        The ID of the scan to initialize
     * @param dicomRootPath The path to the folder containing the DICOM to use for generating the snapshot
     * @param catalogBean   The catalog containing the DICOM references (must be of type <b>CatDcmcatalogBean</b>
     */
    private SnapshotAttributes init(final String sessionId, final String scanId, final Path dicomRootPath, final CatCatalogBean catalogBean) {
        if (!(catalogBean instanceof CatDcmcatalogBean)) {
            log.warn("Tried to init snapshot attributes for session {} scan {} but the submitted catalog {} (ID: {}) is not a DICOM catalog: {}", sessionId, scanId, catalogBean.getCatCatalogId(), catalogBean.getId(), catalogBean.getClass().getName());
            return markBroken(sessionId, scanId);
        }
        final CatDcmcatalogBean catalog = (CatDcmcatalogBean) catalogBean;
        // Sort the list of files by instance number. The catalog is not presorted.
        final List<String> files = CatalogUtils.getEntriesByFilter(catalog, CatDcmentryBean.class::isInstance)
                                               .stream()
                                               .map(CatDcmentryBean.class::cast)
                                               .sorted(Comparator.comparing(CatDcmentryBean::getInstancenumber))
                                               .map(e -> (new File(dicomRootPath.toString(), e.getUri())).getAbsolutePath())
                                               .collect(Collectors.toList());

        if (files.isEmpty() || catalog.getDimensions_z() == null) {
            log.warn("The DICOM catalog {} for scan {} in session {} does not contain any number-of-frames info. This is above my pay grade. Get a smarter SnapshotResourceGenerator", catalog.getName(), scanId, sessionId);
            return markBroken(sessionId, scanId);
        }

        log.debug("Found {} files in the catalog {} (ID: {}) for session {} scan {}", files.size(), catalogBean.getCatCatalogId(), catalogBean.getId(), sessionId, scanId);
        return setAttributes(sessionId, scanId, SnapshotAttributes.builder().sessionId(sessionId).scanId(scanId).hasSnapshot(true).files(files).nSlices(catalog.getDimensions_z()).build());
    }

    private static String getKey(final String sessionId, final String scanId) {
        return sessionId + ":" + scanId;
    }

    @Getter
    @Builder
    private static class SnapshotAttributes {
        static SnapshotAttributes broken(final String sessionId, final String scanId) {
            return builder().sessionId(sessionId).scanId(scanId).hasSnapshot(false).build();
        }

        private final String       sessionId;
        private final String       scanId;
        private final boolean      hasSnapshot;
        private final int          nSlices;
        @Builder.Default
        private final List<String> files = new ArrayList<>();
    }

    private static final String DEFAULT_FORMAT = "gif";

    private final CatalogService _catalogService;
    private final Path           _tmpRoot;

    private final Map<String, SnapshotAttributes> resources = new HashMap<>();
}
