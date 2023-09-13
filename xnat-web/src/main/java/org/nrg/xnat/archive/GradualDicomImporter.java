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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4che2.util.TagUtils;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.config.entities.Configuration;
import org.nrg.dcm.Decompress;
import org.nrg.dcm.Restructurer;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.dicomtools.filters.DicomFilterService;
import org.nrg.dicomtools.filters.SeriesImportFilter;
import org.nrg.framework.constants.PrearchiveCode;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.ArcProject;
import org.nrg.xdat.om.XnatProjectdata;
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
import org.nrg.xnat.services.cache.UserProjectCache;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.restlet.data.Status;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            _transferSyntax = TransferSyntax.valueOf((String) _parameters.get(TSUID_PARAM));
        }
        //noinspection unchecked
        _cache = XDAT.getContextService().getBean(UserProjectCache.class);
        _doCustomProcessing = PrearcUtils.parseParam(_parameters, CUSTOM_PROC_PARAM, false);
        _directArchive = PrearcUtils.parseParam(_parameters, DIRECT_ARCHIVE_PARAM, false);

        // spring beans
        _mizer = XDAT.getContextService().getBeanSafely(MizerService.class);
        _processorInstanceService = XDAT.getContextService().getBeanSafely(ArchiveProcessorInstanceService.class);
        _directArchiveSessionService = XDAT.getContextService().getBeanSafely(DirectArchiveSessionService.class);
        _directArchive &= _directArchiveSessionService != null;
    }

    private int getMaxFilterTag(final SeriesImportFilter filter) {
        if (filter == null || !filter.isEnabled()) {
            return 0;
        }
       List<Integer> tags=filter.getFilterTags();
       if (tags.isEmpty()) {
           return 0;
       }
       return tags.get(tags.size()-1);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public List<String> call() throws ClientException {
        final String name = _fileWriter.getName();
        final DicomObject dicom;
        final XnatProjectdata project;
        final DicomObjectIdentifier<XnatProjectdata> dicomObjectIdentifier = getIdentifier();
        final SeriesImportFilter siteFilter = getDicomFilterService().getSeriesImportFilter();
        final int lastTag = Math.max(getMaxFilterTag(siteFilter), Math.max(dicomObjectIdentifier.getTags().last(), Tag.SeriesDescription))+ 1;
        try (final BufferedInputStream bis = new BufferedInputStream(_fileWriter.getInputStream());
             final DicomInputStream dis = null == _transferSyntax ? new DicomInputStream(bis) : new DicomInputStream(bis, _transferSyntax)) {
            log.trace("reading object into memory up to {}", TagUtils.toString(lastTag));
            dis.setHandler(new StopTagInputHandler(lastTag));
            dicom = dis.readDicomObject();

            if (_doCustomProcessing & !customProcessing(NAME_OF_LOCATION_AT_BEGINNING_AFTER_DICOM_OBJECT_IS_READ, dicom, null)) {
                return new ArrayList<>();
            }

            log.trace("handling file with query parameters {}", _parameters);
            try {
                // project identifier is expensive, so avoid if possible
                project = getProject(PrearcUtils.identifyProject(_parameters),
                        () -> dicomObjectIdentifier.getProject(dicom));
            } catch (MalformedURLException e1) {
                log.error("unable to parse supplied destination flag", e1);
                throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, e1);
            }

            final SessionData tempSession = new SessionData();
            tempSession.setProject(project == null ? null : project.getId());
            tempSession.setSubject("");
            tempSession.setFolderName("");
            if (_doCustomProcessing & !customProcessing(NAME_OF_LOCATION_AFTER_PROJECT_HAS_BEEN_ASSIGNED, dicom, tempSession)) {
                return new ArrayList<>();
            }

            final String projectId = project != null ? project.getId() : null;
            final SeriesImportFilter projectFilter = StringUtils.isNotBlank(projectId) ? getDicomFilterService().getSeriesImportFilter(projectId) : null;
            final int maxProjectTag = getMaxFilterTag(projectFilter);
            final DicomObject filterDicomObject;
            if (maxProjectTag > lastTag) {
                bis.reset();
                dis.setHandler(new StopTagInputHandler(maxProjectTag));
                filterDicomObject = dis.readDicomObject();
            } else {
                filterDicomObject = dicom;
            }
            if (log.isDebugEnabled()) {
                if (siteFilter != null) {
                    if (projectFilter != null) {
                        log.debug("Found " + (siteFilter.isEnabled() ? "enabled" : "disabled") + " site-wide series import filter and " + (siteFilter.isEnabled() ? "enabled" : "disabled") + " series import filter for the project " + projectId);
                    } else if (StringUtils.isNotBlank(projectId)) {
                        log.debug("Found " + (siteFilter.isEnabled() ? "enabled" : "disabled") + " site-wide series import filter and no series import filter for the project " + projectId);
                    } else {
                        log.debug("Found a site-wide series import filter and no project ID was specified");
                    }
                } else if (projectFilter != null) {
                    log.debug("Found no site-wide series import filter and " + (projectFilter.isEnabled() ? "enabled" : "disabled") + " series import filter for the project " + projectId);
                }
            }
            if (!(shouldIncludeDicomObject(siteFilter, filterDicomObject) && shouldIncludeDicomObject(projectFilter, filterDicomObject))) {
                return new ArrayList<>();
                /* TODO: Return information to user on rejected files. Unfortunately throwing an
                 * exception causes DicomBrowser to display a panicked error message. Some way of
                 * returning the information that a particular file type was not accepted would be
                 * nice, though. Possibly record the information and display on an admin page.
                 * Work to be done for 1.7
                 */
            }
            try {
                bis.reset();
            } catch (IOException e) {
                log.error("unable to reset DICOM data stream", e);
            }
            if (Strings.isNullOrEmpty(dicom.getString(Tag.SOPClassUID))) {
                throw new ClientException("object " + name + " contains no SOP Class UID");
            }
            if (Strings.isNullOrEmpty(dicom.getString(Tag.SOPInstanceUID))) {
                throw new ClientException("object " + name + " contains no SOP Instance UID");
            }

            final String studyInstanceUID = dicom.getString(Tag.StudyInstanceUID);
            log.trace("Looking for study {} in project {}", studyInstanceUID, null == project ? null : project.getId());

            final String sessionLabel;
            if (_parameters.containsKey(URIManager.EXPT_LABEL)) {
                sessionLabel = (String) _parameters.get(URIManager.EXPT_LABEL);
                log.trace("using provided experiment label {}", _parameters.get(URIManager.EXPT_LABEL));
            } else {
                sessionLabel = StringUtils.defaultIfBlank(dicomObjectIdentifier.getSessionLabel(dicom), "dicom_upload");
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
                subject = dicomObjectIdentifier.getSubjectLabel(dicom);
            }

            // Fill a SessionData object in case it is the first upload
            final File root;
            final File prearchiveRoot;
            final String timestamp = PrearcUtils.makeTimestamp();
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
                initialize.setFolderName(sessionLabel);
                initialize.setName(sessionLabel);
                initialize.setProject(project == null ? null : project.getId());
                initialize.setVisit(visit);
                initialize.setProtocol(subtype);
                Date studyDate = dicom.getDate(Tag.StudyDate);
                try {
                    Date d2 = dicom.getDate(Tag.StudyTime);
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
                initialize.setUrl((new File(root, sessionLabel)).getAbsolutePath());
                initialize.setSource(_parameters.get(URIManager.SOURCE));
                initialize.setPreventAnon(Boolean.valueOf((String) _parameters.get(URIManager.PREVENT_ANON)));
                initialize.setPreventAutoCommit(Boolean.valueOf((String) _parameters.get(URIManager.PREVENT_AUTO_COMMIT)));

                session = eitherGetOrCreateSession(initialize, prearchiveRoot, project, dicom, isNew);
            } catch (Exception e) {
                throw new ServerException(Status.SERVER_ERROR_INTERNAL, e);
            }

            Callable<Void> cleanupPrearcDb = isNew.get() ? () -> {
                deleteSessionFromDb(session); return null;} : () -> null;

            if (_doCustomProcessing &&
                    !customProcessing(NAME_OF_LOCATION_NEAR_END_AFTER_SESSION_HAS_BEEN_ADDED_TO_THE_PREARCHIVE_DATABASE,
                            dicom, session, cleanupPrearcDb)
            ) {
                return new ArrayList<>();
            }

            // Build the scan label
            final String seriesNum = dicom.getString(Tag.SeriesNumber);
            final String seriesUID = dicom.getString(Tag.SeriesInstanceUID);
            final String scan = Restructurer.determineScanSubdir(seriesNum, seriesUID);

            final String source = getString(_parameters, SENDER_ID_PARAM, _user.getLogin());

            final DicomObject fmi;
            if (dicom.contains(Tag.TransferSyntaxUID)) {
                fmi = dicom.fileMetaInfo();
            } else {
                final String sopClassUID = dicom.getString(Tag.SOPClassUID);
                final String sopInstanceUID = dicom.getString(Tag.SOPInstanceUID);
                final String transferSyntaxUID;
                if (null == _transferSyntax) {
                    transferSyntaxUID = dicom.getString(Tag.TransferSyntaxUID, DEFAULT_TRANSFER_SYNTAX);
                } else {
                    transferSyntaxUID = _transferSyntax.uid();
                }
                fmi = new BasicDicomObject();
                fmi.initFileMetaInformation(sopClassUID, sopInstanceUID, transferSyntaxUID);
                if (_parameters.containsKey(SENDER_AE_TITLE_PARAM)) {
                    fmi.putString(Tag.SourceApplicationEntityTitle, VR.AE, (String) _parameters.get(SENDER_AE_TITLE_PARAM));
                }
            }

            final File sessionFolder = new File(session.getUrl());
            final File outputFile = getSafeFile(sessionFolder, scan, name, dicom,
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
                    write(fmi, dicom, bis, outputFile, source);
                } catch (IOException e) {
                    throw new ServerException(Status.SERVER_ERROR_INSUFFICIENT_STORAGE, e);
                }

                // check to see of this session came in through an application that may have performed anonymization
                // prior to transfer, e.g. the XNAT Upload Assistant.
                if (!_doCustomProcessing && !session.getPreventAnon() &&
                        DefaultAnonUtils.getService().isSiteWideScriptEnabled()) {
                    try {
                        Configuration c = DefaultAnonUtils.getCachedSitewideAnon();
                        if (c != null && c.getStatus().equals(Configuration.ENABLED_STRING)) {
                            final MizerService service = XDAT.getContextService().getBeanSafely(MizerService.class);
                            service.anonymize(outputFile, session.getProject(), session.getSubject(),
                                    session.getFolderName(), true, c.getId(), c.getContents());

                        } else {
                            log.debug("Anonymization is not enabled, allowing session {} {} {} to proceed without " +
                                    "anonymization.", session.getProject(), session.getSubject(), session.getName());
                        }
                    } catch(Throwable e){
                        log.debug("Dicom anonymization failed: " + outputFile, e);
                        try {
                            // if we created a row in the database table for this session
                            // delete it.
                            if (isNew.get()) {
                                deleteSessionFromDb(session);
                            } else {
                                outputFile.delete();
                            }
                        } catch (Throwable t) {
                            log.debug("Unable to delete relevant file: " + outputFile, e);
                            throw new ServerException(Status.SERVER_ERROR_INTERNAL, t);
                        }
                        throw new ServerException(Status.SERVER_ERROR_INTERNAL, e);
                    }
                } else if (session.getPreventAnon()) {
                    log.debug("The session {} {} {} has already been anonymized by the uploader, proceeding without " +
                            "further anonymization.", session.getProject(), session.getSubject(), session.getName());
                }
            } finally {
                //release the file lock
                lock.release();
            }

            log.trace("Stored object {}/{}/{} as {} for {}", project, studyInstanceUID,
                    dicom.getString(Tag.SOPInstanceUID), session.getUrl(), source);

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
        }

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
                                                 XnatProjectdata project, DicomObject dicom, AtomicBoolean isNew)
            throws Exception {
        SessionData session = null;
        if (_directArchive) {
            try {
                session = _directArchiveSessionService.getOrCreate(initialize, isNew);
            } catch (Exception e) {
                log.warn("Unable to directly archive session, will proceed through prearchive", e);
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

    private boolean customProcessing(String location, DicomObject dicom, SessionData session)
            throws Exception {
        return customProcessing(location, dicom, session, () -> null);
    }

    private boolean customProcessing(String location, DicomObject dicom, SessionData session, Callable<Void> onException)
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

    // See XNAT-5441 and commit 73538bf for source of this code
    private boolean iterateOverProcessorsAtLocation(String location, final DicomObject dicom, final SessionData session)
            throws Exception {
        boolean continueProcessingData = true;
        Map<Class<? extends ArchiveProcessor>, ArchiveProcessor> processorsMap = getProcessorsMap();
        //Later this map will be used when iterating over the processorInstances to get the processor for the given instance
        List<ArchiveProcessorInstance> processorInstances = _processorInstanceService.getAllEnabledSiteProcessorsInOrderForLocation(location);
        if (processorInstances != null) {
            for (ArchiveProcessorInstance processorInstance : processorInstances) {
                Class<? extends ArchiveProcessor> processorClass =
                        (Class<? extends ArchiveProcessor>) Class.forName(processorInstance.getProcessorClass());
                ArchiveProcessor processor = processorsMap.get(processorClass);
                if (processor.accept(dicom, session, _mizer, processorInstance, _parameters)) {
                    if (!processor.process(dicom, session, _mizer, processorInstance, _parameters)) {
                        continueProcessingData = false;
                        break;
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

    private File getSafeFile(File sessionDir, String scan, String name, DicomObject o, boolean forceRename) {
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
            try (final FileInputStream fin = new FileInputStream(reqFile)) {
                final DicomObject o1 = read(fin, name);
                if (Objects.equal(o.get(Tag.SOPInstanceUID), o1.get(Tag.SOPInstanceUID)) &&
                        Objects.equal(o.get(Tag.SOPClassUID), o1.get(Tag.SOPClassUID))) {
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

    private boolean shouldIncludeDicomObject(final SeriesImportFilter filter, final DicomObject dicom) {
        // If we don't have a filter or the filter is turned off, then we include the DICOM object by default (no filtering)
        if (filter == null || !filter.isEnabled()) {
            return true;
        }
        final boolean shouldInclude = filter.shouldIncludeDicomObject(dicom);
        if (log.isDebugEnabled()) {
            final String association = StringUtils.isBlank(filter.getProjectId()) ? "site" : "project " + filter.getProjectId();
            log.debug("The series import filter for " + association + " indicated a DICOM object from series \"" +
                    dicom.get(Tag.SeriesDescription).getString(dicom.getSpecificCharacterSet(), true) + "\" " +
                    (shouldInclude ? "should" : "shouldn't") + " be included.");
        }
        return shouldInclude;
    }

    private DicomFilterService getDicomFilterService() {
        if (_filterService == null) {
            synchronized (this) {
                if (_filterService == null) {
                    _filterService = XDAT.getContextService().getBean(DicomFilterService.class);
                }
            }
        }
        return _filterService;
    }

    private PrearchiveCode shouldAutoArchive(final XnatProjectdata project, final DicomObject o) {
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

    private static DicomObject read(final InputStream in, final String name) throws ClientException {
        try (final BufferedInputStream bis = new BufferedInputStream(in);
             final DicomInputStream dis = new DicomInputStream(bis)) {
            final DicomObject o = dis.readDicomObject();
            if (Strings.isNullOrEmpty(o.getString(Tag.SOPClassUID))) {
                throw new ClientException("object " + name + " contains no SOP Class UID");
            }
            if (Strings.isNullOrEmpty(o.getString(Tag.SOPInstanceUID))) {
                throw new ClientException("object " + name + " contains no SOP Instance UID");
            }
            return o;
        } catch (IOException e) {
            throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, "unable to parse or close DICOM object", e);
        }
    }

    private static void write(final DicomObject fmi, final DicomObject dataset, final BufferedInputStream remainder,
                              final File f, final String source)
            throws ClientException, IOException {
        IOException ioexception = null;
        final FileOutputStream fos = new FileOutputStream(f);
        final BufferedOutputStream bos = new BufferedOutputStream(fos);
        try {
            final DicomOutputStream dos = new DicomOutputStream(bos);
            try {
                final String tsuid = fmi.getString(Tag.TransferSyntaxUID, DEFAULT_TRANSFER_SYNTAX);
                try {
                    if (Decompress.needsDecompress(tsuid) && canDecompress) {
                        try {
                            // Read the rest of the object into memory so the pixel data can be decompressed.
                            final DicomInputStream dis = new DicomInputStream(remainder, tsuid);
                            try {
                                dis.readDicomObject(dataset, -1);
                            } catch (IOException e) {
                                ioexception = e;
                                throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST,
                                        "error parsing DICOM object", e);
                            }
                            final ByteArrayInputStream bis = new ByteArrayInputStream(Decompress.dicomObject2Bytes(dataset, tsuid));
                            final DicomObject d = Decompress.decompress_image(bis, tsuid);
                            final String dtsdui = Decompress.getTsuid(d);
                            try {
                                fmi.putString(Tag.TransferSyntaxUID, VR.UI, dtsdui);
                                dos.writeFileMetaInformation(fmi);
                                dos.writeDataset(d.dataset(), dtsdui);
                            } catch (Throwable t) {
                                if (t instanceof IOException) {
                                    ioexception = (IOException) t;
                                } else {
                                    log.error("Unable to write decompressed dataset", t);
                                }
                                try {
                                    dos.close();
                                } catch (IOException e) {
                                    throw ioexception = null == ioexception ? e : ioexception;
                                }
                            }
                        } catch (ClientException e) {
                            throw e;
                        } catch (Throwable t) {
                            log.error("Decompression failed; storing in original format " + tsuid, t);
                            dos.writeFileMetaInformation(fmi);
                            dos.writeDataset(dataset, tsuid);
                            if (null != remainder) {
                                final long copied = ByteStreams.copy(remainder, bos);
                                log.trace("copied {} additional bytes to {}", copied, f);
                            }
                        }
                    } else {
                        dos.writeFileMetaInformation(fmi);
                        dos.writeDataset(dataset, tsuid);
                        if (null != remainder) {
                            final long copied = ByteStreams.copy(remainder, bos);
                            log.trace("copied {} additional bytes to {}", copied, f);
                        }
                    }
                } catch (NoClassDefFoundError t) {
                    log.error("Unable to check compression status; storing in original format " + tsuid, t);
                    dos.writeFileMetaInformation(fmi);
                    dos.writeDataset(dataset, tsuid);
                    if (null != remainder) {
                        final long copied = ByteStreams.copy(remainder, bos);
                        log.trace("copied {} additional bytes to {}", copied, f);
                    }
                }
            } catch (IOException e) {
                throw ioexception = null == ioexception ? e : ioexception;
            } finally {
                try {
                    dos.close();
                    LoggerFactory.getLogger("org.nrg.xnat.received").info("{}:{}", source, f);
                } catch (IOException e) {
                    throw null == ioexception ? e : ioexception;
                }
            }
        } catch (IOException e) {
            throw ioexception = e;
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                throw null == ioexception ? e : ioexception;
            }
        }
    }

    private static final String  DEFAULT_TRANSFER_SYNTAX = TransferSyntax.ExplicitVRLittleEndian.uid();
    private static final String  RENAME_PARAM            = "rename";
    private static final boolean canDecompress           = initializeCanDecompress();

    private final UserProjectCache    _cache;
    private final FileWriterWrapperI  _fileWriter;
    private final UserI               _user;
    private final Map<String, Object> _parameters;
    private final boolean             _doCustomProcessing;
    private boolean                   _directArchive;

    private TransferSyntax     _transferSyntax;
    private DicomFilterService _filterService;

    private final MizerService _mizer;
    private final ArchiveProcessorInstanceService _processorInstanceService;
    private Map<Class<? extends ArchiveProcessor>, ArchiveProcessor> _processorsMap;
    private final DirectArchiveSessionService _directArchiveSessionService;

    public static final String SENDER_AE_TITLE_PARAM = "Sender-AE-Title";
    public static final String RECEIVER_AE_TITLE_PARAM = "Receiver-AE-Title";
    public static final String RECEIVER_PORT_PARAM = "Receiver-Port";
    public static final String SENDER_ID_PARAM = "Sender-ID";
    public static final String TSUID_PARAM = "Transfer-Syntax-UID";
    public static final String CUSTOM_PROC_PARAM = "Custom-Processing";
    public static final String DIRECT_ARCHIVE_PARAM = "Direct-Archive";

    public final static String NAME_OF_LOCATION_AT_BEGINNING_AFTER_DICOM_OBJECT_IS_READ = "AfterDicomRead";
    public final static String NAME_OF_LOCATION_AFTER_PROJECT_HAS_BEEN_ASSIGNED = "AfterProjectSet";
    public final static String NAME_OF_LOCATION_NEAR_END_AFTER_SESSION_HAS_BEEN_ADDED_TO_THE_PREARCHIVE_DATABASE = "AfterAddedToPrearchiveDatabase";
}
