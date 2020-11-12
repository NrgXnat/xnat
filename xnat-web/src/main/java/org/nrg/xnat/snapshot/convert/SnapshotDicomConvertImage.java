package org.nrg.xnat.snapshot.convert;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Toolbar;
import ij.io.FileInfo;
import ij.io.Opener;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import ij.process.StackProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.action.ServerException;
import org.nrg.xdat.bean.CatDcmcatalogBean;
import org.nrg.xdat.bean.CatDcmentryBean;
import org.nrg.xdat.bean.XnatImagescandataBean;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xnat.plexiviewer.lite.io.PlexiFileSaver;
import org.nrg.xnat.plexiviewer.utils.ImageUtils;
import org.nrg.xnat.plexiviewer.utils.UnzipFile;
import org.nrg.xnat.plexiviewer.utils.transform.BitConverter;
import org.nrg.xnat.plexiviewer.utils.transform.IntensitySetter;
import org.nrg.xnat.plexiviewer.utils.transform.PlexiMontageMaker;
import org.nrg.xnat.utils.CatalogUtils;

import java.awt.*;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generate snapshot and thumbnail images for DICOM series.
 *
 * Montage grid dimensions can be specified.
 * This implementation uses ImageJ.
 */
@Slf4j
public class SnapshotDicomConvertImage {
    // variables used by ImageJ. May be null.
    private FileInfo     fi = null;
    private String       info1 = null;
    private String       title = null;

    private Path         rootPath;
    private List<String> files;
    private boolean      zipped = false;
    private int          width  = 0;
    private int          height = 0;

    /**
     * Constructor for DICOM images found in Dicom Catalog file.
     *
     * @param dicomCatalog
     * @throws ServerException if dicomCatalog is not found.
     */
    public SnapshotDicomConvertImage( final XnatResourcecatalog dicomCatalog) throws ServerException {
        // project can be null.  CatalogUtils will sort it out.
        String project = null;
        rootPath = Paths.get( dicomCatalog.getUri()).getParent();
        files = new ArrayList<>();
        CatalogUtils.CatalogData catalogData = CatalogUtils.CatalogData.getOrCreate( rootPath.toString(), dicomCatalog, project);
        if( catalogData.catBean instanceof CatDcmcatalogBean) {

            CatDcmcatalogBean dcmcatalogBean = (CatDcmcatalogBean) catalogData.catBean;
            Collection<CatEntryI> entries = CatalogUtils.getEntriesByFilter(dcmcatalogBean, e -> e instanceof CatDcmentryBean);
            for (CatEntryI entry : entries) {
                File f = new File( rootPath.toString(), entry.getUri());
                files.add( f.getAbsolutePath());
            }
        }
    }

    /**
     * Constructor for DICOM files found at the specified path or the contents of a gzipped file on the path.
     *
     * @param path
     */
    public SnapshotDicomConvertImage(final Path path) {
        rootPath = path;
        files = FileUtils.listFiles(rootPath.toFile(), TrueFileFilter.INSTANCE, FalseFileFilter.INSTANCE).stream().map(file -> rootPath.relativize(file.toPath())).map(Path::toString).collect(Collectors.toList());
        zipped = files.stream().anyMatch(file -> StringUtils.endsWith(file, ".gz"));
        unzip();
    }

    public SnapshotDicomConvertImage(final String dir) {
        this(Paths.get(dir));
    }

    /**
     * Calculates the snapshot file names, with the original snapshot name as the pair key and the thumbnail name as the
     * pair value.
     *
     * @param scan The scan.
     *
     * @return A pair of strings, with the key as the original snapshot filename and the value as the thumbnail filename
     */
    public static Pair<String, String> getSnapshotName(final XnatImagescandataBean scan) {
        return getSnapshotName(scan.getImageSessionId(), scan.getId(), null);
    }

    /**
     * Calculates the snapshot file names, with the original snapshot name as the pair key and the thumbnail name as the
     * pair value.
     *
     * @param scan     The scan.
     * @param gridView The grid view to render.
     *
     * @return A pair of strings, with the key as the original snapshot filename and the value as the thumbnail filename
     */
    public static Pair<String, String> getSnapshotName(final XnatImagescandataBean scan, final String gridView) {
        return getSnapshotName(scan.getImageSessionId(), scan.getId(), gridView);
    }

