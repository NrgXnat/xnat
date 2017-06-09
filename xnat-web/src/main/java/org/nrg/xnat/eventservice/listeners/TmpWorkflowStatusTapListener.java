package org.nrg.xnat.eventservice.listeners;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.event.entities.WorkflowStatusEvent;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.events.ProjectCreatedEvent;
import org.nrg.xnat.eventservice.events.ScanArchiveEvent;
import org.nrg.xnat.eventservice.events.SessionArchiveEvent;
import org.nrg.xnat.eventservice.events.SubjectCreatedEvent;
import org.nrg.xnat.eventservice.services.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import static reactor.bus.selector.Selectors.type;

@Service
@SuppressWarnings("unused")
public class TmpWorkflowStatusTapListener implements Consumer<Event<WorkflowStatusEvent>> {
    private static final Logger log = LoggerFactory.getLogger(TmpWorkflowStatusTapListener.class);

    private final EventService eventService;

    @Autowired
    public TmpWorkflowStatusTapListener(final EventBus eventBus, final EventService eventService) {
        eventBus.on(type(WorkflowStatusEvent.class), this);
        this.eventService = eventService;
    }

    //*
    // Translate workflow status events into Event Service events for workflow events containing appropriate labels
    //*
    @Override
    public void accept(Event<WorkflowStatusEvent> event) {
        final WorkflowStatusEvent wfsEvent = event.getData();

        if (StringUtils.equals(wfsEvent.getEventId(), "Transferred") && wfsEvent.getEntityType().contains("SessionData")) {
            try {
                final UserI user = Users.getUser(wfsEvent.getUserId());
                final XnatImagesessiondata session = XnatImagesessiondata.getXnatImagesessiondatasById(wfsEvent.getEntityId(), user, true);
                String projectId = session.getProject();
                // Trigger Session Archived Lifecycle event from here until we figure out where to launch the event.
                eventService.triggerEvent(new SessionArchiveEvent(session, user.getID()), projectId);
                // Firing ScanArchiveEvent for each contained scan
                for (final XnatImagescandataI scan : session.getScans_scan()) {
                    eventService.triggerEvent(new ScanArchiveEvent(scan, user.getID()), projectId);
                }


            } catch (UserNotFoundException e) {
                log.warn("The specified user was not found: {}", wfsEvent.getUserId());
            } catch (UserInitException e) {
                log.error("An error occurred trying to retrieve the user for a workflow event: " + wfsEvent.getUserId(), e);
            }
        } else if (StringUtils.equals(wfsEvent.getEventId(), "Created Resource") && wfsEvent.getEntityType().contains("ScanData")) {
            try {
                final UserI user = Users.getUser(wfsEvent.getUserId());
                final XnatImagesessiondata session = XnatImagesessiondata.getXnatImagesessiondatasById(wfsEvent.getEntityId(), user, true);
                // Trigger Scan Archived Lifecycle event from here until we figure out where to launch the event.
//                String filter = EventFilter.builder().addProjectId(session.getProject()).build().toRegexKey();
//                nrgEventService.triggerEvent(filter, new ScanArchiveEvent(scan, user.getID()), false);
//                log.debug("Firing ScanArchiveEvent for EventLabel: " + filter);


            } catch (UserNotFoundException e) {
                log.warn("The specified user was not found: {}", wfsEvent.getUserId());
            } catch (UserInitException e) {
                log.error("An error occurred trying to retrieve the user for a workflow event: " + wfsEvent.getUserId(), e);
            }
        } else if (StringUtils.equals(wfsEvent.getEventId(), "Added Project") && wfsEvent.getEntityType().contains("projectData")) {
            try {
                final UserI user = Users.getUser(wfsEvent.getUserId());
                final String projectId = wfsEvent.getEntityId();
                final XnatProjectdata projectData = XnatProjectdata.getProjectByIDorAlias(projectId, user, false);

                // Trigger Session Archived Lifecycle event from here until we figure out where to launch the event.
                eventService.triggerEvent(new ProjectCreatedEvent(projectData, user.getID()), projectId);
            } catch (UserNotFoundException e) {
                log.warn("The specified user was not found: {}", wfsEvent.getUserId());
            } catch (UserInitException e) {
                log.error("An error occurred trying to retrieve the user for a workflow event: " + wfsEvent.getUserId(), e);
            }

        } else if (StringUtils.equals(wfsEvent.getEventId(), "Added Subject") && wfsEvent.getEntityType().contains("SubjectData")) {
            try {
                final UserI user = Users.getUser(wfsEvent.getUserId());
                final String subjectId = wfsEvent.getEntityId();
                final XnatSubjectdata subjectdata = XnatSubjectdata.getXnatSubjectdatasById(subjectId, user, false);
                String projectId = subjectdata.getFirstProject().getId();

                // Trigger Session Archived Lifecycle event from here until we figure out where to launch the event.
                eventService.triggerEvent(new SubjectCreatedEvent(subjectdata, user.getID()), projectId);

            } catch (UserNotFoundException e) {
                log.warn("The specified user was not found: {}", wfsEvent.getUserId());
            } catch (UserInitException e) {
                log.error("An error occurred trying to retrieve the user for a workflow event: " + wfsEvent.getUserId(), e);
            }

        }
    }
}