package org.nrg.xnat.eventservice.actions;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.model.ActionAttributeConfiguration;
import org.nrg.xnat.eventservice.model.Subscription;
import org.nrg.xnat.eventservice.services.EventServiceComponentManager;
import org.nrg.xnat.eventservice.services.SubscriptionDeliveryEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.ACTION_COMPLETE;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.ACTION_FAILED;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.ACTION_STEP;

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

    @Autowired
    SubscriptionDeliveryEntityService subscriptionDeliveryEntityService;


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
        log.info("EventServiceLoggingAction called for Subscription {}", subscription.name());
        try {
            Object payloadObject = event.getObject(user);
            Object serializableObject = componentManager.getModelObject(payloadObject, user);
            if(serializableObject == null && payloadObject != null && mapper.canDeserialize(mapper.getTypeFactory().constructType(event.getObjectClass()))){
                serializableObject = payloadObject;
            }

            if(serializableObject != null){
                subscriptionDeliveryEntityService.addPayload(deliveryId, serializableObject);
                subscriptionDeliveryEntityService.addStatus(deliveryId, ACTION_STEP, new Date(), "Filterable Event Payload Type: " + serializableObject.getClass().getSimpleName());
                if(log.isDebugEnabled()) {
                    log.debug("Subscription: {}", mapper.writeValueAsString(subscription));
                    log.debug("Event: {}", event.toString());
                    log.debug("Event Payload: {}", mapper.writeValueAsString(serializableObject));
                }
            }
            subscriptionDeliveryEntityService.addStatus(deliveryId, ACTION_COMPLETE, new Date(), "Logging action completed successfully.");
        } catch (Throwable e) {
            log.error("Could not write subscription values to log.", e);
            subscriptionDeliveryEntityService.addStatus(deliveryId, ACTION_FAILED, new Date(), "Could not write subscription values to log. " + e.getMessage());
        }

    }

}
