package org.nrg.xnat.archive.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultError;
import org.nrg.dicom.mizer.objects.AnonymizationResultNoOp;
import org.nrg.framework.ajax.Filter;
import org.nrg.framework.ajax.hibernate.HibernateFilter;
import org.nrg.framework.constants.PrearchiveCode;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.archive.ArchivingException;
import org.nrg.xnat.archive.Operation;
import org.nrg.xnat.archive.PrearcSessionArchiver;
import org.nrg.xnat.archive.entities.DirectArchiveSession;
import org.nrg.xnat.archive.services.DirectArchiveSessionHibernateService;
import org.nrg.xnat.archive.services.DirectArchiveSessionService;
import org.nrg.xnat.archive.xapi.DirectArchiveSessionPaginatedRequest;
import org.nrg.xnat.helpers.merge.MergeUtils;
import org.nrg.xnat.helpers.merge.ProjectAnonymizer;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcTableBuilder;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.services.messaging.archive.DirectArchiveRequest;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;
import org.nrg.xnat.turbine.utils.XNATSessionPopulater;
import org.nrg.xnat.utils.CatalogUtils;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xdat.model.CatDcmentryI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xnat.utils.WorkflowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.nrg.xft.event.XftItemEventI.CREATE;
import static org.nrg.xft.event.XftItemEventI.UPDATE;
import static org.nrg.xnat.archive.Operation.Rebuild;

@Slf4j
@Service
public class DirectArchiveSessionServiceImpl implements DirectArchiveSessionService {
    private static final Map<String, Object> EMPTY_MAP = Collections.emptyMap();

    private final Object subjectLock = new Object();

    private final Object createLock = new Object();

    /**
     * Tracks experiments with pending merge workflow completions.
     * Key = experiment ID, Value = info needed to check for active DirectArchiveSessions.
     * When no more files arrive for the timeout period, the merge workflow is completed.
     */
    private final ConcurrentMap<String, PendingMergeInfo> pendingMergeCompletions = new ConcurrentHashMap<>();

    private static class PendingMergeInfo {
        final String project;
        final String tag;
        final String name;
        long lastMergeTime;

        PendingMergeInfo(String project, String tag, String name) {
            this.project = project;
            this.tag = tag;
            this.name = name;
            this.lastMergeTime = System.currentTimeMillis();
        }
    }

    @Autowired
    public DirectArchiveSessionServiceImpl(final DirectArchiveSessionHibernateService directArchiveSessionHibernateService,
                                           final JmsTemplate jmsTemplate,
                                           final XnatUserProvider receivedFileUserProvider,
                                           final GroupsAndPermissionsCache groupsAndPermissionsCache,
                                           final PermissionsServiceI permissionsService) {
        this.directArchiveSessionHibernateService = directArchiveSessionHibernateService;
        this.jmsTemplate                          = jmsTemplate;
        this.receivedFileUserProvider             = receivedFileUserProvider;
        this.groupsAndPermissionsCache            = groupsAndPermissionsCache;
        this.permissionsService                   = permissionsService;
    }

    @Override
    public void delete(SessionData session) {
        Long id = session.getId();
        if(id != null) {
            directArchiveSessionHibernateService.delete(id);
        }
    }

    @Override
    public void delete(long id, UserI user) throws InvalidPermissionException, NotFoundException {
        directArchiveSessionHibernateService.delete(id, user);
    }

    @Override
    public void touch(SessionData session) throws NotFoundException {
        Long id = session.getId();
        if(id != null) {
            directArchiveSessionHibernateService.touch(id);
        }
    }

    @Override
    public SessionData findByProjectTagName(String project, String tag, String name) throws NotFoundException {
        return directArchiveSessionHibernateService.findByProjectTagName(project, tag, name);
    }

