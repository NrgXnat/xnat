package org.nrg.xnat.eventservice.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import org.h2.util.StringUtils;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.entities.SubscriptionEntity;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.exceptions.SubscriptionValidationException;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.nrg.xnat.eventservice.model.*;
import org.nrg.xnat.eventservice.model.xnat.XnatModelObject;
import org.nrg.xnat.eventservice.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.*;

@Service
public class EventServiceImpl implements EventService {
    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private ContextService contextService;
    private EventSubscriptionEntityService subscriptionService;
    private EventBus eventBus;
    private EventServiceComponentManager componentManager;
    private ActionManager actionManager;
    private SubscriptionDeliveryEntityService subscriptionDeliveryEntityService;
    private UserManagementServiceI userManagementService;
    private ObjectMapper mapper;

    @Autowired
    public EventServiceImpl(ContextService contextService,
                            EventSubscriptionEntityService subscriptionService, EventBus eventBus,
                            EventServiceComponentManager componentManager,
                            ActionManager actionManager,
                            SubscriptionDeliveryEntityService subscriptionDeliveryEntityService,
                            UserManagementServiceI userManagementService,
                            ObjectMapper mapper) {
        this.contextService = contextService;
        this.subscriptionService = subscriptionService;
        this.eventBus = eventBus;
        this.componentManager = componentManager;
        this.actionManager = actionManager;
        this.subscriptionDeliveryEntityService = subscriptionDeliveryEntityService;
        this.userManagementService = userManagementService;
        this.mapper = mapper;
    }

    @Override
    public Subscription createSubscription(Subscription subscription) throws SubscriptionValidationException {

        return subscriptionService.createSubscription(subscription);
    }

    @Override
    public Subscription updateSubscription(Subscription subscription) throws SubscriptionValidationException, NotFoundException {
        return subscriptionService.update(subscription);
    }

    @Override
    public void deleteSubscription(Long id) throws Exception {
        subscriptionService.delete(id);
    }

    @Override
    public void throwExceptionIfNameExists(Subscription subscription) throws NrgServiceRuntimeException {
        subscriptionService.throwExceptionIfNameExists(subscription);
    }

    @Override
    public List<Subscription> getSubscriptions() {
        return subscriptionService.getAllSubscriptions();
    }

    @Override
    public List<Subscription> getSubscriptions(String projectId) {
        return subscriptionService.getSubscriptions(projectId);
    }

    @Override
    public Subscription getSubscription(Long id) throws NotFoundException {
        return subscriptionService.getSubscription(id);
    }


    @Override
    public Subscription validateSubscription(Subscription subscription) throws SubscriptionValidationException {
        return subscriptionService.validate(subscription);
    }

    @Override
    public List<ActionProvider> getActionProviders() {
        List<ActionProvider> providers = new ArrayList<>();
        for(EventServiceActionProvider ap : componentManager.getActionProviders()) {
            providers.add(toPojo(ap));
        }
        return providers;
    }

    @Override
    public List<ActionProvider> getActionProviders(String xsiType, String projectId) {
        List<ActionProvider> providers = new ArrayList<>();
        for(EventServiceActionProvider ap : componentManager.getActionProviders()) {
            providers.add(toPojo(ap));
        }
        return providers;
    }


    @Override
    public List<Action> getAllActions() {
        return actionManager.getAllActions();
    }

    @Override
    public List<Action> getActions(String xnatType, UserI user) {
        return actionManager.getActions(xnatType, user);
    }

    @Override
    public List<Action> getActions(String projectId, String xnatType, UserI user) {
        return actionManager.getActions(projectId, xnatType, user);
    }

    @Override
    public List<Action> getActionsByEvent(String eventId, String projectId, UserI user) {
        List<Action> actions = new ArrayList<>();
        EventServiceEvent event = componentManager.getEvent(eventId);
        if(event != null && !StringUtils.isNullOrEmpty(event.getPayloadXnatType())){
            if(StringUtils.isNullOrEmpty(projectId)){
                actions = getActions(event.getPayloadXnatType(), user);
            } else {
                actions = getActions(projectId, event.getPayloadXnatType(), user);
            }
        }
        return actions;
    }

    @Override
    public List<Action> getActionsByProvider(String actionProvider, UserI user) {
        return actionManager.getActionsByProvider(actionProvider, user);
    }

    @Override
    public Action getActionByKey(String actionKey, UserI user) {
        return actionManager.getActionByKey(actionKey, user);
    }

    @Override
    public List<SimpleEvent> getEvents() throws Exception {
        List<SimpleEvent> events = new ArrayList();
        for(EventServiceEvent e : componentManager.getInstalledEvents()){
            events.add(toPojo(e));
        }
        return events;
    }

    @Override
    public SimpleEvent getEvent(UUID uuid) throws Exception {
        for(EventServiceEvent e :componentManager.getInstalledEvents()){
            if(e.getEventUUID().equals(uuid)){
                return toPojo(e);
            }
        }
        return null;
    }

