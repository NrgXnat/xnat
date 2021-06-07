/*
 * web: org.nrg.xnat.helpers.merge.MergePrearcToArchiveSession
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.merge;

import com.google.common.collect.Maps;
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
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.turbine.utils.XNATSessionPopulater;
import org.nrg.xnat.turbine.utils.XNATUtils;
import org.nrg.xnat.utils.CatalogUtils;
import org.restlet.data.Status;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.nrg.xnat.helpers.prearchive.PrearcDatabase.removePrearcVariables;

@Slf4j
public class MergePrearcToArchiveSession extends MergeSessionsA<XnatImagesessiondata> {
    public MergePrearcToArchiveSession(Object control, final PrearcSession prearcSession, final XnatImagesessiondata src, final String srcRootPath, final File destDIR, final XnatImagesessiondata existing, final String destRootPath, boolean addFilesToExisting, boolean overwrite_files, SaveHandlerI<XnatImagesessiondata> saver, final UserI u, final EventMetaI now) {
        super(control, prearcSession.getSessionDir(), src, srcRootPath, destDIR, existing, destRootPath, addFilesToExisting, overwrite_files, saver, u, now);
        setAnonymizer(new PrearcSessionAnonymizer(src, src.getProject(), srcDIR.getAbsolutePath()));
        _prearcSession = prearcSession;
    }

    @Override
    public String getCacheBKDirName() {
        return "merge";
    }

    @Override
    public void finalize(final XnatImagesessiondata session) {
        PrearcUtils.setupScans(session,  destRootPath.replace('\\', '/'));
    }

    @Override
    public void postSave(final XnatImagesessiondata session) {
        PrearcUtils.cleanupScans(session, destRootPath.replace('\\', '/'), c);
    }

    @Override
    public MergeSessionsA.Results<XnatImagesessiondata> mergeSessions(final XnatImagesessiondata src, final String srcRootPath, final XnatImagesessiondata dest, final String destRootPath, final File rootBackUp) throws ClientException, ServerException {
        if (dest == null) {
            return new Results<>(src);
        }

        final Results<XnatImagesessiondata> results   = new Results<>();
        final List<XnatImagescandataI>      srcScans  = src.getScans_scan();
        final List<XnatImagescandataI>      destScans = dest.getScans_scan();
        final String srcProject  = src.getProject();
        final String destProject = dest.getProject();

        final List<File> toDelete = new ArrayList<>();
        processing("Merging new meta-data into existing meta-data.");
        try {
            for (final XnatImagescandataI srcScan : srcScans) {
                final XnatImagescandataI destScan = MergeUtils.getMatchingScan(srcScan, destScans);
                if (destScan == null) {
                    dest.addScans_scan(srcScan);
                } else {
                    final List<XnatAbstractresourceI> source      = srcScan.getFile();
                    final List<XnatAbstractresourceI> destination = destScan.getFile();

                    for (final XnatAbstractresourceI srcRes : source) {
                        final XnatAbstractresourceI destRes = MergeUtils.getMatchingResource(srcRes, destination);
                        if (destRes == null) {
                            destScan.addFile(srcRes);
                        } else {
                            if (destRes instanceof XnatResourcecatalogI) {
                                MergeSessionsA.Results<File> r = mergeCatalogs(srcProject, srcRootPath, (XnatResourcecatalogI) srcRes,
                                        destProject, destRootPath, (XnatResourcecatalogI) destRes);
                                if (r != null) {
                                    toDelete.add(r.result);
                                    results.addAll(r);
                                } else {
                                    CatalogUtils.populateStats((XnatAbstractresource) srcRes, srcRootPath);
                                }
                            } else if (destRes instanceof XnatResourceseriesI) {
                                srcRes.setLabel(srcRes.getLabel() + "2");
                                srcScan.addFile(destRes);

                                destScan.addFile(srcRes);
                            } else if (destRes instanceof XnatResourceI) {
                                srcRes.setLabel(srcRes.getLabel() + "2");
                                srcScan.addFile(destRes);

                                destScan.addFile(srcRes);
                            }
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

        final File backup = new File(rootBackUp, "catalog_bk");
        if (!backup.mkdirs() && !backup.exists()) {
            throw new ServerException("Unable to create back-up folder: " + backup.getAbsolutePath());
        }

        final List<Callable<Boolean>> followup = new ArrayList<>();
        followup.add(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    int count = 0;
                    for (File f : toDelete) {
                        File catBkDir = new File(backup, "" + count++);
                        if (!catBkDir.mkdirs() && !catBkDir.exists()) {
                            throw new ServerException("Unable to create back-up folder: " + catBkDir.getAbsolutePath());
                        }

                        FileUtils.MoveFile(f, new File(catBkDir, f.getName()), false);
                    }
                    return Boolean.TRUE;
                } catch (Exception e) {
                    throw new ServerException(e.getMessage(), e);
                }
            }
        });

        if (src.getXSIType().equals(dest.getXSIType())) {
            try {
                src.copyValuesFrom(dest);
            } catch (Exception e) {
                failed("Failed to merge upload into existing data.");
                throw new ServerException(e.getMessage(), e);
            }

            results.setResult(src);
        } else {
            results.setResult(dest);
        }

        results.getBeforeDirMerge().addAll(followup);
        return results;
    }

    @Override
    protected XnatImagesessiondata getPostAnonSession() throws Exception {
        // Now that we're at the project level, let's re-anonymize.
        final boolean wasAnonymized = !_prearcSession.getSessionData().getPreventAnon() && anonymizer.call();

        final File sessionXml = new File(srcDIR.getPath() + ".xml");

        // If anonymization wasn't performed or the session XML doesn't exist yet...
        if (!wasAnonymized || !sessionXml.exists()) {
            // Return the original session XML.
            return src;
        }

        // Otherwise, we need to rebuild the session XML to match the anonymized DICOM.
        final Map<String, String> params = Maps.newLinkedHashMap();
        params.put("project", StringUtils.defaultString(src.getProject(), ""));
        params.put("label", StringUtils.defaultString(src.getLabel(), ""));
        params.put("subject_ID", getSubjectId(src));

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

    private String getSubjectId(final XnatImagesessiondata session) {
        final XnatSubjectdata subject = session.getSubjectData();
        return subject != null ? subject.getLabel() : "";
    }

    private final PrearcSession _prearcSession;
}