    @Override
    public SessionData getOrCreate(SessionData incoming, AtomicBoolean isNew, String overwriteMode) throws ArchivingException {
        // Site-level feature flag: if direct archive append is not enabled, ignore the overwrite mode
        if (StringUtils.isNotBlank(overwriteMode) && !XDAT.getSiteConfigPreferences().getEnableDirectArchiveAppend()) {
            log.debug("Direct archive append is disabled site-wide; ignoring overwriteMode={}", overwriteMode);
            overwriteMode = null;
        }
        boolean     created = false;
        SessionData session = directArchiveSessionHibernateService.findBySessionData(incoming);
        if(session == null) {
            synchronized (createLock) {
                session = directArchiveSessionHibernateService.findBySessionData(incoming);
                if (session == null) {
                    if (Files.exists(Path.of(incoming.getUrl()))) {
                        if (StringUtils.isBlank(overwriteMode)) {
                            log.info("Session {} already exists at {} and append is not enabled; deferring to prearchive",
                                incoming.getSessionDataTriple(), incoming.getUrl());
                            return null;
                        }
                        // Allow writing directly into existing archive directory for append/overwrite
                        log.info("Direct archive merge: allowing receive into existing directory {} with overwriteMode={}", incoming.getUrl(), overwriteMode);
                    }
                    session = directArchiveSessionHibernateService.create(incoming);
                    if (StringUtils.isNotBlank(overwriteMode)) {
                        try {
                            directArchiveSessionHibernateService.setOverwriteMode(session.getId(), overwriteMode);
                        } catch (NotFoundException e) {
                            throw new ArchivingException("Failed to set overwrite mode on newly created session", e);
                        }
                    }
                    created = true;
                }
            }
        }
        if(!created) {
            if(session.getStatus() != PrearcUtils.PrearcStatus.RECEIVING) {
                throw new ArchivingException("Cannot direct archive additional files for session " + session.getSessionDataTriple() +
                                             " because it is no longer in receiving state (" + session.getStatus() + ")");
            }
        }
        isNew.set(created);
        return session;
    }

    @Override
    public void build(long id) throws NotFoundException, ArchivingException {
        SessionData target = directArchiveSessionHibernateService.setStatusToBuildingAndReturn(id);
        try {
            PrearcUtils.buildSession(target);
            directArchiveSessionHibernateService.setStatusToQueuedArchiving(id);
        } catch (Exception e) {
            log.error("Unable to build DirectArchiveSession id={}, moving to prearchive", id, e);
            moveToPrearchive(id, target, e);
        }
    }

    @Override
    public void archive(long id) throws NotFoundException, ArchivingException {
        SessionData target = directArchiveSessionHibernateService.setStatusToArchivingAndReturn(id);

        // Now, anonymize and archive
        // No perms checking, just use received file user
        boolean              anonymized = false;
        PersistentWorkflowI  workflow   = null;
        UserI                user       = receivedFileUserProvider.get();
        String               location   = target.getUrl();
        String               project    = target.getProject();
        XnatImagesessiondata session;
        try {
            session = populateSession(user, location, project);
            if (Boolean.FALSE.equals(target.getPreventAnon())) {
                List<AnonymizationResult>  anonResults = new ProjectAnonymizer(session, project, location, false).call();
                if (anonResults.stream().anyMatch(AnonymizationResultError.class::isInstance)) {
                    log.error("Anonymization failed for DirectArchiveSession id={} at {} ", id, location);
                    throw new ArchivingException("Anonymization failed for DirectArchiveSession id="+id+ "at "+location);
                }
                if (anonResults.stream().allMatch(AnonymizationResultNoOp.class::isInstance)) {
                    anonymized = false;
                } else {
                    MergeUtils.deleteRejectedFiles(log, anonResults, project);
                    anonymized = true;
                    // rebuild XML and update session
                    PrearcUtils.buildSession(target);
                    session = populateSession(user, location, project);
                }
            }

            MergeUtils.deleteEmptyDirectoriesRecursively(new File(location));

            // Determine if this is a merge into an existing archived session
            String overwriteMode = directArchiveSessionHibernateService.getOverwriteMode(id);
            boolean isMerge = StringUtils.isNotBlank(overwriteMode);

            if (isMerge) {
                archiveMerge(id, target, session, user, location, overwriteMode);
                return;
            }

            setSessionId(session);
            // TODO get rid of this check once XNAT-6889 is fixed
            if (!permissionsService.canCreate(user, session)) {
                groupsAndPermissionsCache.clearUserCache(user.getUsername());
            }
            PrearcSessionArchiver.preArchive(user, session, EMPTY_MAP, null);
            workflow = createWorkflow(user, session);
            saveSubject(session, workflow.buildEvent());
            setupScans(session, location);
            saveSession(session, workflow.buildEvent());
            PrearcSessionArchiver.postArchive(user, session, EMPTY_MAP);
            Files.delete(Path.of(location + ".xml"));
        } catch (Exception e) {
            log.error("Unable to archive DirectArchiveSession id={}, attempting to move to prearchive", id, e);
            if (workflow != null) {
                failWorkflow(workflow, e);
            }
            if (anonymized) {
                // keep from anonymizing again
                target.setPreventAnon(true);
            }
            moveToPrearchive(id, target, e);
            return;
        }

        // At this point, the session has been archived, so we no longer want to move to prearchive if there's an exception
        try {
            cleanupScans(session, location, workflow.buildEvent()); // could potentially be removed for performance. need to set format=DICOM in catalog prior to this
            directArchiveSessionHibernateService.delete(id);
            completeWorkflow(workflow);
        } catch (Exception e) {
            log.error("Issue after direct archive DirectArchiveSession id={}", id, e);
            failWorkflow(workflow, e);
        }
    }

