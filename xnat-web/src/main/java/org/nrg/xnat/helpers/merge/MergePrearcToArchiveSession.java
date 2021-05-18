/*
 * web: org.nrg.xnat.helpers.merge.MergePrearcToArchiveSession
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.merge;

import static org.nrg.xnat.helpers.prearchive.PrearcDatabase.removePrearcVariables;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xdat.model.*;
import org.nrg.xdat.om.*;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.archive.XNATSessionBuilder;
import org.nrg.xnat.helpers.prearchive.PrearcSession;
import org.nrg.xnat.turbine.utils.XNATSessionPopulater;
import org.nrg.xnat.utils.CatalogUtils;
import org.restlet.data.Status;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;

@Slf4j
public class MergePrearcToArchiveSession extends MergeSessionsA<XnatImagesessiondata> {
    public static final String PROJECT               = "project";
    public static final String LABEL                 = "label";
    public static final String SUBJECT_ID            = "subject_ID";
    public static final String DEFAULT_BACKUP_FOLDER = "merge";
    public static final String CATALOG_BACKUP        = "catalog_bk";
    public static final String XML_EXTENSION         = ".xml";
    public static final String CONTENT_RAW           = "RAW";

    private final PrearcSession _prearcSession;

    public MergePrearcToArchiveSession(Object control, final PrearcSession prearcSession, final XnatImagesessiondata src, final String srcRootPath, final File destDIR, final XnatImagesessiondata existing, final String destRootPath, boolean addFilesToExisting, boolean overwrite_files, SaveHandlerI<XnatImagesessiondata> saver, final UserI u, final EventMetaI now) {
        super(control, prearcSession.getSessionDir(), src, srcRootPath, destDIR, existing, destRootPath, addFilesToExisting, overwrite_files, saver, u, now);
        setAnonymizer(new PrearcSessionAnonymizer(src, src.getProject(), srcDIR.getAbsolutePath()));
        _prearcSession = prearcSession;
    }

    @Override
    public String getCacheBKDirName() {
        return DEFAULT_BACKUP_FOLDER;
    }

    @Override
    public void finalize(final XnatImagesessiondata session) {
        final String root = fixRootPath();
        for (XnatImagescandataI scan : session.getScans_scan()) {
            for (final XnatAbstractresourceI resource : scan.getFile()) {
                updateResourceWithArchivePathAndPopulateStats((XnatAbstractresource) resource, root, true);
            }
        }
        for (final XnatAbstractresourceI resource : session.getResources_resource()) {
            updateResourceWithArchivePathAndPopulateStats((XnatAbstractresource) resource, root, false);
        }
    }

    @Override
    public void postSave(final XnatImagesessiondata session) {
        final String          root      = fixRootPath();
        final XnatProjectdata project   = session.getProjectData();
        final boolean         checksums = getChecksumConfiguration(project);

        for (final XnatImagescandataI scan : session.getScans_scan()) {
            for (final XnatAbstractresourceI file : scan.getFile()) {
                if (file instanceof XnatResourcecatalog) {
                    final XnatResourcecatalog catalog = (XnatResourcecatalog) file;
                    try {
                        final CatalogUtils.CatalogData catalogData = CatalogUtils.CatalogData.getOrCreate(root, catalog, project.getId());
                        if (CatalogUtils.formalizeCatalog(catalogData.catBean, catalogData.catPath, catalogData.project, user, c, checksums, false)) {
                            CatalogUtils.writeCatalogToFile(catalogData, checksums);
                        }
                    } catch (Exception exception) {
                        log.error("An error occurred trying to write catalog data for {}", ((XnatResourcecatalog) file).getUri(), exception);
                    }
                }
            }
        }
    }

    @Override
    public MergeSessionsA.Results<XnatImagesessiondata> mergeSessions(final XnatImagesessiondata original, final String sourcePath, final XnatImagesessiondata destination, final String destinationPath, final File backupFolder) throws ClientException, ServerException {
        if (destination == null) {
            return new Results<>(original);
        }

        final Results<XnatImagesessiondata> results            = new Results<>();
        final List<XnatImagescandataI>      destinationScans   = destination.getScans_scan();
        final String                        sourceProject      = original.getProject();
        final String                        destinationProject = destination.getProject();

        processing("Merging new meta-data into existing meta-data.");

        final List<File> toDelete = new ArrayList<>();
        try {
            for (final XnatImagescandataI sourceScan : original.getScans_scan()) {
                final XnatImagescandataI destinationScan = MergeUtils.getMatchingScan(sourceScan, destinationScans);
                if (destinationScan == null) {
                    destination.addScans_scan(sourceScan);
                } else {
                    final List<XnatAbstractresourceI> destinationResources = destinationScan.getFile();
                    for (final XnatAbstractresourceI sourceScanResource : sourceScan.getFile()) {
                        final XnatAbstractresourceI destinationScanResource = MergeUtils.getMatchingResource(sourceScanResource, destinationResources);
                        if (destinationScanResource instanceof XnatResourcecatalogI) {
                            final MergeSessionsA.Results<File> result = mergeCatalogs(sourceProject, sourcePath, (XnatResourcecatalogI) sourceScanResource, destinationProject, destinationPath, (XnatResourcecatalogI) destinationScanResource);
                            if (result != null) {
                                toDelete.add(result.result);
                                results.addAll(result);
                            } else {
                                CatalogUtils.populateStats((XnatAbstractresource) sourceScanResource, sourcePath);
                            }
                        } else if (destinationScanResource instanceof XnatResourceseriesI) {
                            sourceScanResource.setLabel(sourceScanResource.getLabel() + "2");
                            sourceScan.addFile(destinationScanResource);
                            destinationScan.addFile(sourceScanResource);
                        } else if (destinationScanResource instanceof XnatResourceI) {
                            sourceScanResource.setLabel(sourceScanResource.getLabel() + "2");
                            sourceScan.addFile(destinationScanResource);
                            destinationScan.addFile(sourceScanResource);
                        } else {
                            destinationScan.addFile(sourceScanResource);
                        }
                    }
                }
            }
        } catch (MergeCatCatalog.DCMEntryConflict e) {
            failed("Duplicate DCM UID cannot be merged at this time.");
            throw new ClientException(Status.CLIENT_ERROR_CONFLICT, e.getMessage(), e);
        } catch (Exception e) {
            failed("Failed to merge upload into existing data.");
            throw new ServerException(e.getMessage(), e);
        }

        final File backup = new File(backupFolder, CATALOG_BACKUP);
        if (!backup.mkdirs() && !backup.exists()) {
            throw new ServerException("Unable to create back-up folder: " + backup.getAbsolutePath());
        }

        if (original.getXSIType().equals(destination.getXSIType())) {
            try {
                original.copyValuesFrom(destination);
            } catch (Exception e) {
                failed("Failed to merge upload into existing data.");
                throw new ServerException(e.getMessage(), e);
            }
            results.setResult(original);
        } else {
            results.setResult(destination);
        }
        results.getBeforeDirMerge().add(() -> {
            try {
                final AtomicInteger count = new AtomicInteger();
                for (final File file : toDelete) {
                    final File catalogBackupFolder = new File(backup, Integer.toString(count.getAndIncrement()));
                    if (!catalogBackupFolder.mkdirs() && !catalogBackupFolder.exists()) {
                        throw new ServerException("Unable to create back-up folder: " + catalogBackupFolder.getAbsolutePath());
                    }
                    FileUtils.MoveFile(file, new File(catalogBackupFolder, file.getName()), false);
                }
                return Boolean.TRUE;
            } catch (Exception e) {
                throw new ServerException(e.getMessage(), e);
            }
        });
        return results;
    }

    @Override
    protected XnatImagesessiondata getPostAnonSession() throws Exception {
        // Now that we're at the project level, let's re-anonymize.
        final boolean wasAnonymized = !_prearcSession.getSessionData().getPreventAnon() && anonymizer.call();

        final File sessionXml = new File(srcDIR.getPath() + XML_EXTENSION);

        // If anonymization wasn't performed or the session XML doesn't exist yet...
        if (!wasAnonymized || !sessionXml.exists()) {
            // Return the original session XML.
            return src;
        }

        // Otherwise, we need to rebuild the session XML to match the anonymized DICOM.
        final Map<String, String> params = new LinkedHashMap<>();
        params.put(PROJECT, StringUtils.defaultString(src.getProject(), ""));
        params.put(LABEL, StringUtils.defaultString(src.getLabel(), ""));
        params.put(SUBJECT_ID, getSubjectId(src));

        final Map<String, Object> sessionValues = removePrearcVariables(_prearcSession.getAdditionalValues());
        for (final String key : sessionValues.keySet()) {
            final Object value = sessionValues.get(key);
            if (value == null) {
                continue;
            }
            params.put(key, value instanceof String ? (String) value : value.toString());
        }

        final Boolean sessionRebuildSuccess = new XNATSessionBuilder(srcDIR, sessionXml, true, params).call();
        if (!sessionRebuildSuccess || !sessionXml.exists() || sessionXml.length() == 0) {
            throw new ServerException("Something went wrong: I anonymized the data in " + srcDIR.getPath() + " but something failed during the session rebuild.");
        }

        final XnatImagesessiondata session = new XNATSessionPopulater(user, sessionXml, src.getProject(), false).populate();
        session.setId(src.getId());
        return session;
    }

    @Nonnull
    private String fixRootPath() {
        return StringUtils.appendIfMissing(StringUtils.replaceChars(destRootPath, '\\', '/'), "/");
    }

    private boolean getChecksumConfiguration(final XnatProjectdata project) {
        try {
            return CatalogUtils.getChecksumConfiguration(project);
        } catch (ConfigServiceException e) {
            return false;
        }
    }

    private String getSubjectId(final XnatImagesessiondata session) {
        return Optional.ofNullable(session.getSubjectId()).orElseGet(() -> session.getSubjectData() != null ? session.getSubjectData().getId() : "");
    }

    private void updateResourceWithArchivePathAndPopulateStats(final XnatAbstractresource resource, final String root, final boolean setContentToRawIfMissing) {
        resource.prependPathsWith(root);
        if (setContentToRawIfMissing && StringUtils.isBlank(resource.getContent())) {
            ((XnatResource) resource).setContent(CONTENT_RAW);
        }
        if (resource instanceof XnatResourcecatalog) {
            ((XnatResourcecatalog) resource).clearFiles();
        }
        CatalogUtils.populateStats(resource, root);
    }
}
