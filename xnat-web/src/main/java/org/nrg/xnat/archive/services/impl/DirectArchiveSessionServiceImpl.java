package org.nrg.xnat.archive.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.ajax.Filter;
import org.nrg.framework.ajax.hibernate.HibernateFilter;
import org.nrg.framework.constants.PrearchiveCode;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.om.*;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.archive.ArchivingException;
import org.nrg.xnat.archive.entities.DirectArchiveSession;
import org.nrg.xnat.archive.services.DirectArchiveSessionHibernateService;
import org.nrg.xnat.archive.services.DirectArchiveSessionService;
import org.nrg.xnat.archive.xapi.DirectArchiveSessionPaginatedRequest;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcTableBuilder;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.services.cache.DefaultGroupsAndPermissionsCache;
import org.nrg.xnat.services.messaging.archive.DirectArchiveRequest;
import org.nrg.xnat.turbine.utils.XNATSessionPopulater;
import org.nrg.xnat.utils.WorkflowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static org.nrg.xft.event.XftItemEventI.CREATE;

@Slf4j
@Service
public class DirectArchiveSessionServiceImpl implements DirectArchiveSessionService {
    @Autowired
    public DirectArchiveSessionServiceImpl(final DirectArchiveSessionHibernateService directArchiveSessionHibernateService,
                                           final JmsTemplate jmsTemplate,
                                           final XnatUserProvider receivedFileUserProvider,
                                           final DefaultGroupsAndPermissionsCache groupsAndPermissionsCache) {
        this.directArchiveSessionHibernateService = directArchiveSessionHibernateService;
        this.jmsTemplate = jmsTemplate;
        this.receivedFileUserProvider = receivedFileUserProvider;
        this.groupsAndPermissionsCache = groupsAndPermissionsCache;
    }

    @Override
    public void delete(SessionData session) {
        Long id = session.getId();
        if (id != null) {
            directArchiveSessionHibernateService.delete(id);
        }
    }

    @Override
    public void touch(SessionData session) throws NotFoundException {
        Long id = session.getId();
        if (id != null) {
            directArchiveSessionHibernateService.touch(id);
        }
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
            moveToPrearchive(id, target, receivedFileUserProvider.get(), e);
        }
    }

    @Override
    public void archive(long id) throws NotFoundException, ArchivingException {
        PersistentWorkflowI workflow = null;
        UserI user = receivedFileUserProvider.get();
        SessionData target = directArchiveSessionHibernateService.setStatusToArchivingAndReturn(id);
        String location = target.getUrl();
        XnatImagesessiondata session;
        try {
            // No perms checking, just use received file user
            session = new XNATSessionPopulater(user,
                    new File(location),
                    target.getProject(),
                    false).populate();
            setSessionId(session);
            workflow = createWorkflow(user, session);
            saveSubject(session, workflow.buildEvent());
            setupScans(session, location);
            saveSession(session, workflow.buildEvent());
        } catch (Exception e) {
            log.error("Unable to archive DirectArchiveSession id={}", id, e);
            if (workflow != null) {
                failWorkflow(workflow, e);
            }
            moveToPrearchive(id, target, user, e);
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
            Long id = session.getId();
            if (id == null || PrearcUtils.isSessionReceiving(session.getSessionDataTriple())) {
                continue;
            }
            try {
                directArchiveSessionHibernateService.setStatusToQueuedBuilding(id);
            } catch (Exception e) {
                log.error("Issue setting status to queued building for DirectArchiveSession id={}", id, e);
                continue;
            }
            try {
                XDAT.sendJmsRequest(jmsTemplate, new DirectArchiveRequest(id));
            } catch (Exception e) {
                log.error("Issue submitting request for DirectArchiveSession id={}", id, e);
                directArchiveSessionHibernateService.setStatusBackToReceiving(id);
            }
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

    private void moveToPrearchive(long id, SessionData target, UserI user, @Nonnull Exception origException)
            throws NotFoundException {
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

            // add exception info to prearchive log for Details view
            File logFile = prearchivePath.toPath()
                    .resolve(Paths.get( "logs", "directArchive" + id + ".log")).toFile();
            Files.createDirectories(logFile.getParentFile().toPath());
            try (FileWriter fileWriter = new FileWriter(logFile);
                 PrintWriter printWriter = new PrintWriter(fileWriter)) {
                printWriter.print("Attempt to direct-archive failed\n");
                origException.printStackTrace(printWriter);
            }

            // create db entry
            target.setUrl(prearchivePath.getAbsolutePath());
            PrearcUtils.PrearcStatus status = Files.exists(xmlDest) ?
                    PrearcUtils.PrearcStatus.ERROR : PrearcUtils.PrearcStatus.RECEIVING;
            target.setStatus(status);
            PrearcDatabase.eitherGetOrCreateSession(target, prearchivePath.getParentFile(), PrearchiveCode.AutoArchive);
            directArchiveSessionHibernateService.delete(id);
        } catch (Exception e) {
            log.error("Unable to move {} to prearchive", target, e);
            directArchiveSessionHibernateService.setStatusToError(id, e);
        }
    }

    private final JmsTemplate jmsTemplate;
    private final XnatUserProvider receivedFileUserProvider;
    private final DirectArchiveSessionHibernateService directArchiveSessionHibernateService;
    private final DefaultGroupsAndPermissionsCache groupsAndPermissionsCache;

    private static final String PROJECT_KEY = "project";
}