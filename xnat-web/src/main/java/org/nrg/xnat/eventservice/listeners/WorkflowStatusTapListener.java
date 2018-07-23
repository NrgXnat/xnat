package org.nrg.xnat.eventservice.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.event.entities.WorkflowStatusEvent;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.events.WorkflowStatusChangeEvent;
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
public class WorkflowStatusTapListener implements Consumer<Event<WorkflowStatusEvent>> {
    private static final Logger log = LoggerFactory.getLogger(WorkflowStatusTapListener.class);

    private final EventService eventService;
    private final ObjectMapper mapper;

    @Autowired
    public WorkflowStatusTapListener(final EventBus eventBus, final EventService eventService, final ObjectMapper mapper) {
        eventBus.on(type(WorkflowStatusEvent.class), this);
        this.eventService = eventService;
        this.mapper = mapper;
    }

    //*
    // Translate workflow status events into Event Service events for workflow events containing appropriate labels
    //*
    @Override
    public void accept(Event<WorkflowStatusEvent> event) {
        WorkflowStatusEvent wfsEvent = event.getData();
        if (wfsEvent.getWorkflow() instanceof WrkWorkflowdata) {
            try {
                final String project = wfsEvent.getExternalId();
                final UserI user = Users.getUser(wfsEvent.getUserId());
                eventService.triggerEvent(new WorkflowStatusChangeEvent(wfsEvent, user.getLogin(), WorkflowStatusChangeEvent.Status.CHANGED, project));
            } catch (UserNotFoundException e) {
                log.warn("The specified user was not found: {}", wfsEvent.getUserId());
            } catch (UserInitException e) {
                log.error("An error occurred trying to retrieve the user for a workflow event: " + wfsEvent.getUserId(), e);
            } catch (Throwable e) {
                log.error("Exception thrown when trying to catch/trigger WorkFlowStatus event for Event Service.  " + e.getMessage());
            }
        }
    }
}