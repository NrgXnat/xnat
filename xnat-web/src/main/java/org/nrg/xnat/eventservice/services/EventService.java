package org.nrg.xnat.eventservice.services;


import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.exceptions.SubscriptionValidationException;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.nrg.xnat.eventservice.model.Action;
import org.nrg.xnat.eventservice.model.ActionProvider;
import org.nrg.xnat.eventservice.model.EventPropertyNode;
import org.nrg.xnat.eventservice.model.JsonPathFilterNode;
import org.nrg.xnat.eventservice.model.Listener;
import org.nrg.xnat.eventservice.model.SimpleEvent;
import org.nrg.xnat.eventservice.model.Subscription;
import org.nrg.xnat.eventservice.model.SubscriptionDelivery;
import reactor.bus.Event;

import java.util.List;
import java.util.Map;

public interface EventService {
    
    List<SimpleEvent> getEvents() throws Exception;
    List<SimpleEvent> getEvents(Boolean loadDetails) throws Exception;
    //SimpleEvent getEvent(UUID uuid, Boolean loadDetails) throws Exception;
    SimpleEvent getEvent(String eventId, Boolean loadDetails) throws Exception;

    @Deprecated
    List<Listener> getInstalledListeners();


    List<ActionProvider> getActionProviders();
    List<ActionProvider> getActionProviders(String xnatType, String projectId);

    List<Action> getAllActions();

    List<Action> getActions(String xnatType, UserI user);
    List<Action> getActions(String projectId, String xnatType, UserI user);
    List<Action> getActionsByEvent(String eventId, String projectId, UserI user);
    List<Action> getActionsByProvider(String actionProvider, UserI user);
    Action getActionByKey(String actionKey, UserI user);

    Map<String, JsonPathFilterNode> getEventFilterNodes(String eventId);
    List<EventPropertyNode> getEventPropertyNodes(String eventId);

    List<Subscription> getSubscriptions();
    Subscription getSubscription(Long id) throws NotFoundException;
    List<Subscription> getSubscriptions(String projectId);
    Subscription validateSubscription(Subscription subscription) throws SubscriptionValidationException;
    Subscription createSubscription(Subscription subscription) throws SubscriptionValidationException;
    Subscription createSubscription(Subscription subscription, Boolean overpopulateAttributes) throws SubscriptionValidationException;
    Subscription updateSubscription(Subscription subscription) throws SubscriptionValidationException, NotFoundException;
    void deleteSubscription(Long id) throws Exception;
    void throwExceptionIfNameExists(Subscription subscription) throws NrgServiceRuntimeException;

    void reactivateAllSubscriptions();

    void triggerEvent(EventServiceEvent event);


    void processEvent(EventServiceListener listener, Event event);

    Subscription activateSubscription(long id) throws NotFoundException;
    Subscription deactivateSubscription(long id) throws NotFoundException;

    List<SubscriptionDelivery> getSubscriptionDeliveries(String projectId, Long subscriptionId, Boolean includeFilterMismatches);

    String generateFilterRegEx(Map<String, JsonPathFilterNode> nodeFilters);

    List<String> getRecentTriggers(Integer count);
}
