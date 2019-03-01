package org.nrg.xnat.eventservice.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.model.ActionAttributeConfiguration;
import org.nrg.xnat.eventservice.model.Subscription;
import org.nrg.xnat.eventservice.model.xnat.XnatModelObject;
import org.nrg.xnat.eventservice.services.EventServiceComponentManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class EventServiceLoggingAction extends SingleActionProvider {

    private String displayName = "Logging Action";
    private String description = "Simple action for EventService Event that logs event detection.";
    private Map<String, ActionAttributeConfiguration> attributes;
    private Boolean enabled = true;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    @Lazy
    EventServiceComponentManager componentManager;

    public EventServiceLoggingAction() {
    }


    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getDescription() { return description; }

    @Override
    public Map<String, ActionAttributeConfiguration> getAttributes(String projectId, UserI user) {
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
        log.info("EventServiceLoggingAction called for RegKey " + subscription.listenerRegistrationKey());
        try {
            log.info("Subscription: " + mapper.writeValueAsString(subscription));
            log.info("Event: " + event.toString());
            XnatModelObject modelObject = componentManager.getModelObject(event.getObject(), user);
            if (modelObject != null){
                log.info("Event Payload:");
                log.info(mapper.writeValueAsString(modelObject));
            }
        } catch (JsonProcessingException e) {
            log.error("Could not write subscription values to log. ", e.getMessage());
        }

    }

}
