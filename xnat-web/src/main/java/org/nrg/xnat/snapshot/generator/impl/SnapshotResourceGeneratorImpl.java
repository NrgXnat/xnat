package org.nrg.xnat.snapshot.generator.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.nrg.xdat.bean.CatDcmcatalogBean;
import org.nrg.xdat.bean.CatDcmentryBean;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xnat.services.archive.CatalogService;
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
import java.util.stream.Collectors;

/**
 * Implementation of SnapshotResourceGenerator.
 *
 * This version creates the files in a temporary directory. Make sure to delete them when done.
 *
 */
public class SnapshotResourceGeneratorImpl extends DicomImageRenderer implements SnapshotResourceGenerator {
    private final MontageGenerator montageGenerator;
    private final ThumbnailGenerator thumbnailGenerator;
    private final CatalogService catalogService;

    private String sessionId;
    private String scanId;
    private Path tmpRoot;
    private Path dicomRootPath;
    private List<String> files;
    private int nSlices;

    public SnapshotResourceGeneratorImpl(CatalogService catalogService) throws IOException {
        super();
        this.catalogService = catalogService;
        this.montageGenerator = new MontageGenerator();
        this.thumbnailGenerator = new ThumbnailGenerator();
        this.tmpRoot = Files.createTempDirectory( "snapshotGen");
    }

    public SnapshotResourceGeneratorImpl setScan(final String sessionId, final String scanId) throws Exception {
        XnatResourcecatalog resourcecatalog = catalogService.getDicomResourceCatalog( sessionId, scanId);
        Path dicomRootPath = Paths.get( resourcecatalog.getUri()).getParent();
        // project can be null.  CatalogUtils will sort it out.
        String project = null;
        CatalogUtils.CatalogData catalogData = CatalogUtils.CatalogData.getOrCreate( dicomRootPath.toString(), resourcecatalog, project);
        if( catalogData.catBean instanceof CatDcmcatalogBean) {
            init( sessionId, scanId, dicomRootPath, (CatDcmcatalogBean) catalogData.catBean);
        }
        return this;
    }

    private void init( String sessionId, String scanId, Path dicomRootPath, CatDcmcatalogBean dcmcatalogBean) {
        Collection<CatEntryI> entries = CatalogUtils.getEntriesByFilter(dcmcatalogBean, e -> e instanceof CatDcmentryBean);
        // Sort the list of files by instance number. The catalog is not presorted.
        files = entries.stream()
                .map( e -> (CatDcmentryBean) e)
                .sorted( Comparator.comparing( CatDcmentryBean::getInstancenumber))
                .map( e -> (new File( dicomRootPath.toString(), e.getUri())).getAbsolutePath())
                .collect(Collectors.toList());

        nSlices = dcmcatalogBean.getDimensions_z();

        this.sessionId = sessionId;
        this.scanId = scanId;
        this.dicomRootPath = dicomRootPath;
    }

    @Override
    public File createSnaphot() throws Exception {
        return createMontage( 1, 1);
    }

    @Override
    public File createMontage(int nRows, int nCols) throws Exception {
        final String name = getResourceName( sessionId, scanId, nCols, nRows);

        File montageFile = tmpRoot.resolve( name).toFile();
        BufferedImage montageImage = montageGenerator.generate( files, nSlices, nRows, nCols);

        writeImage( montageFile, montageImage);
        return montageFile;
    }

    @Override
    public Pair<File, File> createSnapshotAndThumbnail(float scaleRows, float scaleCols) throws Exception {
        return createMontageAndThumbnail( 1, 1, scaleRows, scaleCols);
    }

    @Override
    public Pair<File, File> createMontageAndThumbnail(int nCols, int nRows, float scaleRows, float scaleCols) throws Exception {
        final Pair<String, String> names = getResourceNames(sessionId, scanId, nCols, nRows);

        File montageFile = tmpRoot.resolve( names.getLeft()).toFile();
        File thumbnailFile = tmpRoot.resolve( names.getRight()).toFile();
        BufferedImage montageImage = montageGenerator.generate( files, nSlices, nRows, nCols);
        BufferedImage thumbnailImage = thumbnailGenerator.rescale( montageImage, scaleRows, scaleCols);

        writeImage( montageFile, montageImage);
        writeImage( thumbnailFile, thumbnailImage);
        return Pair.of(montageFile, thumbnailFile);
    }

    /**
     * Calculates the snapshot file names, with the original snapshot name as the pair key and the thumbnail name as the
     * pair value.
     *
     * @param sessionId The ID of the session.
     * @param scanId    The ID of the scan.
     * @param cols      The number of columns in montage grid.
     * @param rows      The number of rows in montage grid.
     *
     * @return A pair of strings, with the key as the original snapshot filename and the value as the thumbnail filename
     */
    @Override
    public Pair<String, String> getResourceNames(final String sessionId, final String scanId, final int cols, int rows) {
        String root;
        if( cols == 1 && rows == 1) {
            root = String.format("%s_%s_qc", sessionId, scanId);
        } else {
            root = String.format( "%s_%s_%dX%d_qc", sessionId, scanId, cols, rows);
        }
        return Pair.of(root + ".gif", root + "_t.gif");
    }

    @Override
    public String getResourceName(final String sessionId, final String scanId, final int cols, int rows) {
        String root;
        if( cols == 1 && rows == 1) {
            root = String.format("%s_%s_qc", sessionId, scanId);
        } else {
            root = String.format( "%s_%s_%dX%d_qc", sessionId, scanId, cols, rows);
        }
        return root + ".gif";
    }

}
