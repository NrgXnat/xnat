package org.nrg.xnat.eventservice.services.impl;

import org.h2.util.StringUtils;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.services.ContextService;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.exceptions.SubscriptionValidationException;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.nrg.xnat.eventservice.model.*;
import org.nrg.xnat.eventservice.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EventServiceImpl implements EventService {
    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private ContextService contextService;
    private EventSubscriptionEntityService subscriptionService;
    private EventBus eventBus;
    private EventServiceComponentManager componentManager;
    private ActionManager actionManager;
    private SubscriptionDeliveryEntityService subscriptionDeliveryEntityService;

    @Autowired
    public EventServiceImpl(final EventSubscriptionEntityService subscriptionService,
                            final EventBus eventBus,
                            final ContextService contextService,
                            final EventServiceComponentManager componentManager,
                            final ActionManager actionManager,
                            @Lazy final SubscriptionDeliveryEntityService subscriptionDeliveryEntityService) {
        this.subscriptionService = subscriptionService;
        this.eventBus = eventBus;
        this.contextService = contextService;
        this.componentManager = componentManager;
        this.actionManager = actionManager;
        this.subscriptionDeliveryEntityService = subscriptionDeliveryEntityService;

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
    public List<Subscription> getSubscriptions(String projectId, UserI userI) {
        //TODO: Get subscriptions authorized by project scope and userId
        return null;
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
            subscriptionService.processEvent(listener, event);
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
