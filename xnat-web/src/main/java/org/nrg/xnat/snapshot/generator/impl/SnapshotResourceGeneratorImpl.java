package org.nrg.xnat.snapshot.generator.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.bean.CatDcmcatalogBean;
import org.nrg.xdat.bean.CatDcmentryBean;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnat.snapshot.FileResource;
import org.nrg.xnat.snapshot.generator.SnapshotResourceGenerator;
import org.nrg.xnat.utils.CatalogUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of SnapshotResourceGenerator.
 *
 * This version creates the files in a temporary directory. Make sure to delete them when done.
 *
 * Assumes the scan catalog contains a z-dimension attribute and gives up if it is absent.
 *
 */
@Slf4j
public class SnapshotResourceGeneratorImpl extends DicomImageRenderer implements SnapshotResourceGenerator {
    private final MontageGenerator montageGenerator;
    private final ThumbnailGenerator thumbnailGenerator;
    private final CatalogService catalogService;

    private boolean hasSnapshot;
    private String sessionId;
    private String scanId;
    private Path tmpRoot;
    private Path dicomRootPath;
    private List<String> files;
    private int nSlices;

    public SnapshotResourceGeneratorImpl(CatalogService catalogService) throws IOException {
        super();
        this.hasSnapshot = false;
        this.catalogService = catalogService;
        this.montageGenerator = new MontageGenerator();
        this.thumbnailGenerator = new ThumbnailGenerator();
        this.tmpRoot = Files.createTempDirectory( "snapshotGen");
    }

    /**
     * Returns true if this generator can create snapshots for this scan and initializes the generator if necessary.
     *
     * @param sessionId
     * @param scanId
     * @return
     * @throws Exception
     */
    public boolean hasSnapshot( final String sessionId, final String scanId) throws Exception {
        if( isKnownScan( sessionId, scanId)) {
            return hasSnapshot;
        } else {
            hasSnapshot = initGenerator( sessionId, scanId);
            return hasSnapshot;
        }
    }

    /**
     * Returns true if this generator has encountered this scan before.
     *
     * @param sessionId
     * @param scanId
     * @return
     */
    private boolean isKnownScan(final String sessionId, final String scanId) {
        return this.sessionId != null && this.sessionId.equals( sessionId) && this.scanId != null && this.scanId.equals( scanId);
    }

    /**
     * Determine if this generator can handle the scan and prepare the generator as needed.
     *
     * @param sessionId
     * @param scanId
     * @return
     * @throws Exception
     */
    private boolean initGenerator(final String sessionId, final String scanId) throws Exception {
        boolean isHandler = false;

        log.debug("Initialize snapshot generator for sessionId {}, scanId {}.", sessionId, scanId);
        XnatResourcecatalog resourcecatalog = catalogService.getDicomResourceCatalog(sessionId, scanId);
        if( resourcecatalog != null) {
            log.debug("Dicom resource catalog found at {}", resourcecatalog.getUri());
            Path dicomRootPath = Paths.get(resourcecatalog.getUri()).getParent();
            // project can be null.  CatalogUtils will sort it out.
            String project = null;
            CatalogUtils.CatalogData catalogData = CatalogUtils.CatalogData.getOrCreate(dicomRootPath.toString(), resourcecatalog, project);
            if (catalogData.catBean instanceof CatDcmcatalogBean) {
                isHandler = init(sessionId, scanId, dicomRootPath, (CatDcmcatalogBean) catalogData.catBean);
            }
        }
        else {
            isHandler = false;
            log.warn("The scan {} in session {} does not contain a DICOM resource catalog. Creating snapshots here is above my pay grade. Get a smarter SnapshotResourceGenerator", scanId, sessionId);
        }
        return isHandler;
    }