    private XnatImagesessiondata populateSession(UserI user, String location, String project)
            throws IOException, SAXException {
        return new XNATSessionPopulater(user,
                                        new File(location),
                                        project,
                                        false).populate();
    }

    @Override
    public synchronized void triggerArchive() {
        // This method only runs on node assigned the direct archive task, and it is synchronized, so it cannot overlap
        // itself. A session could still be sent into building/archiving twice if additional files were received after
        // the build started, and this will be handled by sending the later files to the prearchive
        List<SessionData> sessions = directArchiveSessionHibernateService.findReadyForArchive();
        if(sessions != null) {
            for (SessionData session : sessions) {
                try {
                    triggerArchive(session);
                } catch (ClientException e) {
                    log.warn("Skip trigger archive", e);
                } catch (ServerException e) {
                    log.error("Unable to trigger archive", e);
                }
            }
        }

        // Complete any merge workflows whose timeout has expired with no new files arriving
        completePendingMergeWorkflows();
    }

    @Override
    public synchronized void triggerArchive(@Nonnull SessionData session) throws ClientException, ServerException {
        Long id = session.getId();
        if(id == null || PrearcUtils.isSessionReceiving(session.getSessionDataTriple())) {
            throw new ClientException("Refusing to trigger archive on DirectArchiveSession id=" + id +
                                      " because it is still receiving new files or doesn't have an id");
        }
        try {
            directArchiveSessionHibernateService.setStatusToQueuedBuilding(id);
        } catch (Exception e) {
            throw new ServerException("Issue setting status to queued building for DirectArchiveSession id=" + id, e);
        }
        try {
            XDAT.sendJmsRequest(jmsTemplate, new DirectArchiveRequest(id));
        } catch (Exception e) {
            directArchiveSessionHibernateService.setStatusBackToReceiving(id);
            throw new ServerException("Issue submitting request for DirectArchiveSession id=" + id, e);
        }
    }

    @Override
    public List<SessionData> getPaginated(UserI user, DirectArchiveSessionPaginatedRequest request) {
        List<String> projects = groupsAndPermissionsCache.getProjectsForUser(user.getUsername(), SecurityManager.READ);
        if(projects.isEmpty()){
            return Collections.emptyList();
        }

        // restrict to projects user can access
        restrictProjects(request, projects);
        return directArchiveSessionHibernateService.getPaginated(request).stream()
                                                   .map(DirectArchiveSession::toSessionData).collect(Collectors.toList());
    }

    private void restrictProjects(DirectArchiveSessionPaginatedRequest request, List<String> projects) {
        Map<String, Filter> filtersMap = request.getFiltersMap();
        HibernateFilter projectFilter = HibernateFilter.builder()
                                                       .values(projects.toArray())
                                                       .operator(HibernateFilter.Operator.IN).build();
        if(filtersMap.containsKey(PROJECT_KEY)) {
            HibernateFilter projectFilterAggregate = HibernateFilter.builder()
                                                                    .andFilters(Arrays.asList(projectFilter, (HibernateFilter) filtersMap.get(PROJECT_KEY)))
                                                                    .build();
            filtersMap.put(PROJECT_KEY, projectFilterAggregate);
        } else {
            filtersMap.put(PROJECT_KEY, projectFilter);
        }
        request.setFiltersMap(filtersMap);
    }