    /**
     * Calculates the snapshot file names, with the original snapshot name as the pair key and the thumbnail name as the
     * pair value.
     *
     * @param sessionId The ID of the session.
     * @param scanId    The ID of the scan.
     *
     * @return A pair of strings, with the key as the original snapshot filename and the value as the thumbnail filename
     */
    public static Pair<String, String> getSnapshotName(final String sessionId, final String scanId) {
        return getSnapshotName(sessionId, scanId, null);
    }

    /**
     * Calculates the snapshot file names, with the original snapshot name as the pair key and the thumbnail name as the
     * pair value.
     *
     * @param sessionId The ID of the session.
     * @param scanId    The ID of the scan.
     * @param gridView  The grid view to render.
     *
     * @return A pair of strings, with the key as the original snapshot filename and the value as the thumbnail filename
     */
    public static Pair<String, String> getSnapshotName(final String sessionId, final String scanId, final String gridView) {
        final String root = sessionId + "_" + scanId + (StringUtils.isNotBlank(gridView) ? "_" + gridView.toUpperCase() : "") + "_qc";
        return Pair.of(root + ".gif", root + "_t.gif");
    }

    /**
     *
     */
    private void unzip() {
        if (zipped) {
            String suffix  = "_" + new Random().nextInt();
            File   tempDir = FileUtils.getTempDirectory();
            try {
                File dir = File.createTempFile("NRG", suffix, tempDir);
                if (dir.exists()) {
                    dir.delete();
                }
                boolean success = dir.mkdir();
                for (final String s : files) {
                    new UnzipFile().gunzip(rootPath + File.separator + s, dir.getPath());
                }
                rootPath = dir.toPath();
                files.clear();
                files.addAll(FileUtils.listFilesAndDirs(rootPath.toFile(), TrueFileFilter.INSTANCE, FalseFileFilter.INSTANCE).stream().map(file -> rootPath.relativize(file.toPath())).map(Path::toString).collect(Collectors.toList()));
            } catch (IOException e) {
                log.error("An error occurred while unzipping files in the folder {}", rootPath, e);
            } catch (Exception e) {
                log.error("An unexpected error occurred while unzipping files in the folder {}", rootPath, e);
            }
        }
    }

    /**
     * @return
     */
    public ImagePlus getImagePlus() {
        ImagePlus   imagesPlus         = null;
        final int   n                  = files.size();
        ImageStack  stack              = null;
        double      min                = Double.MAX_VALUE;
        double      max                = -Double.MAX_VALUE;
        Calibration cal                = null;
        boolean     allSameCalibration = true;
        int         count              = 0;

        try {
            String  dimResult    = getResizeDimensionCalc();
            Integer resizeWid    = null;
            Integer resizeHeight = null;
            if (dimResult != null) {
                String reSize[] = dimResult.split("X");
                resizeWid = Integer.parseInt(reSize[0]);
                resizeHeight = Integer.parseInt(reSize[1]);
                log.debug("Different dimension is found " + dimResult);
            }

            for (final String file : files) {
                Opener opener = new Opener();
                opener.setSilentMode(true);
                ImagePlus imp = opener.openImage(rootPath.resolve(file).toString());
                if (imp != null && stack == null) {
                    width = imp.getWidth();
                    height = imp.getHeight();
                    cal = imp.getCalibration();
                    ColorModel cm = imp.getProcessor().getColorModel();
                    if (resizeWid != null & resizeHeight != null) {
                        stack = new ImageStack(resizeWid, resizeHeight, cm);
                        width = resizeWid;
                        height = resizeHeight;
                    } else {
                        stack = new ImageStack(width, height, cm);
                    }
                }

                if (imp == null) {
                    if (!file.startsWith(".")) {
                        log.error(file + ": unable to open");
                    }
                    continue;
                }

                if (imp.getWidth() != width || imp.getHeight() != height) {
                    log.error(file + ": wrong size; " + width + "x" + height + " expected, " + imp.getWidth() + "x" + imp.getHeight() + " found");
                    double tempWidth = 0, tempHeight = 0;
                    if (imp.getWidth() > resizeWid && resizeWid > 0) {
                        tempWidth = resizeWid;
                        tempHeight = (double) (imp.getHeight() * resizeWid) / (double) imp.getWidth();
                        if (tempHeight > resizeHeight && tempHeight > 0) {
                            tempHeight = resizeHeight;
                            tempWidth = (tempWidth * resizeHeight) / tempHeight;
                        }
                    }
                    if (imp.getHeight() > resizeHeight && resizeHeight > 0) {
                        tempHeight = resizeHeight;
                        tempWidth = (double) (imp.getWidth() * resizeHeight) / (double) imp.getHeight();
                        if (tempWidth > resizeWid && tempWidth > 0) {
                            tempWidth = resizeWid;
                            tempHeight = (tempHeight * resizeWid) / tempWidth;
                        }
                    }
                    if ((int) Math.round(tempWidth) > 0 && (int) Math.round(tempHeight) > 0) {
                        ImageProcessor imageProcessor = imp.getProcessor();
                        imageProcessor = imageProcessor.resize((int) Math.round(tempWidth), (int) Math.round(tempHeight));
                        imp.setProcessor(imageProcessor);
                        log.error("resize Width :: " + tempWidth + "  --resize Height:: " + tempHeight);
                    }
                }

                ImageStack inputStack = imp.getStack();
                if (imp.getWidth() != width || imp.getHeight() != height) {
                    int xCenter = 0, yCenter = 0;
                    if (resizeWid > imp.getWidth()) {
                        xCenter = (resizeWid - imp.getWidth()) / 2;
                    }
                    if (resizeHeight > imp.getHeight()) {
                        yCenter = (resizeHeight - imp.getHeight()) / 2;
                    }
                    inputStack = resizeStack(inputStack, resizeWid, resizeHeight, xCenter, yCenter);
                }
                for (int slice = 1; slice <= inputStack.getSize(); slice++) {
                    ImageProcessor ip = inputStack.getProcessor(slice);
                    if (slice == 1) {
                        count++;
                    }
                    if (ip.getMin() < min) {
                        min = ip.getMin();
                    }
                    if (ip.getMax() > max) {
                        max = ip.getMax();
                    }
                    stack.addSlice(ip);
                }
                if (count >= n) {
                    break;
                }
            }
            if (stack != null && stack.getSize() > 0) {
                ImagePlus imagePlus = new ImagePlus(title, stack);
                if (imagePlus.getType() == ImagePlus.GRAY16 || imagePlus.getType() == ImagePlus.GRAY32) {
                    imagePlus.getProcessor().setMinAndMax(min, max);
                }
                imagePlus.setFileInfo(fi);
                if (allSameCalibration) {
                    imagePlus.setCalibration(cal);
                }
                if (imagePlus.getStackSize() == 1 && info1 != null) {
                    imagePlus.setProperty("Info", info1);
                }
                imagesPlus = imagePlus;
            }
        } catch (OutOfMemoryError e) {
            log.warn("Got an out of memory error", e);
        } finally {
            if (zipped) {
                FileUtils.deleteQuietly(rootPath.toFile());
            }
        }
        return imagesPlus;
    }

