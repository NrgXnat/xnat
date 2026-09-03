/*
 * web: org.nrg.xnat.archive.GradualDicomImporter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.archive;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.util.TagUtils;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.config.entities.Configuration;
import org.nrg.dcm.Decompress;
import org.nrg.dcm.Restructurer;
import org.nrg.dcm.io.ResumableDicomInputStream;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultError;
import org.nrg.dicom.mizer.objects.AnonymizationResultReject;
import org.nrg.dicom.mizer.objects.Dcm4cheConvert;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.framework.constants.PrearchiveCode;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.ArcProject;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.xnat.Files;
import org.nrg.xnat.archive.services.DirectArchiveSessionService;
import org.nrg.xnat.entities.ArchiveProcessorInstance;
import org.nrg.xnat.helpers.merge.anonymize.DefaultAnonUtils;
import org.nrg.xnat.helpers.prearchive.DatabaseSession;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase.Either;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.SessionFileLockException;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.processor.services.ArchiveProcessorInstanceService;
import org.nrg.xnat.processors.ArchiveProcessor;
import org.nrg.xnat.restlet.actions.importer.ImporterHandler;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerA;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.restlet.util.RequestUtil;
import org.nrg.xnat.services.cache.UserProjectCache;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.restlet.data.Status;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("ThrowFromFinallyBlock")
@Slf4j
@ImporterHandler(handler = ImporterHandlerA.GRADUAL_DICOM_IMPORTER)
public class GradualDicomImporter extends ImporterHandlerA {
    @SuppressWarnings("RedundantThrows")
    public GradualDicomImporter(final Object listenerControl, final UserI user, final FileWriterWrapperI fileWriter, final Map<String, Object> parameters) throws ServerException {
        super(listenerControl, user);
        _user = user;
        _fileWriter = fileWriter;
        _parameters = parameters;
        if (_parameters.containsKey(TSUID_PARAM)) {
            _transferSyntax = (String) _parameters.get(TSUID_PARAM);
        }
        //noinspection unchecked
        _cache = XDAT.getContextService().getBean(UserProjectCache.class);
        _doCustomProcessing = PrearcUtils.parseParam(_parameters, CUSTOM_PROC_PARAM, false);
        _directArchive = PrearcUtils.parseParam(_parameters, DIRECT_ARCHIVE_PARAM, false);
        _directArchiveOverwrite = (String) _parameters.getOrDefault(DIRECT_ARCHIVE_OVERWRITE_PARAM, null);

        // spring beans
        _mizer = XDAT.getContextService().getBeanSafely(MizerService.class);
        _processorInstanceService = XDAT.getContextService().getBeanSafely(ArchiveProcessorInstanceService.class);
        _directArchiveSessionService = XDAT.getContextService().getBeanSafely(DirectArchiveSessionService.class);
        _directArchive &= _directArchiveSessionService != null;
    }

    public static boolean isAutoArchive(Map<String, Object> params) {
        if (params.containsKey(RequestUtil.AA) && (RequestUtil.TRUE.equalsIgnoreCase((String) params.get(RequestUtil.AA)))) {
            return true;
        }
        if (params.containsKey(RequestUtil.AUTO_ARCHIVE) && (RequestUtil.TRUE.equalsIgnoreCase((String) params.get(RequestUtil.AUTO_ARCHIVE)))) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public List<String> call() throws ClientException {
        final String name = _fileWriter.getName();
        final XnatProjectdata project;
        final DicomObjectIdentifier<XnatProjectdata> dicomObjectIdentifier = getIdentifier();
        // DICOM tags are unsigned, but the identifier's tag set is naturally ordered, so a tag above
        // 0x7FFFFFFF sorts first rather than last and last() would miss it. Both the maximum and the
        // comparison against SeriesDescription therefore have to be unsigned, or the read below stops
        // short of the tag and the identifier finds nothing.
        final int maxIdentifierTag = dicomObjectIdentifier.getTags().stream()
                                                          .max(Integer::compareUnsigned)
                                                          .orElse(Tag.SeriesDescription);
        // The last tag the identifier needs, not a stop tag: dcm4che's stop tag is exclusive, so the read
        // below adds the one. Adding it here as well reserved a slot past the tag we actually want.
        final int lastTag = Integer.compareUnsigned(maxIdentifierTag, Tag.SeriesDescription) > 0
                            ? maxIdentifierTag
                            : Tag.SeriesDescription;
        // Populated only when the window above reaches the pixel data, so empty for an ordinary import.
        // See ResumableDicomInputStream.openWithBulkDataOffHeap for why they exist and who owns them.
        final List<File> bulkDataFiles = new ArrayList<>();
        // A processor running a script with alterPixels stages the redacted pixels in a scratch
        // file of its own and points the dataset at that instead. It is not one of the spool files
        // above -- those come from the read, this is written afterwards by the edit -- so deleting
        // them does not delete it. Each round below wraps the dataset afresh, so a registration can
        // be left on any of the wrappers and every one of them has to be released.
        final List<DicomObjectI> processedObjects = new ArrayList<>();
        try (final BufferedInputStream bis = new BufferedInputStream(_fileWriter.getInputStream());
             final DicomInputStream dis = ResumableDicomInputStream.openWithBulkDataOffHeap(bis)) {
            Attributes fmi = dis.readFileMetaInformation();
            final String transferSyntaxUID = null == _transferSyntax ? dis.getTransferSyntax() : _transferSyntax;
            Attributes dataset = new Attributes();
            dis.readAttributes(dataset, -1, lastTag + 1);
            bulkDataFiles.addAll(dis.getBulkDataFiles());
            dis.reset();

            // CStore (DIMSE) has no FMI preamble, so fmi is null; generate complete FMI for file output.
            if (null == fmi || !fmi.contains(Tag.TransferSyntaxUID)) {
                fmi = dataset.createFileMetaInformation(transferSyntaxUID);
            }
            // Merge FMI into dataset so processors see a complete DICOM object.
            // FMI will be split out before writing to file.
            dataset.addAll(fmi);

            DicomObjectI dicomObject = new DicomObjectFactory.MizerDicomObject(dataset);
            processedObjects.add(dicomObject);
            if (_doCustomProcessing & !customProcessing(NAME_OF_LOCATION_AT_BEGINNING_AFTER_DICOM_OBJECT_IS_READ, dicomObject, null)) {
                return returnEmptyList();
            }
            dataset = dicomObject.getAttributes();

            log.trace("handling file with query parameters {}", _parameters);
            try {
                // project identifier is expensive, so avoid if possible
                final Attributes datasetForProject = dataset;
                project = getProject(PrearcUtils.identifyProject(_parameters),
                        () -> dicomObjectIdentifier.getProject(datasetForProject));
            } catch (MalformedURLException e1) {
                log.error("unable to parse supplied destination flag", e1);
                throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, e1);
            }

            final SessionData tempSession = new SessionData();
            tempSession.setProject(project == null ? null : project.getId());
            tempSession.setSubject("");
            tempSession.setFolderName("");

            dicomObject = new DicomObjectFactory.MizerDicomObject(dataset);
            processedObjects.add(dicomObject);
            if (_doCustomProcessing & !customProcessing(NAME_OF_LOCATION_AFTER_PROJECT_HAS_BEEN_ASSIGNED, dicomObject, tempSession)) {
                return returnEmptyList();
            }
            dataset = dicomObject.getAttributes();

            if (Strings.isNullOrEmpty(dataset.getString(Tag.SOPClassUID))) {
                throw new ClientException("object " + name + " contains no SOP Class UID");
            }
            if (Strings.isNullOrEmpty(dataset.getString(Tag.SOPInstanceUID))) {
                throw new ClientException("object " + name + " contains no SOP Instance UID");
            }

            final String studyInstanceUID = dataset.getString(Tag.StudyInstanceUID);
            log.trace("Looking for study {} in project {}", studyInstanceUID, null == project ? null : project.getId());

            String sessionLabel = null;
            if (_parameters.containsKey(URIManager.EXPT_LABEL)) {
                sessionLabel = (String) _parameters.get(URIManager.EXPT_LABEL);
                log.trace("using provided experiment label {}", _parameters.get(URIManager.EXPT_LABEL));
            }
            if (sessionLabel == null) {
                sessionLabel = StringUtils.defaultIfBlank(dicomObjectIdentifier.getSessionLabel(dataset), "dicom_upload");
            }

            String folderName = null;
            if (_parameters.containsKey(PrearcUtils.PREARC_SESSION_FOLDER)) {
                folderName = (String) _parameters.get(PrearcUtils.PREARC_SESSION_FOLDER);
                log.trace("using provided prearchive session folder {}", _parameters.get(PrearcUtils.PREARC_SESSION_FOLDER));
            }
            if (folderName == null) {
                folderName = sessionLabel;
            }

            final String visit;
            if (_parameters.containsKey(URIManager.VISIT_LABEL)) {
                visit = (String) _parameters.get(URIManager.VISIT_LABEL);
                log.trace("using provided visit label {}", _parameters.get(URIManager.VISIT_LABEL));
            } else {
                visit = null;
            }

            final String subtype;
            if (_parameters.containsKey(URIManager.SUBTYPE_LABEL)) {
                subtype = (String) _parameters.get(URIManager.SUBTYPE_LABEL);
                log.trace("using provided subtype label {}", _parameters.get(URIManager.SUBTYPE_LABEL));
            } else {
                subtype = null;
            }

            final String subject;
            if (_parameters.containsKey(URIManager.SUBJECT_ID)) {
                subject = (String) _parameters.get(URIManager.SUBJECT_ID);
            } else {
                subject = dicomObjectIdentifier.getSubjectLabel(dataset);
            }

            // Fill a SessionData object in case it is the first upload
            final File root;
            final File prearchiveRoot;

            final String timestamp;
            if (_parameters.containsKey(PrearcUtils.PREARC_TIMESTAMP)) {
                timestamp = (String) _parameters.get(PrearcUtils.PREARC_TIMESTAMP);
                log.trace("using provided timestamp {}", _parameters.get(PrearcUtils.PREARC_TIMESTAMP));
            } else {
                timestamp = PrearcUtils.makeTimestamp();
            }

            if (null == project) {
                prearchiveRoot = new File(ArcSpecManager.GetInstance().getGlobalPrearchivePath(), timestamp);
                _directArchive = false;
            } else {
                prearchiveRoot = Paths.get(ArcSpecManager.GetInstance().getGlobalPrearchivePath(), project.getId(),
                        timestamp).toFile();
            }
            root = _directArchive
                    ? new File(project.getRootArchivePath(), project.getCurrentArc())
                    : prearchiveRoot;

            if (null == subject) {
                log.trace("subject is null for session {}/{}", root, sessionLabel);
            }

            // Query the cache for an existing session that has this Study Instance UID, project name, and optional modality.
            // If found the SessionData object we just created is over-ridden with the values from the cache.
            // Additionally a record of which operation was performed is contained in the Either<SessionData,SessionData>
            // object returned.
            //
            // This record is necessary so that, if this row was created by this call, it can be deleted if anonymization
            // goes wrong. In case of any other error the file is left on the filesystem.
            final SessionData session;
            final AtomicBoolean isNew = new AtomicBoolean();
            try {
                final SessionData initialize = new SessionData();
                initialize.setFolderName(folderName);
                initialize.setName(sessionLabel);
                initialize.setProject(project == null ? null : project.getId());
                initialize.setVisit(visit);
                initialize.setProtocol(subtype);
                Date studyDate = dataset.getDate(Tag.StudyDate);
                try {
                    Date d2 = dataset.getDate(Tag.StudyTime);
                    if (d2 != null) {
                        studyDate.setHours(d2.getHours());
                        studyDate.setMinutes(d2.getMinutes());
                        studyDate.setSeconds(d2.getSeconds());
                    }
                } catch (Exception e1) {
                    // Ignore
                }
                initialize.setScan_date(studyDate);
                initialize.setTag(studyInstanceUID);
                initialize.setTimestamp(timestamp);
                initialize.setStatus(PrearcUtils.PrearcStatus.RECEIVING);
                initialize.setLastBuiltDate(Calendar.getInstance().getTime());
                initialize.setSubject(subject);
                initialize.setUrl((new File(root, folderName)).getAbsolutePath());
                initialize.setSource(_parameters.get(URIManager.SOURCE));
                initialize.setPreventAnon(Boolean.valueOf((String) _parameters.get(URIManager.PREVENT_ANON)));
                initialize.setPreventAutoCommit(Boolean.valueOf((String) _parameters.get(URIManager.PREVENT_AUTO_COMMIT)));
                if (isAutoArchive(_parameters)) {
                    initialize.setAutoArchive(PrearchiveCode.AutoArchive);
                }
                session = eitherGetOrCreateSession(initialize, prearchiveRoot, project, dataset, isNew);
            } catch (Exception e) {
                throw new ServerException(Status.SERVER_ERROR_INTERNAL, e);
            }

            Callable<Void> cleanupPrearcDb = isNew.get() ? () -> {
                deleteSessionFromDb(session); return null;} : () -> null;

            dicomObject = new DicomObjectFactory.MizerDicomObject(dataset);
            processedObjects.add(dicomObject);
            if (_doCustomProcessing &&
                    !customProcessing(NAME_OF_LOCATION_NEAR_END_AFTER_SESSION_HAS_BEEN_ADDED_TO_THE_PREARCHIVE_DATABASE,
                            dicomObject, session, cleanupPrearcDb)
            ) {
                return returnEmptyList();
            }
            dataset = dicomObject.getAttributes();

            // Build the scan label
            final String seriesNum = dataset.getString(Tag.SeriesNumber);
            final String seriesUID = dataset.getString(Tag.SeriesInstanceUID);
            final String scan = Restructurer.determineScanSubdir(StringUtils.startsWith(seriesNum, "+")? "_"+seriesNum.substring(1): seriesNum, seriesUID);

            final String source = getString(_parameters, SENDER_ID_PARAM, _user.getLogin());

            final File sessionFolder = new File(session.getUrl());
            final File outputFile = getSafeFile(sessionFolder, scan, name, dataset,
                    Boolean.parseBoolean((String) _parameters.get(RENAME_PARAM)));
            outputFile.getParentFile().mkdirs();

            final PrearcUtils.PrearcFileLock lock;
            try {
                // Because filenames can overlap across scans, we need to include the scan id. Because the prearchive
                // doesn't use the DICOM/secondary distinction, DICOM filenames must be unique within a scan
                // (see getSafeFile method within this class, particularly if rename=False).
                final String uniqueFilename = scan + outputFile.getName();
                lock = PrearcUtils.lockFile(session.getSessionDataTriple(), uniqueFilename);
            } catch (SessionFileLockException e) {
                throw new ClientException("Concurrent file sends of the same data is not supported.");
            }

            try {
                try {
                    // Split FMI from dataset before writing (dcm4che5 requires them separate for i/o)
                    final Dcm4cheConvert.SplitAttributes split = Dcm4cheConvert.splitFmiAndDataset(dataset);
                    write(split.fmi, split.onlyDataset, transferSyntaxUID, _parameters.get(SENDER_AE_TITLE_PARAM), bis, outputFile, source);
                    // Re-merge FMI back into dataset for any subsequent processing
                    //https://radiologics.atlassian.net/browse/XNAT-8719
                    dataset.addAll(split.fmi);
                } catch (IOException e) {
                    throw new ServerException(Status.SERVER_ERROR_INSUFFICIENT_STORAGE, e);
                }

                if (XDAT.getSiteConfigPreferences().getEnableNativeDicomPreCompression()) {
                    NativeDicomPreCompressor.preCompressIfNeeded(outputFile, transferSyntaxUID);
                }

                // check to see of this session came in through an application that may have performed anonymization
                // prior to transfer, e.g. the XNAT Upload Assistant.
                if (!session.getPreventAnon() && DefaultAnonUtils.getService().isSiteWideScriptEnabled()) {
                    try {
                        Configuration c = DefaultAnonUtils.getCachedSitewideAnon();
                        if (c != null && c.getStatus().equals(Configuration.ENABLED_STRING)) {
                            final MizerService service = XDAT.getContextService().getBeanSafely(MizerService.class);
                            final AnonymizationResult anonResult = service.anonymize(outputFile, session.getProject(), session.getSubject(),
                                    session.getFolderName(), true, false, c.getId(), c.getContents());
                            if (anonResult instanceof AnonymizationResultReject) {
                                FileUtils.deleteQuietly(outputFile);
                                return returnEmptyList();
                            }
                            else if (anonResult instanceof AnonymizationResultError) {
                                // Errors of this type are script evaluation problems.
                                handleAnonymizationError(isNew, session, outputFile, new ServerException(String.join("\n",anonResult.getMessages())));
                            }
                        } else {
                            log.debug("Anonymization is not enabled, allowing session {} {} {} to proceed without " +
                                    "anonymization.", session.getProject(), session.getSubject(), session.getName());
                        }
                    } catch(Throwable e){
                        // Errors of this type are deeper system errors.
                        handleAnonymizationError(isNew, session, outputFile, e);
                    }
                } else if (session.getPreventAnon()) {
                    log.debug("The session {} {} {} has already been anonymized by the uploader, proceeding without " +
                            "further anonymization.", session.getProject(), session.getSubject(), session.getName());
                }

                // Apply a one-off inline anonymization script supplied with the import request (the "Anon-Script"
                // parameter). This runs after, and in addition to, the site-wide script.
                final String inlineAnonScript = (String) TurbineUtils.unescapeParam(_parameters.get(ANON_SCRIPT_PARAM));
                if (StringUtils.isNotBlank(inlineAnonScript)) {
                    try {
                        final AnonymizationResult inlineResult = _mizer.anonymize(outputFile, session.getProject(),
                                session.getSubject(), session.getFolderName(), false, false,
                                new ByteArrayInputStream(inlineAnonScript.getBytes(StandardCharsets.UTF_8)));
                        if (inlineResult instanceof AnonymizationResultReject) {
                            FileUtils.deleteQuietly(outputFile);
                            return returnEmptyList();
                        } else if (inlineResult instanceof AnonymizationResultError) {
                            // A valid script that errors on this data (e.g. an absent tag) returns an error
                            // result: a 400, cleaned up like the site-wide path. A parse failure instead throws
                            // MizerException, which the catch below maps to a 500 (mizer can't tell it from a real fault).
                            final ClientException error = new ClientException(Status.CLIENT_ERROR_BAD_REQUEST,
                                    "The supplied inline anonymization script could not be applied: " +
                                            String.join("\n", inlineResult.getMessages()));
                            handleAnonymizationError(isNew, session, outputFile, error, error);
                        }
                    } catch (ClientException e) {
                        throw e;
                    } catch (Throwable e) {
                        handleAnonymizationError(isNew, session, outputFile, e);
                    }
                }

            } finally {
                //release the file lock
                lock.release();
            }

            log.trace("Stored object {}/{}/{} as {} for {}", project, studyInstanceUID,
                    dataset.getString(Tag.SOPInstanceUID), session.getUrl(), source);

            // There is no direct archive commit (yet) so just return the session triple
            return _directArchive ?
                    Collections.singletonList(
                            String.format("/xapi/direct-archive/%s/%s/%s",
                                    session.getProject(), session.getTag(), session.getName())) :
                    Collections.singletonList(session.getExternalUrl());
        } catch (ClientException e) {
            throw e;
        } catch (Throwable t) {
            throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, "unable to read DICOM object " + name, t);
        } finally {
            // Only safe here, and for the same reason as the spool files below: the dataset holds
            // references into the staged pixels and write() reads them.
            for (final DicomObjectI processed : processedObjects) {
                processed.releaseScratchFiles();
            }
            // Only safe here: the dataset holds references into these files, and write() reads them.
            ResumableDicomInputStream.deleteBulkDataFiles(bulkDataFiles);
        }
    }

    /**
     * Reject a Dicom instance by simply returning.
     *
     * @return empty list of Strings
     */
    private List<String> returnEmptyList() {
        return Collections.emptyList();
    }

    private void handleAnonymizationError(AtomicBoolean isNew, SessionData session,File outputFile, Throwable e) throws ServerException {
        // Site-wide and other system-level anonymization failures surface as a 500 caused by the underlying error.
        handleAnonymizationError(isNew, session, outputFile, e, new ServerException(Status.SERVER_ERROR_INTERNAL, e));
    }

    /**
     * Cleans up the artifacts of a failed ingest &mdash; the newly-created prearchive/direct-archive session if this
     * import created it, otherwise just the written file &mdash; then throws {@code toThrow}. Shared by the
     * site-wide/system error path (which throws a 500 {@link ServerException}) and the inline Anon-Script error path
     * (which throws a 400 {@link ClientException}). If the cleanup itself fails, a {@link ServerException} for that
     * failure is thrown instead of {@code toThrow}.
     *
     * @param isNew      Whether this import created the session row, in which case the row is deleted
     * @param session    The session being ingested
     * @param outputFile The file written by this import
     * @param cause      The underlying failure, used only for logging (may be the same object as {@code toThrow})
     * @param toThrow    The exception to throw once cleanup completes
     */
    private <E extends ActionException> void handleAnonymizationError(AtomicBoolean isNew, SessionData session, File outputFile, Throwable cause, E toThrow) throws E, ServerException {
        log.debug("Dicom anonymization failed: {}", outputFile, cause);
        try {
            // if we created a row in the database table for this session
            // delete it.
            if (isNew.get()) {
                deleteSessionFromDb(session);
            } else {
                outputFile.delete();
            }
        } catch (Throwable t) {
            log.debug("Unable to delete relevant file: " + outputFile, cause);
            throw new ServerException(Status.SERVER_ERROR_INTERNAL, t);
        }
        throw toThrow;
    }

    private void deleteSessionFromDb(SessionData session) throws Exception {
        if (_directArchive) {
            _directArchiveSessionService.delete(session);
        } else {
            PrearcDatabase.deleteSession(session.getFolderName(), session.getTimestamp(), session.getProject());
        }
    }

    private void updateSessionLastMod(SessionData session) {
        // If the last mod time is more then 15 seconds ago, update it.
        if (Calendar.getInstance().getTime().after(DateUtils.addSeconds(session.getLastBuiltDate(), 15))) {
            try {
                if (_directArchive) {
                    _directArchiveSessionService.touch(session);
                } else {
                    // this code builds and executes the sql directly, because the APIs for doing so generate multiple SELECT statements (to confirm the row is there)
                    // we've confirmed the row is there in eitherGetOrCreateSession, so that shouldn't be necessary here.
                    // this code executes for every file received, so any unnecessary sql should be eliminated.
                    PoolDBUtils.ExecuteNonSelectQuery(DatabaseSession.updateSessionLastModSQL(session.getName(), session.getTimestamp(), session.getProject()), null, null);
                }
            } catch (Exception e) {
                log.error("An error occurred trying to update the session update timestamp.", e);
            }
        }
    }

    private SessionData eitherGetOrCreateSession(SessionData initialize, File prearchiveRoot,
                                                 XnatProjectdata project, Attributes dicom, AtomicBoolean isNew)
            throws Exception {
        SessionData session = null;
        if (_directArchive) {
            try {
                session = _directArchiveSessionService.getOrCreate(initialize, isNew, _directArchiveOverwrite);
            } catch (Exception e) {
                log.warn("Unable to directly archive session, will proceed through prearchive", e);
            }
            if (session == null) {
                _directArchive = false;
                initialize.setUrl(new File(prearchiveRoot, initialize.getFolderName()).getAbsolutePath());
            }
        }

        if (session == null) {
            Either<SessionData, SessionData> getOrCreate = PrearcDatabase.eitherGetOrCreateSession(initialize,
                    prearchiveRoot, shouldAutoArchive(project, dicom));
            isNew.set(getOrCreate.isLeft());

            if (isNew.get()) {
                session = getOrCreate.getLeft();
            } else {
                session = getOrCreate.getRight();
            }
        }

        if (!isNew.get()) {
            updateSessionLastMod(session);
        }
        return session;
    }

    /**
     *
     * @param location
     * @param dicom
     * @param session
     * @return true if processing success, false if the instance is rejected.
     * @throws Exception
     */
    private boolean customProcessing(String location, DicomObjectI dicom, SessionData session)
            throws Exception {
        return customProcessing(location, dicom, session, () -> null);
    }

    /**
     *
     * @param location
     * @param dicom
     * @param session
     * @param onException
     * @return true if processing success, false if the instance is rejected.
     * @throws Exception
     */
    private boolean customProcessing(String location, DicomObjectI dicom, SessionData session, Callable<Void> onException)
            throws Exception {
        try {
            return iterateOverProcessorsAtLocation(location, dicom, session);
        } catch (Throwable e) {
            //If a processor throws an exception, processing should not proceed and that exception will be passed to the calling class.
            //We may be okay just passing an empty list in this case, but since I wasn't sure, I didn't want to change how it works now where if there's a problem importing part of a zip, the whole import fails.
            onException.call();
            throw new ServerException(Status.SERVER_ERROR_INTERNAL, e);
        }
    }

    /**
     * See XNAT-5441 and commit 73538bf for source of this code
     * @param location
     * @param dicom
     * @param session
     * @return true if processing success, false if the Dicom instance is rejected.
     * @throws Exception
     */
    private boolean iterateOverProcessorsAtLocation(String location, final DicomObjectI dicom, final SessionData session)
            throws Exception {
        boolean continueProcessingData = true;
        Map<Class<? extends ArchiveProcessor>, ArchiveProcessor> processorsMap = getProcessorsMap();
        //Later this map will be used when iterating over the processorInstances to get the processor for the given instance
        List<ArchiveProcessorInstance> processorInstances = _processorInstanceService.getAllEnabledSiteProcessorsInOrderForLocation(location);
        if (processorInstances != null) {
            for (ArchiveProcessorInstance processorInstance : processorInstances) {
                try {
                    Class<? extends ArchiveProcessor> processorClass =
                            (Class<? extends ArchiveProcessor>) Class.forName(processorInstance.getProcessorClass());
                    ArchiveProcessor processor = processorsMap.get(processorClass);
                    if (processor.accept(dicom, session, _mizer, processorInstance, _parameters)) {
                        // processor.process return false if the instance is rejected.
                        if (!processor.process(dicom, session, _mizer, processorInstance, _parameters)) {
                            continueProcessingData = false;
                            break;
                        }
                    }
                } catch (ClassNotFoundException e) {
                    final String label = processorInstance.getLabel();;
                    if (!missingArchiveProcessors.contains(label)) {
                        // only notify once(ish) for each processor where the class is missing
                        // this is an obscure failure that probably only happens in dev
                        missingArchiveProcessors.add(label);
                        log.error("unable to apply archive processor " + label, e);
                    }
                }
            }
        }
        return continueProcessingData;
    }

    private Map<Class<? extends ArchiveProcessor>, ArchiveProcessor> getProcessorsMap() {
        if (_processorsMap == null) {
            synchronized (this) {
                if (_processorsMap == null) {
                    _processorsMap = new HashMap<>();
                    Map<String, ArchiveProcessor> processorMap =
                            XDAT.getContextService().getBeansOfType(ArchiveProcessor.class);
                    if (processorMap != null) {
                        for (ArchiveProcessor processor : processorMap.values()) {
                            if (processor != null) {
                                _processorsMap.put(processor.getClass(), processor);
                            }
                        }
                    }
                }
            }
        }
        return _processorsMap;
    }

    private XnatProjectdata getProject(final String alias, final Callable<XnatProjectdata> lookupProject) {
        if (null != alias) {
            log.debug("looking for project matching alias {} from query parameters", alias);
            final XnatProjectdata project = _cache.get(_user, alias);
            if (project != null) {
                log.info("Storage request specified project or alias {}, found accessible project {}", alias, project.getId());
                return project;
            }
            log.info("storage request specified project {}, which does not exist or user does not have create perms", alias);
        } else {
            log.trace("no project alias found in query parameters");
        }

        // No alias, or we couldn't match it to a project. Run the identifier to see if that can get a project name/alias.
        // (Don't cache alias->identifier-derived-project because we didn't use the alias to derive the project.)
        try {
            return null == lookupProject ? null : lookupProject.call();
        } catch (Throwable t) {
            log.error("error in project lookup", t);
            return null;
        }
    }

    private File getSafeFile(File sessionDir, String scan, String name, Attributes o, boolean forceRename) {
        String fileName = getNamer().makeFileName(o);
        while (fileName.charAt(0) == '.') {
            fileName = fileName.substring(1);
        }
        final File safeFile = Files.getImageFile(sessionDir, scan, fileName);
        if (forceRename) {
            return safeFile;
        }
        final String valname = Files.toFileNameChars(name);
        if (!Files.isValidFilename(valname)) {
            return safeFile;
        }
        final File reqFile = Files.getImageFile(sessionDir, scan, valname);
        if (reqFile.exists()) {
            try (final DicomInputStream fin = new DicomInputStream(reqFile)) {
                final Attributes o1 = fin.readDataset(Tag.SOPInstanceUID + 1);
                if (Objects.equal(o.getString(Tag.SOPInstanceUID), o1.getString(Tag.SOPInstanceUID)) &&
                        Objects.equal(o.getString(Tag.SOPClassUID), o1.getString(Tag.SOPClassUID))) {
                    return reqFile;  // object are equivalent; ok to overwrite
                } else {
                    return safeFile;
                }
            } catch (Throwable t) {
                return safeFile;
            }
        } else {
            return reqFile;
        }
    }

    private PrearchiveCode shouldAutoArchive(final XnatProjectdata project, final Attributes o) {
        if (null == project) {
            return null;
        }
        Boolean fromDicomObject = getIdentifier().requestsAutoarchive(o);
        if (fromDicomObject != null) {
            return fromDicomObject ? PrearchiveCode.AutoArchive : PrearchiveCode.Manual;
        }
        ArcProject arcProject = project.getArcSpecification();
        if (arcProject == null) {
            log.warn("Tried to get the arc project from project {}, but got null in return. Returning null for the " +
                    "prearchive code, but it's probably not good that the arc project wasn't found.", project.getId());
            return null;
        }
        return PrearchiveCode.code(arcProject.getPrearchiveCode());
    }

    private static boolean initializeCanDecompress() {
        try {
            return Decompress.isSupported();
        } catch (NoClassDefFoundError error) {
            return false;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static <K, V> String getString(final Map<K, V> m, final K k, final V defaultValue) {
        final V v = m.get(k);
        if (null == v) {
            return null == defaultValue ? null : defaultValue.toString();
        } else {
            return v.toString();
        }
    }

    private static DicomObjectI read(final InputStream in, final String name) throws ClientException {
        try (final BufferedInputStream bis = new BufferedInputStream(in)) {
            // dcm4che3 - Use DicomObjectFactory to create DicomObjectI from InputStream
            final DicomObjectI o = DicomObjectFactory.newInstance(bis);
            if (Strings.isNullOrEmpty(o.getString(Tag.SOPClassUID))) {
                throw new ClientException("object " + name + " contains no SOP Class UID");
            }
            if (Strings.isNullOrEmpty(o.getString(Tag.SOPInstanceUID))) {
                throw new ClientException("object " + name + " contains no SOP Instance UID");
            }
            return o;
        } catch (Exception e) {
            throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, "unable to parse or close DICOM object", e);
        }
    }

    private static void write(final Attributes originalFmi, final Attributes dataset,
                              final String transferSyntaxUid, final Object sourceAeTitle,
                              final InputStream remainder,
                              final File outputFile, final String source)
            throws ClientException, IOException {
        // Preserve the FMI from the received file, if present.
        final Attributes fmi = null == originalFmi ? dataset.createFileMetaInformation(transferSyntaxUid) : originalFmi;
        if (null != sourceAeTitle) {
            fmi.setString(Tag.SourceApplicationEntityTitle, VR.AE, (String) sourceAeTitle);
        }
        try (final FileOutputStream fos = new FileOutputStream(outputFile);
             final BufferedOutputStream bos = new BufferedOutputStream(fos);
             final DicomOutputStream dos = new DicomOutputStream(bos, UID.ExplicitVRLittleEndian)) {
                // open stream with Explicit VR Little Endian because that's the required TS for FMI.
                // stream object will switch to our provided TS after writing FMI.
                dos.writeDataset(fmi, dataset);
                dos.flush();

                // If there's remaining data (like pixel data), append it
                if (null != remainder) {
                    final long copied = ByteStreams.copy(remainder, bos);
                    log.trace("copied {} additional bytes to {}", copied, outputFile);
                }

                LoggerFactory.getLogger("org.nrg.xnat.received").info("{}:{}", source, outputFile);
        }
    }

    private static final String  RENAME_PARAM            = "rename";
    private static final boolean canDecompress           = initializeCanDecompress();

    private final UserProjectCache    _cache;
    private final FileWriterWrapperI  _fileWriter;
    private final UserI               _user;
    private final Map<String, Object> _parameters;
    private final boolean             _doCustomProcessing;
    private boolean                   _directArchive;
    private final String              _directArchiveOverwrite;

    private String     _transferSyntax;

    private final MizerService _mizer;
    private final ArchiveProcessorInstanceService _processorInstanceService;
    private Map<Class<? extends ArchiveProcessor>, ArchiveProcessor> _processorsMap;
    private final DirectArchiveSessionService _directArchiveSessionService;

    private static final Set<String> missingArchiveProcessors = new HashSet<>();

    public static final String SENDER_AE_TITLE_PARAM = "Sender-AE-Title";
    public static final String RECEIVER_AE_TITLE_PARAM = "Receiver-AE-Title";
    public static final String RECEIVER_PORT_PARAM = "Receiver-Port";
    public static final String SENDER_ID_PARAM = "Sender-ID";
    public static final String TSUID_PARAM = "Transfer-Syntax-UID";
    public static final String CUSTOM_PROC_PARAM = "Custom-Processing";
    public static final String DIRECT_ARCHIVE_PARAM = "Direct-Archive";
    public static final String DIRECT_ARCHIVE_OVERWRITE_PARAM = "Direct-Archive-Overwrite";
    public static final String ANON_SCRIPT_PARAM = "Anon-Script";

    public final static String NAME_OF_LOCATION_AT_BEGINNING_AFTER_DICOM_OBJECT_IS_READ = "AfterDicomRead";
    public final static String NAME_OF_LOCATION_AFTER_PROJECT_HAS_BEEN_ASSIGNED = "AfterProjectSet";
    public final static String NAME_OF_LOCATION_NEAR_END_AFTER_SESSION_HAS_BEEN_ADDED_TO_THE_PREARCHIVE_DATABASE = "AfterAddedToPrearchiveDatabase";
}
