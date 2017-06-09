package org.nrg.xnat.eventservice.actions;

import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.entities.SubscriptionEntity;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.services.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TestAction extends SingleActionProvider {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private String displayName = "Test Action";
    private String description = "Test action for EventService Event";
    private List<String> events;
    private Map<String, String> attributes;
    private Boolean enabled = true;

    public List<EventServiceEvent> getDetectedEvents() {
        return detectedEvents;
    }

    List<EventServiceEvent> detectedEvents = new ArrayList();

    public TestAction() {
    }


    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getDescription() { return description; }

    @Override
    public List<String> getAttributeKeys() { return null; }

    @Override
    public void processEvent(EventServiceEvent event, SubscriptionEntity subscription, UserI user) {
        if (event instanceof EventServiceEvent) {
            detectedEvents.add(event);
            log.error("Test Action Firing for: " + event.getId());

        }
        synchronized (this) {
            notifyAll();

        }

    }

}