    /**
     * @param scan
     * @param cachepaths
     * @param montageFlag
     * @param gridview
     *
     * @return
     *
     * @throws Exception
     */
    public Pair<File, File> createSnapshotImage(XnatImagescandataBean scan, String cachepaths, boolean montageFlag, String gridview) throws Exception {
        ImagePlus baseimage     = getImagePlus();
        File      snapshotFile  = null;
        File      thumbnailFile = null;
        ImagePlus snapshot      = getSnapshot(baseimage, montageFlag, gridview);

        if (snapshot != null) {
            BitConverter converter = new BitConverter();
            converter.convertTo8BitColor(snapshot);
            final Pair<String, String> names    = getSnapshotName(scan, gridview);
            final String               fileName = names.getKey();
            final String  filePath = Paths.get(cachepaths, fileName).toString();
            final boolean saved    = new PlexiFileSaver(snapshot.getImage()).saveImageAsGif(filePath);
            if (!saved) {
                throw new Exception("Couldn't save file snapshot for session " + scan.getImageSessionId() + " scan " + scan.getId() + " at the location " + filePath);
            }
            snapshotFile = Paths.get(filePath).toFile();
            final String thumbnailPath = Paths.get(cachepaths, names.getValue()).toString();
            if (!generateThumbnail(snapshot, thumbnailPath)) {
                log.warn("Couldn't save file thumbnail for session " + scan.getImageSessionId() + " scan " + scan.getId() + " at the location " + thumbnailPath + ", although I think the snapshot image is there.");
            }
            thumbnailFile = Paths.get(thumbnailPath).toFile();
        }
        return Pair.of(snapshotFile, thumbnailFile);
    }

