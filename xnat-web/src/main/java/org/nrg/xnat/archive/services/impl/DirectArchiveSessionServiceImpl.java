package org.nrg.xnat.archive.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.constants.PrearchiveCode;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.*;
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
import org.nrg.xnat.archive.services.DirectArchiveSessionHibernateService;
import org.nrg.xnat.archive.services.DirectArchiveSessionService;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.services.messaging.archive.DirectArchiveRequest;
import org.nrg.xnat.turbine.utils.XNATSessionPopulater;
import org.nrg.xnat.utils.WorkflowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.nrg.xft.event.XftItemEventI.CREATE;

@Slf4j
@Service
public class DirectArchiveSessionServiceImpl implements DirectArchiveSessionService {
    @Autowired
    public DirectArchiveSessionServiceImpl(final DirectArchiveSessionHibernateService directArchiveSessionHibernateService,
                                           final JmsTemplate jmsTemplate,
                                           final XnatUserProvider receivedFileUserProvider) {
        this.directArchiveSessionHibernateService = directArchiveSessionHibernateService;
        this.jmsTemplate = jmsTemplate;
        this.receivedFileUserProvider = receivedFileUserProvider;
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
        } catch (Exception e) {
            log.error("Unable to build DirectArchiveSession id={}, moving to prearchive", id, e);
            moveToPrearchive(target, receivedFileUserProvider.get());
            directArchiveSessionHibernateService.setStatusToError(id, e);
        }
        directArchiveSessionHibernateService.setStatusToQueuedArchiving(id);
    }

    @Override
    public void archive(long id) throws NotFoundException, ArchivingException {
        PersistentWorkflowI workflow = null;
        UserI user = receivedFileUserProvider.get();
        SessionData target = directArchiveSessionHibernateService.setStatusToArchivingAndReturn(id);
        try {
            String location = target.getUrl();
            // No perms checking, just use received file user
            XnatImagesessiondata session = new XNATSessionPopulater(user,
                    new File(location),
                    target.getProject(),
                    false).populate();
            setSessionId(session);
            workflow = createWorkflow(user, session);
            saveSubject(session, workflow.buildEvent());
            setupScans(session, location);
            saveSession(session, workflow.buildEvent());
            cleanupScans(session, location, workflow.buildEvent()); // could potentially be removed for performance. need to set format=DICOM in catalog prior to this
            completeWorkflow(workflow);
            directArchiveSessionHibernateService.delete(id);
        } catch (Exception e) {
            log.error("Unable to archive DirectArchiveSession id={}", id, e);
            if (workflow != null) {
                failWorkflow(workflow, e);
            }
            moveToPrearchive(target, user);
            directArchiveSessionHibernateService.setStatusToError(id, e);
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

    private void moveToPrearchive(SessionData target, UserI user) {
        try {
            File sessionDir = PrearcUtils.getPrearcSessionDir(user, target.getProject(), target.getTimestamp(),
                    target.getFolderName(), true);

            // ensure target location is empty, increment timestamp if needed
            if (Files.exists(sessionDir.toPath())) {
                target.setTimestamp(target.getTimestamp() + "_DA");
                sessionDir = PrearcUtils.getPrearcSessionDir(user, target.getProject(), target.getTimestamp(),
                        target.getFolderName(), true);
            }

            // move files
            FileUtils.moveDirectory(new File(target.getUrl()), sessionDir);

            // create db entry
            target.setUrl(sessionDir.getAbsolutePath());
            PrearcDatabase.eitherGetOrCreateSession(target, sessionDir.getParentFile(), PrearchiveCode.Manual);
        } catch (Exception e) {
            log.error("Unable to move {} to prearchive", target, e);
        }
    }

    private final JmsTemplate jmsTemplate;
    private final XnatUserProvider receivedFileUserProvider;
    private final DirectArchiveSessionHibernateService directArchiveSessionHibernateService;
}