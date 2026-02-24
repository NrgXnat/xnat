package org.nrg.xnat.archive;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.framework.status.StatusProducer;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xft.XFTItem;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.helpers.merge.MergeUtils;
import org.nrg.xnat.helpers.prearchive.PrearcSession;
import org.nrg.xnat.utils.CatalogUtils;
import org.restlet.data.Status;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static org.nrg.dcm.xnat.CatalogBuilder.RESOURCE_LABEL_DICOM;
import static org.nrg.dcm.xnat.CatalogBuilder.SCANS_DIR;

@Slf4j
public class ScanIdValidator extends StatusProducer implements Callable<Boolean> {
    private static final File NULL_FILE;

    static {
        try {
            NULL_FILE = Files.createTempFile("null-", ".txt").toFile();
            NULL_FILE.deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final XnatImagesessiondata existing;
    private final XnatImagesessiondata source;
    private final PrearcSession        prearcSession;
    private final boolean              allowSessionMerge;
    private final boolean              overrideExceptions;
    private final boolean              needsScanIdCorrection;

    /**
     * Creates a new scan ID validator in "default" mode. This just returns <pre>false</pre> when
     * {@link #needsScanIdCorrection()} is called. This should be used when no existing session is
     * present with which to check for conflicts.
     *
     * @param control The control object for status reporting.
     *
     * @throws ClientException When an error occurs during validation.
     */
    public ScanIdValidator(final Object control) throws ClientException {
        this(control, null, null, null, false, false);
    }

    public ScanIdValidator(final Object control, final XnatImagesessiondata existing, final XnatImagesessiondata source, final PrearcSession prearcSession, final boolean allowSessionMerge, final boolean overrideExceptions) throws ClientException {
        super(control);
        this.existing           = existing;
        this.source             = source;
        this.prearcSession      = prearcSession;
        this.allowSessionMerge  = allowSessionMerge;
        this.overrideExceptions = overrideExceptions;

        // Figure out if we need scan ID correction
        needsScanIdCorrection = validate();
    }

    /**
     * Performs the scan ID correction, if needed.
     *
     * @return Returns <pre>true</pre> if any scan IDs were modified, <pre>false</pre> otherwise.
     *
     * @throws ServerException When an error occurs during scan ID correction.
     */
    @Override
    public Boolean call() throws ServerException {
        if (!needsScanIdCorrection) {
            return false;
        }

        final List<List<XnatImagescandataI>> preexistingMatches = new ArrayList<>();

        final List<String> usedIds = existing.getScans_scan().stream().map(XnatImagescandataI::getId).collect(Collectors.toList());

        for (final XnatImagescandataI newScan : source.getScans_scan()) {
            //build modality code via parsing of the xsi:type.  modality code matches first 2 characters after the : for xnat types.  Otherwise, leave it empty.
            //this is a bit of a hack.  It would be better to have an official mapping
            String modalityCode = (newScan.getXSIType().startsWith("xnat:")) ? newScan.getXSIType().substring(5, 7).toUpperCase() : "";
            if ("PE".equals(modalityCode)) {
                modalityCode = "PT";//this works for everything but PET, which gets called PE instead of PT, so we correct it.
            }

            //find matching scan by UID
            final XnatImagescandataI match2 = MergeUtils.getMatchingScanByUID(newScan, existing.getScans_scan());//match by UID
            if (match2 != null) {
                if ((!StringUtils.equals(match2.getId(), newScan.getId())) || match2.getId().contains("-" + modalityCode)) {
                    //this UID has been mapped to a different scan ID (or possibly different file system path)
                    //update the prearc session to match
                    //place them in an array and process them after the others, to avoid temporary conflicts
                    preexistingMatches.add(Arrays.asList(newScan, match2));
                }
                //scan with matching UID is done (whether their ID's matched or not)
                continue;
            }

            final String originalScanId = newScan.getId();
            String       scanId         = newScan.getId();
            String       scanStub       = null;
            int          count          = 1;
            boolean      needsMove      = false;

            if (scanId.matches("^.*-" + modalityCode + "[0-9]+$")) {
                scanStub = scanId.substring(0, scanId.lastIndexOf("-"));
            }

            // Make sure there aren't any matches by ID. If there aren't needsMove stays false. And it identifies a good scan ID to use in the process.
            while (usedIds.contains(scanId)) {
                scanId    = scanStub != null
                            ? scanStub + "-" + modalityCode + count++
                            : newScan.getId() + "-" + modalityCode + count++;
                needsMove = true;
            }

            usedIds.add(scanId);

            if (needsMove) {
                //the scan id conflicted with a pre-existing one, so we have to rename this one.
                processing("Renaming scan " + newScan.getId() + " to " + scanId + " due to ID conflict.");
                moveScan(newScan, scanId, originalScanId, null);
            }
        }

        //process previously matched scans
        for (final List<XnatImagescandataI> preexistingMatch : preexistingMatches) {
            final XnatImagescandataI newScan = preexistingMatch.get(0);
            final XnatImagescandataI match   = preexistingMatch.get(1);

            //use same catalog path as existing resource
            final XnatResourcecatalog cat          = (XnatResourcecatalog) match.getFile().get(0);
            final String              archivedPath = cat.getUri();
            final String              partialPath  = archivedPath.substring(archivedPath.lastIndexOf(SCANS_DIR));

            processing("Renaming scan " + newScan.getId() + " to " + match.getId() + " due to UID match.");
            moveScan(newScan, match.getId(), newScan.getId(), partialPath);

            usedIds.add(match.getId());
        }
        return true;
    }

    /**
     * Indicates whether scan ID correction is needed.
     *
     * @return Returns <pre>true</pre> if scan ID correction is needed, <pre>false</pre> otherwise.
     */
    public boolean needsScanIdCorrection() {
        return needsScanIdCorrection;
    }

    private boolean validate() throws ClientException {
        if (existing == null) {
            return false;
        }

        boolean needsScanIdCorrection = false;
        for (final XnatImagescandataI newScan : source.getScans_scan()) {
            final XnatImagescandataI matchById = MergeUtils.getMatchingScanById(newScan.getId(), existing.getScans_scan());//match by ID
            if (matchById != null) {
                if (StringUtils.equals(matchById.getUid(), newScan.getUid())) {
                    if (!allowSessionMerge) {
                        throw new ClientException(Status.CLIENT_ERROR_CONFLICT, "Session already contains a scan (" + matchById.getId() + ") with the same UID and number.", new Exception());
                    }
                } else if (StringUtils.isNotEmpty(matchById.getUid())) {
                    if (!allowSessionMerge) {
                        throw new ClientException(Status.CLIENT_ERROR_CONFLICT, "Session already contains a scan (" + matchById.getId() + ") with the same number, but a different UID.", new Exception());
                    } else {
                        needsScanIdCorrection = true;
                    }
                }
            }
            final XnatImagescandataI matchByUid = MergeUtils.getMatchingScanByUID(newScan, existing.getScans_scan());//match by UID
            if (matchByUid != null) {
                if (matchById == null || !StringUtils.equals(matchById.getId(), newScan.getId())) {
                    if (!overrideExceptions) {
                        throw new ClientException(Status.CLIENT_ERROR_CONFLICT, "Session already contains a scan with the same UID, but a different number (" + matchByUid.getId() + ").", new Exception());
                    }
                }
            }
        }
        return needsScanIdCorrection;
    }

    /**
     * Used to move a scan to a different scan ID within the prearchive, prior to transfer
     *
     * @param srcScan             The new scan to move to.
     * @param destScanId          The new scan ID.
     * @param srcScanId           The original scan ID
     * @param destScanCatalogPath Destination for new catalog
     *
     * @throws ServerException When an error occurs moving the specified scan.
     */
    private void moveScan(final XnatImagescandataI srcScan, final String destScanId, final String srcScanId, String destScanCatalogPath) throws ServerException {
        for (final XnatAbstractresourceI resource : srcScan.getFile()) {
            final XnatResourcecatalog cat                = (XnatResourcecatalog) resource;
            final String              srcScanCatalogPath = cat.getUri();
            final File                srcCatalog         = new File(source.getPrearchivepath(), srcScanCatalogPath);
            final String              srcScanFolderPath  = Paths.get(SCANS_DIR, srcScanId).toString();

            //confirm expected structure
            if (!srcCatalog.exists()) {
                throw new ServerException("Non-standard prearchive structure (no catalog)- failed scan rename.");
            }

            if (destScanId.equals(srcScanId)) {
                if (!srcScanCatalogPath.startsWith(srcScanFolderPath)) {
                    throw new ServerException("Non-standard prearchive structure (invalid catalog location)- failed scan rename.");
                }
            }

            if (destScanCatalogPath == null) {
                if (RESOURCE_LABEL_DICOM.equals(cat.getLabel())) {
                    destScanCatalogPath = Paths.get(SCANS_DIR, destScanId, RESOURCE_LABEL_DICOM,
                                                    "scan_" + destScanId + "_catalog.xml").toString();
                } else {
                    destScanCatalogPath = Paths.get(SCANS_DIR, destScanId, cat.getLabel(),
                                                    "scan_" + destScanId + "_" + cat.getLabel() + "_catalog.xml").toString();
                }
            }

            final File   destCatalogFile       = new File(source.getPrearchivepath(), destScanCatalogPath);
            final String originalScanFolder    = new File(source.getPrearchivepath(), srcScanCatalogPath).getParent();
            final File   destinationScanFolder = destCatalogFile.getParentFile();

            //get catalog bean
            CatalogUtils.CatalogData catalogData = new CatalogUtils.CatalogData(srcCatalog, prearcSession.getProject(), false);
            //move each entry to its new location
            final Map<File, File> fileMap = catalogData.catBean.getEntries_entry().stream().collect(
                                                               Collectors.toMap(entry -> new File(destinationScanFolder, entry.getUri()),
                                                                                entry -> ObjectUtils.defaultIfNull(CatalogUtils.getFile(entry, originalScanFolder, prearcSession.getProject()), NULL_FILE)))
                                                               .entrySet().stream()
                                                               .filter(entry -> !entry.getValue().equals(NULL_FILE)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            for (final Map.Entry<File, File> entry : fileMap.entrySet()) {
                final File source      = entry.getValue();
                final File destination = entry.getKey();
                try {
                    FileUtils.MoveFile(source, destination, false);
                } catch (IOException e) {
                    throw new ServerException("An error occurred trying to move the file " + source.getAbsolutePath() +
                                              " to the destination " + destination.getAbsolutePath(), e);
                }
            }

            //move catalog file
            try {
                if (!StringUtils.equals(srcCatalog.getAbsolutePath(), destCatalogFile.getAbsolutePath())) {
                    FileUtils.MoveFile(srcCatalog, destCatalogFile, true);
                }
            } catch (IOException e) {
                throw new ServerException(e);
            }

            // fix the file path
            cat.setUri(destScanCatalogPath);
        }

        srcScan.setId(destScanId);
        try {
            updatePrearchiveSessionXML(prearcSession.getSessionDir().getAbsolutePath(), source);
        } catch (Throwable e) {
            throw new ServerException(e);
        }
    }

    /**
     * Updates the prearchive session XML, if possible. Errors here are logged but not
     * otherwise handled; messing up the prearchive session XML is not a disaster.
     *
     * @param prearcSessionPath path of session directory in prearchive
     */
    private void updatePrearchiveSessionXML(final String prearcSessionPath, final XnatImagesessiondata newSession) {
        final File prearcSessionDir = new File(prearcSessionPath);
        try (final FileWriter prearcXML = new FileWriter(prearcSessionDir.getPath() + ".xml")) {
            log.debug("Preparing to update prearchive XML for {}", newSession);
            ((XFTItem) newSession.getItem().clone()).toXML(prearcXML, false);
        } catch (RuntimeException e) {
            log.error("unable to update prearchive session XML", e);
            warning("updated prearchive session XML could not be written: " + e.getMessage());
        } catch (SAXException e) {
            log.error("attempted to write invalid updated prearchive session XML", e);
            warning("updated prearchive session XML is invalid: " + e.getMessage());
        } catch (FileNotFoundException e) {
            log.error("unable to update prearchive session XML", e);
            warning("prearchive session XML not found, cannot update");
        } catch (IOException e) {
            log.error("error updating prearchive session XML", e);
            warning("could not update prearchive session XML: " + e.getMessage());
        }
    }
}
