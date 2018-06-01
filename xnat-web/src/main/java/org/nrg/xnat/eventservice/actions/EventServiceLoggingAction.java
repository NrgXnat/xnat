package org.nrg.xnat.eventservice.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.model.ActionAttributeConfiguration;
import org.nrg.xnat.eventservice.model.Subscription;
import org.nrg.xnat.eventservice.services.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ObjectMapper mapper;

    public EventServiceLoggingAction() {
    }


    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getDescription() { return description; }

    @Override
    public Map<String, ActionAttributeConfiguration> getAttributes(String projectId, String xnatType, UserI user) {
        Map<String, ActionAttributeConfiguration> attributeConfigurationMap = new HashMap<>();
        attributeConfigurationMap.put("param1",
                ActionAttributeConfiguration.builder()
                                            .description("Sample description of attribute.")
                                            .type("string")
                                            .defaultValue("default-value")
                                            .required(false)
                                            .build());

        attributeConfigurationMap.put("param2",
                ActionAttributeConfiguration.builder()
                                            .description("Another description of attribute.")
                                            .type("string")
                                            .defaultValue("default-value")
                                            .required(false)
                                            .build());
        return attributeConfigurationMap;
    }

    @Override
    public void processEvent(EventServiceEvent event, Subscription subscription, UserI user, final Long deliveryId) {
        log.debug("EventServiceLoggingAction called for RegKey " + subscription.listenerRegistrationKey());
        try {
            log.debug(mapper.writeValueAsString(subscription));
        } catch (JsonProcessingException e) {
            log.error("Could not write subscription values to log. ", e.getMessage());
        }

    }

}