    /**
     * @param baseimage
     * @param montage
     * @param gridview
     *
     * @return
     *
     * @throws Exception
     */
    private ImagePlus getSnapshot(ImagePlus baseimage, boolean montage, String gridview) throws Exception {
        ImagePlus rtn = null;
        if (montage) {
            rtn = createMontage(baseimage, gridview);
        } else {
            if (baseimage != null) {
                int sliceNo = 5;
                if (baseimage.getStackSize() == 1) {
                    sliceNo = 1;
                } else if (baseimage.getStackSize() < sliceNo) {
                    sliceNo = 2;
                }
                baseimage.setSlice(sliceNo);
                baseimage.updateImage();
                baseimage.getProcessor().setColor(Color.WHITE);
                baseimage.getProcessor().setFont(new Font("Serif", Font.BOLD, 10));
                baseimage.getProcessor().drawString("Frame: " + sliceNo, baseimage.getWidth(),
                                                    baseimage.getHeight());
                baseimage.updateImage();
                rtn = baseimage;
            }
        }
        return rtn;
    }

    /**
     * @param image
     * @param gridViews
     *
     * @return
     *
     * @throws Exception
     */
    private ImagePlus createMontage(ImagePlus image, String gridViews) throws Exception {
        int               columns = 1;
        int               rows    = 1;
        PlexiMontageMaker mm      = new PlexiMontageMaker();
        try {
            if (gridViews != null && !gridViews.isEmpty()) {
                String[] rowCol = gridViews.toUpperCase().split("X");
                rows = Integer.parseInt(rowCol[0]);
                columns = Integer.parseInt(rowCol[1]);
            }
        } catch (NumberFormatException ex) {
            log.error("Error createMontage :: " + ex.getMessage());
            throw new NumberFormatException("Provide valid Grid views ROWXCOL paramter");

        } catch (Exception ex) {
            log.error("Error createMontage :: " + ex.getMessage());
            throw new Exception("Provide valid Grid views ROWXCOL paramter");
        }

        if (image.getStackSize() <= 0) {
            log.error("Error createMontage :: DICOM Image does not exist ");
            throw new Exception("DICOM Image does not exist");
        }

        final Hashtable<?, ?> attribs = ImageUtils.getSliceIncrement(image, columns * rows);
        final IntensitySetter is      = new IntensitySetter(image, true);
        is.autoAdjust(image, image.getProcessor());

        image = mm.makeMontage(image, columns, rows, 1.0, (Integer) attribs.get("startslice"), rows == 1 && columns == 1 ? image.getStackSize() : (Integer) attribs.get("endslice"), (Integer) attribs.get("increment"), true, false);
        image.getProcessor().resetMinAndMax();
        return image;
    }

    /**
     * @return
     */
    private String getResizeDimensionCalc() {
        Map<String, Integer> dimList   = new HashMap<>();
        String               dimResult = null;
        for (final String file : files) {
            Opener opener = new Opener();
            opener.setSilentMode(true);
            ImagePlus impagePlug = opener.openImage(rootPath.resolve(file).toString());
            Integer   maxCount   = 0;
            if (impagePlug != null) {
                String  dimension = impagePlug.getWidth() + "X" + impagePlug.getHeight();
                Integer dimCount  = dimList.get(dimension);
                dimList.put(dimension, (dimCount == null) ? 1 : dimCount + 1);
                if (dimList.size() > 1) {
                    if (dimList.get(dimension) > maxCount) {
                        dimResult = dimension;
                    }
                }
            }
        }
        return dimResult;
    }

    /**
     * @param stackOld
     * @param widthNew
     * @param heightNew
     * @param xOff
     * @param yOff
     *
     * @return
     */
    private ImageStack resizeStack(ImageStack stackOld, int widthNew, int heightNew, int xOff, int yOff) {
        int            nFrames   = stackOld.getSize();
        ImageProcessor imProcOld = stackOld.getProcessor(1);
        Color          colorBack = Toolbar.getBackgroundColor();
        ImageStack     stackNew  = new ImageStack(widthNew, heightNew, stackOld.getColorModel());
        ImageProcessor imProcNew;
        for (int i = 1; i <= nFrames; i++) {
            IJ.showProgress((double) i / nFrames);
            imProcNew = imProcOld.createProcessor(widthNew, heightNew);
            imProcNew.setColor(colorBack);
            imProcNew.fill();
            imProcNew.insert(stackOld.getProcessor(i), xOff, yOff);
            stackNew.addSlice(null, imProcNew);
        }
        return stackNew;
    }

    private boolean generateThumbnail(final ImagePlus baseImage, final String filePath) {
        baseImage.setStack("", new StackProcessor(baseImage.getStack(), baseImage.getProcessor()).resize(baseImage.getWidth() / 2, baseImage.getHeight() / 2));
        return new PlexiFileSaver(baseImage.getImage()).saveImageAsGif(filePath);
    }
}