    @Override
    @Deprecated
    public List<Listener> getInstalledListeners() {

        return null;
    }

    @Override
    public void reactivateAllSubscriptions() {

        List<Subscription> failedReactivations = new ArrayList<>();
        for (Subscription subscription:subscriptionService.getAllSubscriptions()) {
            if(subscription.active()) {
                log.debug("Reactivating of subscription: " + Long.toString(subscription.id()));
                try {
                    Subscription active = subscriptionService.activate(subscription);
                    if(active == null || !active.active()){
                        failedReactivations.add(subscription);
                    }

                } catch (Exception e) {
                    log.error("Failed to reactivate and update subscription: " + Long.toString(subscription.id()));
                    log.error(e.getMessage());
                }
            }
        }
        if(!failedReactivations.isEmpty()){
            log.error("Failed to re-activate %i event subscriptions.", failedReactivations.size());
            for (Subscription fs:failedReactivations) {
                log.error("Subscription activation: <" + fs.toString() + "> failed.");
            }
        }
    }

    @Override
    public void triggerEvent(EventServiceEvent event) {
        // TODO: Extract project id from event payload
        triggerEvent(event, null);
    }

    @Override
    public void triggerEvent(EventServiceEvent event, String projectId) {
        // Manually build event label
        EventFilter filter = EventFilter.builder().build();
        String regexKey = filter.toRegexKey(event.getClass().getName(), projectId);
        eventBus.notify(regexKey, Event.wrap(event));
        log.debug("Fired EventService Event for Label: " + regexKey);
    }


