/*
 * web: org.nrg.xnat.helpers.merge.MergePrearcToArchiveSession
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.merge;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultError;
import org.nrg.dicom.mizer.objects.AnonymizationResultNoOp;
import org.nrg.xdat.model.*;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.preferences.HandlePetMr;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.archive.ArchivingException;
import org.nrg.xnat.archive.XNATSessionBuilder;
import org.nrg.xnat.helpers.prearchive.PrearcSession;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.turbine.utils.XNATSessionPopulater;
import org.nrg.xnat.utils.CatalogUtils;
import org.restlet.data.Status;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Boolean.TRUE;
import static org.nrg.xdat.preferences.HandlePetMr.SEPARATE_PET_MR;
import static org.nrg.xnat.helpers.prearchive.PrearcDatabase.removePrearcVariables;

@Slf4j
public class MergePrearcToArchiveSession extends MergeSessionsA<XnatImagesessiondata> {
    public static final String PROJECT               = "project";
    public static final String LABEL                 = "label";
    public static final String SUBJECT_ID            = "subject_ID";
    public static final String DEFAULT_BACKUP_FOLDER = "merge";
    public static final String CATALOG_BACKUP        = "catalog_bk";
    public static final String XML_EXTENSION         = ".xml";

    private final PrearcSession _prearcSession;

    public MergePrearcToArchiveSession(Object control, final PrearcSession prearcSession, final XnatImagesessiondata src, final String srcRootPath, final File destDIR, final XnatImagesessiondata existing, final String destRootPath, boolean addFilesToExisting, boolean overwrite_files, SaveHandlerI<XnatImagesessiondata> saver, final UserI u, final EventMetaI now) {
        super(control, prearcSession.getSessionDir(), src, srcRootPath, destDIR, existing, destRootPath, addFilesToExisting, overwrite_files, saver, u, now);
        setAnonymizer(new PrearcSessionAnonymizer(src, src.getProject(), srcDIR.getAbsolutePath(), false));
        _prearcSession = prearcSession;
    }

    @Override
    public String getCacheBKDirName() {
        return DEFAULT_BACKUP_FOLDER;
    }

    @Override
    public void finalize(final XnatImagesessiondata session) {
        PrearcUtils.setupScans(session, destRootPath);
    }

    @Override
    public void postSave(final XnatImagesessiondata session) {
        PrearcUtils.cleanupScans(session, destRootPath, c);
    }

    @Override
    public MergeSessionsA.Results<XnatImagesessiondata> mergeSessions(final XnatImagesessiondata original, final String sourcePath, final XnatImagesessiondata destination, final String destinationPath, final File backupFolder) throws ClientException, ServerException {
        if (destination == null) {
            return new Results<>(original);
        }

        final Results<XnatImagesessiondata> results            = new Results<>();
        final List<XnatImagescandataI>      sourceScans        = original.getScans_scan();
        final List<XnatImagescandataI>      destinationScans   = destination.getScans_scan();
        final String                        sourceProject      = original.getProject();
        final String                        destinationProject = destination.getProject();

        final List<File> toDelete = new ArrayList<>();
        processing("Merging new meta-data into existing meta-data.");
        try {
            for (final XnatImagescandataI sourceScan : sourceScans) {
                final XnatImagescandataI destinationScan = MergeUtils.getMatchingScan(sourceScan, destinationScans);
                if (destinationScan == null) {
                    destination.addScans_scan(sourceScan);
                } else {
                    final List<XnatAbstractresourceI> destinationScanResources = destinationScan.getFile();
                    for (final XnatAbstractresourceI sourceScanResource : sourceScan.getFile()) {
                        final XnatAbstractresourceI destinationScanResource = MergeUtils.getMatchingResource(sourceScanResource, destinationScanResources);
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

        if (StringUtils.equals(original.getXSIType(), destination.getXSIType())) {
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
        boolean wasAnonymized = anonymizeSession();

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
        final HandlePetMr separatePetMr = HandlePetMr.getSeparatePetMr(params.get(PROJECT));
        if (separatePetMr != HandlePetMr.Default) {
            params.put(SEPARATE_PET_MR, separatePetMr.value());
        }

        final Map<String, Object> sessionValues = removePrearcVariables(_prearcSession.getAdditionalValues());
        for (Map.Entry<String, Object> entry : sessionValues.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            params.put(key, value instanceof String ? (String) value : value.toString());
        }

        final Boolean sessionRebuildSuccess = new XNATSessionBuilder(srcDIR, sessionXml, true, params).call();
        if (Boolean.TRUE.equals(!sessionRebuildSuccess || !sessionXml.exists()) || sessionXml.length() == 0) {
            throw new ServerException("Something went wrong: I anonymized the data in " + srcDIR.getPath() + " but something failed during the session rebuild.");
        }

        final XnatImagesessiondata session = new XNATSessionPopulater(user, sessionXml, src.getProject(), false).populate();
        session.setId(src.getId());
        return session;
    }

    private boolean anonymizeSession() throws Exception {
        if (TRUE.equals(_prearcSession.getSessionData().getPreventAnon())) {
            return false;
        }
        final List<AnonymizationResult> anonResults = anonymizer.call();
        if (anonResults.stream().anyMatch(AnonymizationResultError.class::isInstance)) {
            log.error("Anonymization failed for prearcSession at {} ", _prearcSession.getSessionDir().getAbsolutePath());
            throw new ArchivingException("Anonymization failed for prearcSession at " + _prearcSession.getSessionDir().getAbsolutePath());
        }
        if (anonResults.stream().allMatch(AnonymizationResultNoOp.class::isInstance)) {
            return false;
        }
        MergeUtils.deleteRejectedFiles(log, anonResults, src.getProject());
        return true;
    }

    private String getSubjectId(final XnatImagesessiondata session) {
        return Optional.ofNullable(session.getSubjectId()).orElseGet(() -> session.getSubjectData() != null ? session.getSubjectData().getId() : "");
    }
}

