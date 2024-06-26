package org.nrg.xnat.eventservice.actions;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.model.ActionAttributeConfiguration;
import org.nrg.xnat.eventservice.model.Subscription;
import org.nrg.xnat.eventservice.services.SubscriptionDeliveryEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.ACTION_COMPLETE;

@Slf4j
@Service
public class TestAction extends SingleActionProvider {

    private String displayName = "Test Action";
    private String description = "Test action for EventService Event";
    private List<String> events;
    private Map<String, String> attributes;
    private Boolean enabled = true;
    private SubscriptionDeliveryEntityService subscriptionDeliveryEntityService;
    private String actionUser = "";

    public String getActionUser() {
        return actionUser;
    }

    public List<EventServiceEvent> getDetectedEvents() {
        return detectedEvents;
    }

    List<EventServiceEvent> detectedEvents = new ArrayList();

    @Autowired
    public TestAction(final SubscriptionDeliveryEntityService subscriptionDeliveryEntityService) {
        this.subscriptionDeliveryEntityService = subscriptionDeliveryEntityService;
    }


    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getDescription() { return description; }



    @Override
    public void processEvent(EventServiceEvent event, Subscription subscription, UserI user, final Long deliveryId) {
        if (event instanceof EventServiceEvent) {
            detectedEvents.add(event);
            actionUser = user.getLogin();
            subscriptionDeliveryEntityService.addStatus(deliveryId, ACTION_COMPLETE, new Date(), "TestAction Launched.");
            log.debug("Test Action Firing for: " + event.getType());

        }
        synchronized (this) {
            notifyAll();

        }

    }

    @Override
    public Map<String, ActionAttributeConfiguration> getAttributes(String projectId, UserI user) {
        return null;
    }
}