    private void saveSubject(XnatImagesessiondata session, EventMetaI c) throws Exception {
        UserI  user             = c.getUser();
        String project          = session.getProject();
        String subjectLabelOrId = StringUtils.firstNonBlank(session.getSubjectId(), session.getDcmpatientname());
        XnatSubjectdata subject = XnatSubjectdata.GetSubjectByIdOrProjectlabelCaseInsensitive(project,subjectLabelOrId,user,false);

        if(subject == null) {
            synchronized (subjectLock) {
                //recheck for subject
                subject = XnatSubjectdata.GetSubjectByIdOrProjectlabelCaseInsensitive(project,subjectLabelOrId,user,false);

                if(subject == null) {
                    subject = new XnatSubjectdata(user);
                    subject.setProject(project);
                    subject.setLabel(subjectLabelOrId);

                    subject.setId(XnatSubjectdata.CreateNewID());
                    SaveItemHelper.authorizedSave(subject, user, false, false, c);
                    XDAT.triggerXftItemEvent(subject, CREATE);
                }
            }
        }

        session.setSubjectId(subject.getId());
    }

    private void setSessionId(XnatImagesessiondata session) throws Exception {
        if(StringUtils.isBlank(session.getId())) {
            session.setId(XnatExperimentdata.CreateNewID());
        }
    }

    private void saveSession(XnatImagesessiondata session, EventMetaI c) throws Exception {
        saveSession(session, c, true);
    }

    private void saveSession(XnatImagesessiondata session, EventMetaI c, boolean isNew) throws Exception {
        UserI user = c.getUser();
        // For merge/update (isNew=false), allow item removal so that overwritten
        // catalog entries (e.g. replaced DICOM files) can be saved.
        boolean allowItemRemoval = !isNew;
        if(SaveItemHelper.authorizedSave(session, c.getUser(), false, allowItemRemoval, c)){
            XDAT.triggerXftItemEvent(session, isNew ? CREATE : UPDATE);
        }
    }

    private void setupScans(XnatImagesessiondata session, String root) {
        PrearcUtils.setupScans(session, root);
    }

    private void cleanupScans(XnatImagesessiondata session, String root, EventMetaI c) {
        PrearcUtils.cleanupScans(session, root, c);
    }

