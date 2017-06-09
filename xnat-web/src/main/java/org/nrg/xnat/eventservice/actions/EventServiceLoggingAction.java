package org.nrg.xnat.eventservice.actions;

import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.entities.SubscriptionEntity;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.services.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EventServiceLoggingAction extends SingleActionProvider {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private String displayName = "Logging Action";
    private String description = "Logging action for EventService Event";
    private Map<String, String> attributes;
    private Boolean enabled = true;

    public EventServiceLoggingAction() {
    }


    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getDescription() { return description; }


    @Override
    public List<String> getAttributeKeys() { return null; }

    @Override
    public void processEvent(EventServiceEvent event, SubscriptionEntity subscription, UserI user) {
        log.error("EventServiceLoggingAction called for RegKey " + subscription.getListenerRegistrationKey());

    }

}
