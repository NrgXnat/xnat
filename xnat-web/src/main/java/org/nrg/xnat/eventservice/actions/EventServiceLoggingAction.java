package org.nrg.xnat.eventservice.actions;

import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.entities.SubscriptionEntity;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.model.ActionAttributeConfiguration;
import org.nrg.xnat.eventservice.services.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EventServiceLoggingAction extends SingleActionProvider {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private String displayName = "Sample Action";
    private String description = "Sample action for EventService Event logs event.";
    private Map<String, ActionAttributeConfiguration> attributes;
    private Boolean enabled = true;

    public EventServiceLoggingAction() {
    }


    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getDescription() { return description; }

    @Override
    public Map<String, ActionAttributeConfiguration> getAttributes() {
        Map<String, ActionAttributeConfiguration> attributeConfigurationMap = new HashMap<>();
        attributeConfigurationMap.put("param1",
                ActionAttributeConfiguration.builder()
                                            .description("Sample description of attribute.")
                                            .type("string")
                                            .defaultValue("default-value")
                                            .userSettable(true)
                                            .required(false)
                                            .build());

        attributeConfigurationMap.put("param2",
                ActionAttributeConfiguration.builder()
                                            .description("Another description of attribute.")
                                            .type("string")
                                            .defaultValue("default-value")
                                            .userSettable(true)
                                            .required(false)
                                            .build());
        return attributeConfigurationMap;
    }

    @Override
    public void processEvent(EventServiceEvent event, SubscriptionEntity subscription, UserI user, final Long deliveryId) {
        log.error("EventServiceLoggingAction called for RegKey " + subscription.getListenerRegistrationKey());

    }

}