    private PersistentWorkflowI createWorkflow(UserI user, XnatImagesessiondata session)
            throws PersistentWorkflowUtils.EventRequirementAbsent {
        PersistentWorkflowI workflow = PersistentWorkflowUtils.buildOpenWorkflow(user, session.getItem(),
                                                                                 EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE,
                                                                                                             EventUtils.TRANSFER, "Direct-to-archive upload", null));
        assert workflow != null;
        workflow.setStepDescription("Archiving");
        return workflow;
    }

    private void completeWorkflow(PersistentWorkflowI workflow) throws Exception {
        workflow.setStepDescription(PersistentWorkflowUtils.COMPLETE);
        WorkflowUtils.complete(workflow, workflow.buildEvent());
    }

    private void failWorkflow(PersistentWorkflowI workflow, Exception cause) {
        workflow.setComments("Exception: " + cause.getMessage());
        try {
            WorkflowUtils.fail(workflow, workflow.buildEvent());
        } catch (Exception e) {
            log.error("Unable to fail workflow {}", workflow, e);
        }
    }

    private void moveToPrearchive(long id, SessionData target, Exception origException)
            throws NotFoundException {
        doPrearchiveMove(id, target, Rebuild, null, origException);
    }

    private void doPrearchiveMove(long id, SessionData target, Operation nextOperation,
                                  @Nullable PrearcUtils.PrearcStatus status,
                                  @Nullable Exception origException)
            throws NotFoundException {
        UserI user = receivedFileUserProvider.get();
        try {
            File prearchivePath = PrearcUtils.getPrearcSessionDir(user, target.getProject(), target.getTimestamp(),
                                                                  target.getFolderName(), true);

            // ensure target location is empty, increment timestamp if needed
            if(Files.exists(prearchivePath.toPath())) {
                target.setTimestamp(target.getTimestamp() + "_DA");
                prearchivePath = PrearcUtils.getPrearcSessionDir(user, target.getProject(), target.getTimestamp(),
                                                                 target.getFolderName(), true);
            }

            // move files
            String archivePath = target.getUrl();
            FileUtils.moveDirectory(new File(archivePath), prearchivePath);
            String xml = archivePath.replaceAll(Matcher.quoteReplacement(File.separator) + "$", "")
                         + ".xml";
            Path xmlSource = Path.of(xml);
            Path xmlDest   = Path.of(prearchivePath.getParent(), prearchivePath.getName() + ".xml");
            if(Files.exists(xmlSource)) {
                // Adjust prearchive path in xml to point to prearchive rather than archive and save
                XnatImagesessiondataBean session = PrearcTableBuilder.parseSession(xmlSource.toFile());
                session.setPrearchivepath(prearchivePath.getAbsolutePath());
                try (final FileOutputStream fos = new FileOutputStream(xmlDest.toFile());
                     final OutputStreamWriter fw = new OutputStreamWriter(fos)) {
                    session.toXML(fw);
                }
            }

            if(origException != null) {
                // add exception info to prearchive log for Details view
                File logFile = prearchivePath.toPath()
                                             .resolve(Path.of("logs", "directArchive" + id + ".log")).toFile();
                Files.createDirectories(logFile.getParentFile().toPath());
                try (FileWriter fileWriter = new FileWriter(logFile);
                     PrintWriter printWriter = new PrintWriter(fileWriter)) {
                    printWriter.print("Attempt to direct-archive failed\n");
                    origException.printStackTrace(printWriter);
                }
            }

            // create db entry
            target.setUrl(prearchivePath.getAbsolutePath());
            if(status == null) {
                status = Files.exists(xmlDest) ?
                         PrearcUtils.PrearcStatus.ERROR : PrearcUtils.PrearcStatus.RECEIVING;
            }
            target.setStatus(status);
            PrearcDatabase.Either<SessionData, SessionData> sd = PrearcDatabase.eitherGetOrCreateSession(target,
                                                                                                         prearchivePath.getParentFile(),
                                                                                                         PrearchiveCode.AutoArchive);
            SessionData prearchiveSession = sd.isLeft() ? sd.getLeft() : sd.getRight();
            if (prearchiveSession != null && prearchiveSession.getStatus() == PrearcUtils.PrearcStatus.RECEIVING) {
                PrearcUtils.queuePrearchiveOperation(new PrearchiveOperationRequest(receivedFileUserProvider.get(),
                                                                                    nextOperation,
                                                                                    prearchiveSession,
                                                                                    new File(prearchiveSession.getUrl())));
            }
            directArchiveSessionHibernateService.delete(id);
        } catch (Exception e) {
            log.error("Unable to move {} to prearchive", target, e);
            directArchiveSessionHibernateService.setStatusToError(id, e);
        }
    }

    // ---- Direct-to-archive merge (append/overwrite) support ----

    private void archiveMerge(long id, SessionData target,
                              XnatImagesessiondata incomingSession,
                              UserI user, String location,
                              String overwriteMode) throws Exception {
        // Look up the existing archived session from XNAT DB
        XnatExperimentdata existing = BaseXnatExperimentdata
                .GetExptByProjectIdentifier(target.getProject(), incomingSession.getLabel(), user, false);

        if (existing == null || !(existing instanceof XnatImagesessiondata existingSession)) {
            // Session was deleted between receive and archive -- treat as fresh archive
            log.info("Merge target session not found for {}, treating as fresh archive", target.getSessionDataTriple());
            setSessionId(incomingSession);
            if (!permissionsService.canCreate(user, incomingSession)) {
                groupsAndPermissionsCache.clearUserCache(user.getUsername());
            }
            PrearcSessionArchiver.preArchive(user, incomingSession, EMPTY_MAP, null);
            PersistentWorkflowI workflow = createWorkflow(user, incomingSession);
            saveSubject(incomingSession, workflow.buildEvent());
            setupScans(incomingSession, location);
            saveSession(incomingSession, workflow.buildEvent());
            PrearcSessionArchiver.postArchive(user, incomingSession, EMPTY_MAP);
            Files.deleteIfExists(Path.of(location + ".xml"));
            cleanupScans(incomingSession, location, workflow.buildEvent());
            directArchiveSessionHibernateService.delete(id);
            completeWorkflow(workflow);
            return;
        }

        boolean overwriteFiles = PrearcUtils.DELETE.equals(overwriteMode);
        log.info("Direct archive merge into existing session {} (overwriteMode={}, overwriteFiles={})",
                existingSession.getId(), overwriteMode, overwriteFiles);

        // Reuse the existing session's ID
        incomingSession.setId(existingSession.getId());

        // For each incoming scan, merge into existing session
        List<XnatImagescandataI> existingScans = existingSession.getScans_scan();
        for (XnatImagescandataI incomingScan : incomingSession.getScans_scan()) {
            // Find matching scan by SeriesInstanceUID in existing session
            XnatImagescandataI matchingScan = MergeUtils.getMatchingScanByUID(incomingScan, existingScans);

            if (matchingScan != null) {
                // CASE A: Appending files to an existing scan -- merge catalogs
                log.debug("Merging files into existing scan {} (UID={})", matchingScan.getId(), matchingScan.getUid());
                mergeScanCatalogs(incomingScan, matchingScan, target.getProject(), location, overwriteFiles, user);
            } else {
                // CASE B: New scan -- check for scan ID collision
                XnatImagescandataI idCollision = MergeUtils.getMatchingScan(incomingScan, existingScans);
                if (idCollision != null) {
                    // Scan ID exists but different SeriesInstanceUID -- rename incoming scan
                    String newId = generateUniqueScanId(incomingScan, existingScans);
                    log.info("Scan ID collision: renaming incoming scan {} to {} (UID={})",
                            incomingScan.getId(), newId, incomingScan.getUid());
                    renameScanOnDisk(incomingScan, newId, location);
                }
                // Add new scan to existing session
                existingSession.addScans_scan(incomingScan);
            }
        }

        // Set up scan resources with archive paths
        setupScans(existingSession, location);

        // Get or reuse an open Merge workflow (pipeline_name='Merged').
        // Creates and persists a new one if none exists.
        PersistentWorkflowI workflow = getOrCreateMergeWorkflow(user, existingSession);

        // Save updated session to DB (UPDATE, not CREATE).
        // Because the workflow has pipeline_name='Merged', EventServiceItemSaveAspect
        // will trigger SessionEvent.Status.MERGED for event service subscribers.
        PrearcSessionArchiver.preArchive(user, existingSession, EMPTY_MAP, existingSession);
        saveSession(existingSession, workflow.buildEvent(), false);
        PrearcSessionArchiver.postArchive(user, existingSession, EMPTY_MAP);

        // Finalize catalogs
        cleanupScans(existingSession, location, workflow.buildEvent());

        // Clean up the session XML
        Files.deleteIfExists(Path.of(location + ".xml"));

        // Delete the DirectArchiveSession tracking entry for this batch
        directArchiveSessionHibernateService.delete(id);

        // Do NOT complete the workflow here -- it stays In Progress because more files
        // may arrive. Track this experiment for eventual workflow completion by the
        // periodic triggerArchive handler when no new files arrive within the timeout.
        pendingMergeCompletions.put(existingSession.getId(),
                new PendingMergeInfo(target.getProject(), target.getTag(), target.getName()));
    }

    private void mergeScanCatalogs(XnatImagescandataI incomingScan, XnatImagescandataI existingScan,
                                   String project, String rootPath, boolean overwriteFiles,
                                   UserI user) throws Exception {
        String fixedRootPath = rootPath.endsWith(File.separator) ? rootPath : rootPath + File.separator;
        for (XnatAbstractresourceI incomingResource : incomingScan.getFile()) {
            if (!(incomingResource instanceof XnatResourcecatalog incomingCatalog)) {
                continue;
            }
            // Find matching resource in existing scan
            XnatAbstractresourceI existingResource = MergeUtils.getMatchingResource(incomingResource, existingScan.getFile());
            if (existingResource instanceof XnatResourcecatalog existingCatalogResource) {
                // Merge entries from incoming catalog into existing catalog
                CatalogUtils.CatalogData existingCatData = CatalogUtils.CatalogData.getOrCreate(
                        fixedRootPath, existingCatalogResource, project);

                CatalogUtils.CatalogData incomingCatData = CatalogUtils.CatalogData.getOrCreate(
                        fixedRootPath, incomingCatalog, project);

                for (CatEntryI incomingEntry : incomingCatData.catBean.getEntries_entry()) {
                    // Check for duplicate by SOP Instance UID (for DICOM entries)
                    boolean duplicate = false;
                    if (incomingEntry instanceof CatDcmentryI dcmEntry && StringUtils.isNotBlank(dcmEntry.getUid())) {
                        CatDcmentryI existingDcm = CatalogUtils.getDCMEntryByUID(existingCatData.catBean, dcmEntry.getUid());
                        if (existingDcm != null) {
                            duplicate = true;
                            if (overwriteFiles) {
                                log.debug("Overwriting existing catalog entry with UID={}", dcmEntry.getUid());
                                CatalogUtils.addOrUpdateEntry(existingCatData, existingDcm,
                                        incomingEntry.getUri(), incomingEntry.getUri(),
                                        new File(existingCatData.catFile.getParentFile(), incomingEntry.getUri()),
                                        null, null);
                            } else {
                                log.debug("Skipping duplicate DICOM entry with UID={} (append mode)", dcmEntry.getUid());
                            }
                        }
                    }
                    if (!duplicate) {
                        existingCatData.catBean.addEntries_entry(incomingEntry);
                    }
                }

                CatalogUtils.writeCatalogToFile(existingCatData);
            } else {
                // No matching resource in existing scan -- add the incoming resource
                existingScan.addFile(incomingResource);
            }
        }
    }

    private String generateUniqueScanId(XnatImagescandataI scan, List<XnatImagescandataI> existingScans) {
        // Extract modality code from XSI type (e.g., xnat:mrscandata -> MR)
        String xsiType = scan.getXSIType();
        String modalityCode = "";
        if (xsiType != null && xsiType.startsWith("xnat:") && xsiType.length() >= 7) {
            modalityCode = xsiType.substring(5, 7).toUpperCase();
            if ("PE".equals(modalityCode)) {
                modalityCode = "PT";
            }
        }

        String originalId = scan.getId();
        int counter = 1;
        String newId;
        do {
            newId = originalId + "-" + modalityCode + counter;
            counter++;
        } while (scanIdExists(newId, existingScans));

        return newId;
    }

    private boolean scanIdExists(String scanId, List<XnatImagescandataI> scans) {
        return scans.stream().anyMatch(s -> scanId.equals(s.getId()));
    }

    private void renameScanOnDisk(XnatImagescandataI scan, String newId, String sessionDir) throws IOException {
        File scansDir = new File(sessionDir, "scans");
        File oldScanDir = new File(scansDir, scan.getId());
        File newScanDir = new File(scansDir, newId);

        if (oldScanDir.exists()) {
            FileUtils.moveDirectory(oldScanDir, newScanDir);
            log.debug("Renamed scan directory {} to {}", oldScanDir, newScanDir);
        }

        // Update the scan's ID
        scan.setId(newId);

        // Update catalog file references if present
        for (XnatAbstractresourceI resource : scan.getFile()) {
            if (resource instanceof XnatResourcecatalog catalog) {
                String oldUri = catalog.getUri();
                if (oldUri != null) {
                    catalog.setUri(oldUri.replace(oldScanDir.getName(), newId));
                }
            }
        }
    }

    /**
     * Create a new Merge workflow for direct-to-archive merge operations and save it to the
     * database immediately in In Progress state.
     */
    private PersistentWorkflowI createMergeWorkflow(UserI user, XnatImagesessiondata session) throws Exception {
        PersistentWorkflowI workflow = PersistentWorkflowUtils.buildOpenWorkflow(user, session.getItem(),
                EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE,
                        EventUtils.MERGE, "Direct-to-archive upload", null));
        assert workflow != null;
        workflow.setStepDescription("Archiving");
        PersistentWorkflowUtils.save(workflow, workflow.buildEvent());
        return workflow;
    }

    /**
     * Find an existing open (In Progress) Merge workflow with justification "Direct-to-archive upload"
     * for this session, or create a new one. This ensures that multiple file additions to the same
     * session share a single workflow entry rather than creating one per batch.
     */
    private PersistentWorkflowI getOrCreateMergeWorkflow(UserI user, XnatImagesessiondata session) throws Exception {
        // Look for existing open Merge workflows for this session
        Collection<? extends PersistentWorkflowI> openWorkflows =
                PersistentWorkflowUtils.getOpenWorkflows(user, session.getId());

        PersistentWorkflowI existing = openWorkflows.stream()
                .filter(w -> EventUtils.MERGE.equals(w.getPipelineName()))
                .filter(w -> "Direct-to-archive upload".equals(w.getJustification()))
                .max(Comparator.comparing(PersistentWorkflowI::getLaunchTimeDate))
                .orElse(null);

        if (existing != null) {
            log.debug("Reusing existing merge workflow for session {}", session.getId());
            existing.setStepDescription("Archiving (append)");
            return existing;
        }

        // No existing open Merge workflow -- create and persist a new one
        return createMergeWorkflow(user, session);
    }

    /**
     * Check for merge workflows that should be completed because no new files have arrived
     * within the timeout period. Called periodically from {@link #triggerArchive()}.
     *
     * For each tracked experiment:
     * 1. Skip if the timeout hasn't elapsed since the last merge
     * 2. Skip if there's still a RECEIVING DirectArchiveSession for this study (files still arriving)
     * 3. Otherwise, find the open Merge workflow and set it to Complete
     */
    private void completePendingMergeWorkflows() {
        if (pendingMergeCompletions.isEmpty()) {
            return;
        }

        long timeoutMs = XDAT.getSiteConfigPreferences().getSessionXmlRebuilderInterval() * 60 * 1000L;
        long now = System.currentTimeMillis();
        UserI user = receivedFileUserProvider.get();

        for (Map.Entry<String, PendingMergeInfo> entry : pendingMergeCompletions.entrySet()) {
            String experimentId = entry.getKey();
            PendingMergeInfo pm = entry.getValue();

            if (now - pm.lastMergeTime < timeoutMs) {
                continue; // Not enough time has passed since last merge
            }

            // Check if there's still a RECEIVING DirectArchiveSession for this study
            try {
                SessionData active = directArchiveSessionHibernateService.findByProjectTagName(
                        pm.project, pm.tag, pm.name);
                if (active.getStatus() == PrearcUtils.PrearcStatus.RECEIVING) {
                    log.debug("Still receiving files for experiment {}, deferring merge workflow completion",
                            experimentId);
                    continue;
                }
            } catch (NotFoundException e) {
                // No active DirectArchiveSession -- safe to complete the workflow
            }

            // Find and complete the open Merge workflow for this experiment
            try {
                Collection<? extends PersistentWorkflowI> openWorkflows =
                        PersistentWorkflowUtils.getOpenWorkflows(user, experimentId);

                for (PersistentWorkflowI wf : openWorkflows) {
                    if (EventUtils.MERGE.equals(wf.getPipelineName()) &&
                            "Direct-to-archive upload".equals(wf.getJustification())) {
                        log.info("Completing direct-archive merge workflow for experiment {}", experimentId);
                        completeWorkflow(wf);
                    }
                }
            } catch (Exception e) {
                log.error("Error completing merge workflow for experiment {}", experimentId, e);
            }

            pendingMergeCompletions.remove(experimentId);
        }
    }

    private final JmsTemplate                          jmsTemplate;
    private final XnatUserProvider                     receivedFileUserProvider;
    private final DirectArchiveSessionHibernateService directArchiveSessionHibernateService;
    private final GroupsAndPermissionsCache            groupsAndPermissionsCache;
    private final PermissionsServiceI                  permissionsService;

    private static final String PROJECT_KEY = "project";
}