    /**
     * Initialize the scan generator with the contents of the catalog in the given location.
     *
     * Find all files in the catalog that have instance numbers and sort them by instance number.
     * Find the number of slices recorded in the Dicom catalog. Number of slices will not equal number of files
     * when files are multi-frame.
     *
     * @param sessionId
     * @param scanId
     * @param dicomRootPath
     * @param dcmcatalogBean
     */
    private boolean init( String sessionId, String scanId, Path dicomRootPath, CatDcmcatalogBean dcmcatalogBean) {
        boolean hasSnapshot;
        Collection<CatEntryI> entries = CatalogUtils.getEntriesByFilter(dcmcatalogBean, e -> e instanceof CatDcmentryBean);
        // Sort the list of files by instance number. The catalog is not presorted.
        files = entries.stream()
                .map( e -> (CatDcmentryBean) e)
                .sorted( Comparator.comparing( CatDcmentryBean::getInstancenumber))
                .map( e -> (new File( dicomRootPath.toString(), e.getUri())).getAbsolutePath())
                .collect(Collectors.toList());

        if( ! files.isEmpty() && dcmcatalogBean.getDimensions_z() != null ) {
            nSlices = dcmcatalogBean.getDimensions_z();
            hasSnapshot = true;
        } else {
            hasSnapshot = false;
            log.warn("The dicom catalog {} for scan {} in session {} does not contain any number-of-frames info. This is above my pay grade. Get a smarter SnapshotResourceGenerator", dcmcatalogBean.getName(), scanId, sessionId);
        }

        this.sessionId = sessionId;
        this.scanId = scanId;
        this.dicomRootPath = dicomRootPath;
        this.hasSnapshot = hasSnapshot;
        return hasSnapshot;
    }

    @Override
    public Optional<FileResource> createSnaphot( String sessionId, String scanId, int nRows, int nCols) throws Exception {
        Optional<FileResource> montageFileResource = Optional.ofNullable( null);
        if( hasSnapshot( sessionId, scanId)) {
            final String name = getSnapshotResourceName( sessionId, scanId, nRows, nCols, getFormat());

            Path montageFile = tmpRoot.resolve(name);
            BufferedImage montageImage = montageGenerator.generate(files, nSlices, nRows, nCols);

            writeImage(montageFile.toFile(), montageImage);
            montageFileResource = Optional.of( new FileResource( montageFile, getSnapshotContentName( sessionId, scanId, nRows, nCols), getFormat()));
        }
        return montageFileResource;
    }

    @Override
    public Optional< FileResource> createThumbnail( String sessionId, String scanId, int nRows, int nCols, float scaleRows, float scaleCols) throws Exception {
        Optional< FileResource> fileResource = Optional.ofNullable( null);
        log.debug("Create thumbnail montage: sessionId: {}, scanId: {} nrows: {}, ncols: {}, scaleRows: {}, scaleCols: {}", sessionId, scanId, nRows, nCols, scaleRows, scaleCols);
        if( hasSnapshot( sessionId, scanId)) {

            Path thumbnailFile = tmpRoot.resolve( getThumbnailResourceName( sessionId, scanId, nRows, nCols, getFormat()));
            BufferedImage montageImage = montageGenerator.generate(files, nSlices, nRows, nCols);
            BufferedImage thumbnailImage = thumbnailGenerator.rescale( montageImage, scaleRows, scaleCols);

            writeImage(thumbnailFile.toFile(), thumbnailImage);
            FileResource thumbnailFileResource = new FileResource( thumbnailFile, getThumbnailContentName( sessionId, scanId, nRows, nCols), getFormat());
            fileResource = Optional.of( thumbnailFileResource);
        }
        return fileResource;
    }

    public String getFormat() { return "gif";}

    public String getSnapshotContentName(String sessionId, String scanId, int rows, int cols) {
        return (rows == 1 && cols ==1) ? "ORIGINAL" : String.format( "%dX%d", cols, rows);
    }

    public String getThumbnailContentName(String sessionId, String scanId, int rows, int cols) {
        return (rows == 1 && cols ==1) ? "THUMBNAIL" : String.format( "%dX%d_THUMBNAIL", cols, rows);
    }

    public String getSnapshotResourceName(final String sessionId, final String scanId, final int rows, int cols, String format) {
        String grid = (rows == 1 && cols == 1) ? "" : String.format( "_%sx%s", cols, rows);
        return String.format("%s_%s%s_qc.%s", sessionId, scanId, grid, format.toLowerCase());
    }

    public String getThumbnailResourceName(final String sessionId, final String scanId, final int rows, int cols, String format) {
        String grid = (rows == 1 && cols == 1) ? "" : String.format( "_%sx%s", cols, rows);
        return String.format("%s_%s%s_qc_t.%s", sessionId, scanId, grid, format.toLowerCase());
    }

}