    @Override
    public void processEvent(EventServiceListener listener, Event event) {
        try {
            log.debug("Event noticed by EventService: " + event.getData().getClass().getSimpleName());
            String jsonObject = null;
            XnatModelObject modelObject = null;
            if(event.getData() instanceof EventServiceEvent) {
                EventServiceEvent esEvent = (EventServiceEvent) event.getData();
                for (Subscription subscription : subscriptionService.getSubscriptionsByKey(listener.getInstanceId().toString())) {
                    log.debug("RegKey matched for " + subscription.listenerRegistrationKey() + "  " + subscription.name());
                    // Create subscription delivery entry
                    Long deliveryId = subscriptionDeliveryEntityService.create(
                            SubscriptionEntity.fromPojoWithTemplate(subscription, subscriptionService.retrieve(subscription.id())),
                            esEvent,
                            listener,
                            subscription.actAsEventUser() ? esEvent.getUser() : subscription.subscriptionOwner(),
                            subscription.projectId() == null ? "" : subscription.projectId(),
                            subscription.attributes() == null ? "" : subscription.attributes().toString());
                    try {
                        subscriptionDeliveryEntityService.addStatus(deliveryId, SUBSCRIPTION_TRIGGERED, new Date(), "Subscription Service process started.");
                        // Is subscription enabled
                        if (!subscription.active()) {
                            subscriptionDeliveryEntityService.addStatus(deliveryId, SUBSCRIPTION_DISABLED_HALT, new Date(), "Inactive subscription skipped.");
                            log.debug("Inactive subscription: " + subscription.name() != null ? subscription.name() : "" + " skipped.");
                            return;
                        }
                        log.debug("Resolving action user (subscription owner or event user).");
                        UserI actionUser = subscription.actAsEventUser() ?
                                userManagementService.getUser(esEvent.getUser()) :
                                userManagementService.getUser(subscription.subscriptionOwner());
                        log.debug("Action User: " + actionUser.getUsername());

                        // ** Serialized event object ** //
                        try {
                            modelObject = componentManager.getModelObject(esEvent.getObject(), actionUser);
                            if (modelObject != null && mapper.canSerialize(modelObject.getClass())) {
                                // Serialize data object
                                log.debug("Serializing event object as known Model Object.");
                                jsonObject = mapper.writeValueAsString(modelObject);
                            } else if (esEvent.getObject() != null && mapper.canSerialize(esEvent.getObject().getClass())) {
                                log.debug("Serializing event object as unknown object type.");
                                jsonObject = mapper.writeValueAsString(esEvent.getObject());
                            } else {
                                log.debug("Could not serialize event object in: " + esEvent.toString());
                            }
                            if(!Strings.isNullOrEmpty(jsonObject)) {
                                String objectSubString = org.apache.commons.lang.StringUtils.substring(jsonObject, 0, 60);
                                log.debug("Serialized Object: " + objectSubString + "...");
                                subscriptionDeliveryEntityService.addStatus(deliveryId, OBJECT_SERIALIZED, new Date(), "Object Serialized: " + objectSubString + "...");
                            }
                        }catch(NullPointerException | JsonProcessingException e){
                            log.error("Aborting Event Service object serialization. Exception serializing event object: " + esEvent.getObjectClass());
                            log.error(e.getMessage());
                            subscriptionDeliveryEntityService.addStatus(deliveryId, OBJECT_SERIALIZATION_FAULT, new Date(), e.getMessage());
                        }

                        //Filter on data object (if filter and object exist)
                        if( subscription.eventFilter() != null && subscription.eventFilter().jsonPathFilter() != null ) {
                            // ** Attempt to filter event if serialization was successful ** //
                            if(Strings.isNullOrEmpty(jsonObject)){
                                log.debug("Aborting event pipeline - Event: {} has no object that can be serialized and filtered.", esEvent.getId());
                                subscriptionDeliveryEntityService.addStatus(deliveryId, OBJECT_FILTER_MISMATCH_HALT, new Date(), "Event has no object that can be serialized and filtered.");
                                return;
                            } else {
                                String jsonFilter = subscription.eventFilter().jsonPathFilter();
                                Configuration conf = Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST);
                                List<String> filterResult = JsonPath.using(conf).parse(jsonObject).read(jsonFilter);
                                String objectSubString = org.apache.commons.lang.StringUtils.substring(jsonObject, 0, 60);
                                if (filterResult.isEmpty()) {
                                    log.debug("Aborting event pipeline - Serialized event:\n" + objectSubString + "..." + "\ndidn't match JSONPath Filter:\n" + jsonFilter);
                                    subscriptionDeliveryEntityService.addStatus(deliveryId, OBJECT_FILTER_MISMATCH_HALT, new Date(), "Event objected failed filter test.");
                                    return;
                                } else {
                                    log.debug("JSONPath Filter Match - Serialized event:\n" + objectSubString + "..." + "\nJSONPath Filter:\n" + jsonFilter);
                                    subscriptionDeliveryEntityService.addStatus(deliveryId, OBJECT_FILTERED, new Date(), "Event objected passed filter test.");
                                }
                            }
                        }
                        try {
                            // ** Extract triggering event details and save to delivery entity ** //
                            String xsiUri = null;
                            String objectLabel = null;
                            if (modelObject != null) {
                                xsiUri = modelObject.getUri();
                                objectLabel = modelObject.getLabel();
                            } else if (esEvent.getObject() != null) {
                                Object object = esEvent.getObject();
                                // TODO: Handle other object types
                            }
                            subscriptionDeliveryEntityService.setTriggeringEvent(
                                    deliveryId, esEvent.getDisplayName(), esEvent.isPayloadXsiType(), esEvent.getPayloadXnatType(), xsiUri, objectLabel);
                        } catch (Throwable e){
                            log.error("Could not build TriggeringEventEntity ", e.getMessage(), e);
                        }
                        // call Action Manager with payload
                        SubscriptionEntity subscriptionEntity = subscriptionService.get(subscription.id());

                        actionManager.processEvent(subscriptionEntity, esEvent, actionUser, deliveryId);
                    } catch (UserNotFoundException |UserInitException e) {
                        log.error("Failed to process subscription:" + subscription.name());
                        log.error(e.getMessage());
                        e.printStackTrace();
                        if(deliveryId != null){
                            subscriptionDeliveryEntityService.addStatus(deliveryId, FAILED, new Date(), e.getMessage());
                        }
                    }
                }
            }
        } catch (NotFoundException e) {
            log.error("Failed to processEvent with subscription service.\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Subscription activateSubscription(long id) throws NotFoundException {
        Subscription subscription = subscriptionService.getSubscription(id);
        return subscriptionService.activate(subscription);
    }


    @Override
    public Subscription deactivateSubscription(long id) throws NotFoundException {
        Subscription subscription = subscriptionService.getSubscription(id);
        return subscriptionService.deactivate(subscription);
    }

    @Override
    public List<SubscriptionDelivery> getSubscriptionDeliveries(String projectId, Long subscriptionId) {
        return subscriptionDeliveryEntityService.get(projectId, subscriptionId);
    }

    private SimpleEvent toPojo(@Nonnull EventServiceEvent event) {
        return SimpleEvent.builder()
                .id(event.getId() == null ? "" : event.getId())
                .listenerService(
                        event instanceof EventServiceListener
                        ? ((EventServiceListener) event).getClass().getName()
                        : "")
                .displayName(event.getDisplayName() == null ? "" : event.getDisplayName())
                .description(event.getDescription() == null ? "" : event.getDescription())
                .payloadClass(event.getObjectClass() == null ? "" : event.getObjectClass())
                .xnatType(event.getPayloadXnatType() == null ? "" : event.getPayloadXnatType())
                .isXsiType(event.isPayloadXsiType() == null ? false : event.isPayloadXsiType())
                .build();
    }

    private ActionProvider toPojo(@Nonnull EventServiceActionProvider actionProvider) {
        return ActionProvider.builder()
                .className(actionProvider.getName())
                .displayName((actionProvider.getDisplayName()))
                .description(actionProvider.getDescription())
                .actions(actionProvider.getActions(null))
                .build();
    }


}
