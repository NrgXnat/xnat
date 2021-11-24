package org.nrg.xnat.archive.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.framework.ajax.Filter;
import org.nrg.framework.ajax.hibernate.HibernateFilter;
import org.nrg.framework.constants.PrearchiveCode;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.bean.XnatPetmrsessiondataBean;
import org.nrg.xdat.bean.reader.XDATXMLReader;
import org.nrg.xdat.om.*;
import org.nrg.xdat.preferences.HandlePetMr;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xft.db.MaterializedView;
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
import org.nrg.xnat.helpers.merge.ProjectAnonymizer;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcTableBuilder;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.services.messaging.archive.DirectArchiveRequest;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;
import org.nrg.xnat.turbine.utils.XNATSessionPopulater;
import org.nrg.xnat.utils.WorkflowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jms.Destination;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static org.nrg.xft.event.XftItemEventI.CREATE;
import static org.nrg.xnat.archive.Operation.Rebuild;
import static org.nrg.xnat.archive.Operation.Separate;

@Slf4j
@Service
public class DirectArchiveSessionServiceImpl implements DirectArchiveSessionService {
    private static final Map<String, Object> EMPTY_MAP = Collections.emptyMap();

    @Autowired
    public DirectArchiveSessionServiceImpl(final DirectArchiveSessionHibernateService directArchiveSessionHibernateService,
                                           final Destination prearchiveOperationRequest,
                                           final JmsTemplate jmsTemplate,
                                           final XnatUserProvider receivedFileUserProvider,
                                           final GroupsAndPermissionsCache groupsAndPermissionsCache,
                                           final PermissionsServiceI permissionsService) {
        this.directArchiveSessionHibernateService = directArchiveSessionHibernateService;
        this.jmsTemplate = jmsTemplate;
        this.prearchiveOperationDestination = prearchiveOperationRequest;
        this.receivedFileUserProvider = receivedFileUserProvider;
        this.groupsAndPermissionsCache = groupsAndPermissionsCache;
        this.permissionsService = permissionsService;
    }

