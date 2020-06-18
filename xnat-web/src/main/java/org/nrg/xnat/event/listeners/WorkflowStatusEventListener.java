package org.nrg.xnat.event.listeners;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.services.NrgEventService;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.event.entities.WorkflowStatusEvent;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xnat.event.model.BulkLaunchEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import static reactor.bus.selector.Selectors.type;

@Slf4j
@Component
public class WorkflowStatusEventListener implements Consumer<Event<WorkflowStatusEvent>> {
    private final NrgEventService eventService;

    @Autowired
    public WorkflowStatusEventListener(final NrgEventService eventService,
                                       final EventBus eventBus) {
        this.eventService = eventService;
        eventBus.on(type(WorkflowStatusEvent.class), this);
    }

    @Override
    public void accept(Event<WorkflowStatusEvent> busEvent) {
        final PersistentWorkflowI workflow = busEvent.getData().getWorkflow();
        final String bulkLaunchId = workflow.getJobid();
        if (StringUtils.isBlank(bulkLaunchId)) {
            return;
        }
        Integer userId;
        try {
            userId = Users.getUser(workflow.getUsername()).getID();
        } catch (UserInitException | UserNotFoundException | NullPointerException e) {
            userId = null;
        }
        if (userId == null) {
            // No user info, can't track this event
            return;
        }
        eventService.triggerEvent(new BulkLaunchEvent(bulkLaunchId, userId, workflow.getWorkflowId(),
                workflow.getId(), workflow.getStatus(), workflow.getDetails(), workflow.getComments()));
    }
}