    @Override
    public void delete(SessionData session) {
        Long id = session.getId();
        if (id != null) {
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
        if (id != null) {
            directArchiveSessionHibernateService.touch(id);
        }
    }

    @Override
    public SessionData findByProjectTagName(String project, String tag, String name) throws NotFoundException {
        return directArchiveSessionHibernateService.findByProjectTagName(project, tag, name);
    }

    @Override
    public SessionData getOrCreate(SessionData incoming, AtomicBoolean isNew) throws ArchivingException {
        boolean created = false;
        SessionData session;
        synchronized (this) {
            session = directArchiveSessionHibernateService.findBySessionData(incoming);
            if (session == null) {
                if (Files.exists(Paths.get(incoming.getUrl()))) {
                    throw new ArchivingException("Cannot direct archive session " + incoming.getSessionDataTriple() +
                            " because data already exists in " + incoming.getUrl());
                }
                session = directArchiveSessionHibernateService.create(incoming);
                created = true;
            }
        }
        if (!created) {
            if (session.getStatus() != PrearcUtils.PrearcStatus.RECEIVING) {
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
        try {
            // If PET/MR are to be separated, we have to go through the prearchive
            if (handleSeparatePetMr(id, target)) {
                return;
            }
        } catch (Exception e) {
            log.error("Issue during move to prearchive for PET/MR split for DirectArchiveSession id={}", id, e);
            directArchiveSessionHibernateService.setStatusToError(id, e);
            return;
        }

        // Now, anonymize and archive
        // No perms checking, just use received file user
        boolean anonymized = false;
        PersistentWorkflowI workflow = null;
        UserI user = receivedFileUserProvider.get();
        String location = target.getUrl();
        String project = target.getProject();
        XnatImagesessiondata session;
        try {
            session = populateSession(user, location, project);
            if (!target.getPreventAnon()) {
                anonymized = new ProjectAnonymizer(session, project, location).call();
                if (anonymized) {
                    // rebuild XML and update session
                    PrearcUtils.buildSession(target);
                    session = populateSession(user, location, project);
                }
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
            Files.delete(Paths.get(location + ".xml"));
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
        // This method only runs on node assigned the direct archive task, and it is synchronized so it cannot overlap itself
        // A session could still be sent into building/archiving twice if addl files were received after the build started,
        // and this will be handled by sending the later files to the prearchive
        List<SessionData> sessions = directArchiveSessionHibernateService.findReadyForArchive();
        if (sessions == null) {
            return;
        }
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

    @Override
    public synchronized void triggerArchive(@Nonnull SessionData session) throws ClientException, ServerException {
        Long id = session.getId();
        if (id == null || PrearcUtils.isSessionReceiving(session.getSessionDataTriple())) {
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
        // restrict to projects user can access
        restrictProjects(request, user);

        return directArchiveSessionHibernateService.getPaginated(request).stream()
                .map(DirectArchiveSession::toSessionData).collect(Collectors.toList());
    }

    private void restrictProjects(DirectArchiveSessionPaginatedRequest request, UserI user) {
        Map<String, Filter> filtersMap = request.getFiltersMap();
        List<String> projects = groupsAndPermissionsCache.getProjectsForUser(user.getUsername(), SecurityManager.READ);
        HibernateFilter projectFilter = HibernateFilter.builder()
                .values(projects.toArray())
                .operator(HibernateFilter.Operator.IN).build();
        if (filtersMap.containsKey(PROJECT_KEY)) {
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
        UserI user = c.getUser();
        String project = session.getProject();
        String subjectLabelOrId = StringUtils.firstNonBlank(session.getSubjectId(), session.getDcmpatientname());
        synchronized (this) {
            // try by ID
            XnatSubjectdata existing = XnatSubjectdata.getXnatSubjectdatasById(subjectLabelOrId, user, false);
            if (existing == null) {
                // try by label
                existing = XnatSubjectdata.GetSubjectByProjectIdentifierCaseInsensitive(session.getProject(),
                        subjectLabelOrId, user, false);
            }
            if (existing != null) {
                session.setSubjectId(existing.getId());
                return;
            }
            final XnatSubjectdata subject = new XnatSubjectdata(user);
            subject.setProject(project);
            subject.setLabel(subjectLabelOrId);

            subject.setId(XnatSubjectdata.CreateNewID());
            SaveItemHelper.authorizedSave(subject, user, false, false, c);
            XDAT.triggerXftItemEvent(subject, CREATE);
            session.setSubjectId(subject.getId());
        }
    }

    private void setSessionId(XnatImagesessiondata session) throws Exception {
        if (StringUtils.isBlank(session.getId())) {
            session.setId(XnatExperimentdata.CreateNewID());
        }
    }

    private void saveSession(XnatImagesessiondata session, EventMetaI c) throws Exception {
        UserI user = c.getUser();
        if (!SaveItemHelper.authorizedSave(session, c.getUser(), false, false, c)) {
            throw new ArchivingException("Unable to save session");
        }

        XDAT.triggerXftItemEvent(session, CREATE);
        Users.clearCache(user);
        try {
            MaterializedView.deleteByUser(user);
        } catch (Exception e) {
            log.error("Unable to delete user materialized views", e);
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

    private boolean handleSeparatePetMr(long id, SessionData target) throws IOException, SAXException, NotFoundException {
        if (!HandlePetMr.shouldSeparatePetMr(target.getProject())) {
            // not splitting
            return false;
        }
        final File sessionXml = new File(target.getUrl() + ".xml");
        final XnatImagesessiondataBean bean = (XnatImagesessiondataBean) new XDATXMLReader().parse(sessionXml);
        if (!(bean instanceof XnatPetmrsessiondataBean)) {
            // nothing to split
            return false;
        }
        moveToPrearchiveAndRequestSeparatePetMr(id, target);
        return true;
    }

    private void moveToPrearchiveAndRequestSeparatePetMr(long id, SessionData target) throws NotFoundException {
        doPrearchiveMove(id, target, Separate, PrearcUtils.PrearcStatus.RECEIVING, null);
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
            if (Files.exists(prearchivePath.toPath())) {
                target.setTimestamp(target.getTimestamp() + "_DA");
                prearchivePath = PrearcUtils.getPrearcSessionDir(user, target.getProject(), target.getTimestamp(),
                        target.getFolderName(), true);
            }

            // move files
            String archivePath = target.getUrl();
            FileUtils.moveDirectory(new File(archivePath), prearchivePath);
            String xml = archivePath.replaceAll(Matcher.quoteReplacement(File.separator) + "$", "")
                    + ".xml";
            Path xmlSource = Paths.get(xml);
            Path xmlDest = Paths.get(prearchivePath.getParent(), prearchivePath.getName() + ".xml");
            if (Files.exists(xmlSource)) {
                // Adjust prearchive path in xml to point to prearchive rather than archive and save
                XnatImagesessiondataBean session = PrearcTableBuilder.parseSession(xmlSource.toFile());
                session.setPrearchivepath(prearchivePath.getAbsolutePath());
                try (final FileOutputStream fos = new FileOutputStream(xmlDest.toFile());
                     final OutputStreamWriter fw = new OutputStreamWriter(fos)) {
                    session.toXML(fw);
                }
            }

            if (origException != null) {
                // add exception info to prearchive log for Details view
                File logFile = prearchivePath.toPath()
                        .resolve(Paths.get("logs", "directArchive" + id + ".log")).toFile();
                Files.createDirectories(logFile.getParentFile().toPath());
                try (FileWriter fileWriter = new FileWriter(logFile);
                     PrintWriter printWriter = new PrintWriter(fileWriter)) {
                    printWriter.print("Attempt to direct-archive failed\n");
                    origException.printStackTrace(printWriter);
                }
            }

            // create db entry
            target.setUrl(prearchivePath.getAbsolutePath());
            if (status == null) {
                status = Files.exists(xmlDest) ?
                        PrearcUtils.PrearcStatus.ERROR : PrearcUtils.PrearcStatus.RECEIVING;
            }
            target.setStatus(status);
            PrearcDatabase.Either<SessionData, SessionData> sd = PrearcDatabase.eitherGetOrCreateSession(target,
                    prearchivePath.getParentFile(), PrearchiveCode.AutoArchive);
            SessionData prearchiveSession = sd.isLeft() ? sd.getLeft() : sd.getRight();
            if (prearchiveSession != null && prearchiveSession.getStatus() == PrearcUtils.PrearcStatus.RECEIVING) {
                jmsTemplate.convertAndSend(prearchiveOperationDestination,
                        new PrearchiveOperationRequest(receivedFileUserProvider.get(), nextOperation,
                                prearchiveSession, new File(prearchiveSession.getUrl())));
            }
            directArchiveSessionHibernateService.delete(id);
        } catch (Exception e) {
            log.error("Unable to move {} to prearchive", target, e);
            directArchiveSessionHibernateService.setStatusToError(id, e);
        }
    }

    private final JmsTemplate jmsTemplate;
    private final Destination prearchiveOperationDestination;
    private final XnatUserProvider receivedFileUserProvider;
    private final DirectArchiveSessionHibernateService directArchiveSessionHibernateService;
    private final GroupsAndPermissionsCache groupsAndPermissionsCache;
    private final PermissionsServiceI permissionsService;

    private static final String PROJECT_KEY = "project";
}