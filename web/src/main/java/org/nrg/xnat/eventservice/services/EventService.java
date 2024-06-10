package org.nrg.xnat.eventservice.services;


import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.exceptions.SubscriptionAccessException;
import org.nrg.xnat.eventservice.exceptions.SubscriptionValidationException;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.nrg.xnat.eventservice.model.Action;
import org.nrg.xnat.eventservice.model.ActionProvider;
import org.nrg.xnat.eventservice.model.EventPropertyNode;
import org.nrg.xnat.eventservice.model.EventServicePrefs;
import org.nrg.xnat.eventservice.model.JsonPathFilterNode;
import org.nrg.xnat.eventservice.model.SimpleEvent;
import org.nrg.xnat.eventservice.model.Subscription;
import org.nrg.xnat.eventservice.model.SubscriptionDelivery;
import org.nrg.xnat.eventservice.model.SubscriptionDeliverySummary;
import org.springframework.scheduling.annotation.EnableScheduling;
import reactor.bus.Event;

import java.util.List;
import java.util.Map;

@EnableScheduling
public interface EventService {
    List<SimpleEvent> getEvents() throws Exception;
    List<SimpleEvent> getEvents(Boolean loadDetails) throws Exception;
    //SimpleEvent getEvent(UUID uuid, Boolean loadDetails) throws Exception;
    SimpleEvent getEvent(String eventId, Boolean loadDetails) throws Exception;


    List<ActionProvider> getActionProviders();
    List<ActionProvider> getActionProviders(String xnatType, String projectId);

    List<Action> getAllActions();

    List<Action> getActions(List<String> xnatTypes, UserI user);
    List<Action> getActions(String projectId, List<String> xnatTypes, UserI user);

    List<Action> getActionsByEvent(String eventId, String projectId, UserI user);
    List<Action> getActionsByProvider(String actionProvider, UserI user);
    Action getActionByKey(String actionKey, UserI user);

    Map<String, JsonPathFilterNode> getEventFilterNodes(String eventId);
    List<EventPropertyNode> getEventPropertyNodes(String eventId);

    List<Subscription> getSubscriptions() throws SubscriptionAccessException;
    Subscription getSubscription(Long id) throws NotFoundException, SubscriptionAccessException;
    List<Subscription> getSubscriptions(String projectId) throws SubscriptionAccessException;
    Subscription validateSubscription(Subscription subscription) throws SubscriptionValidationException;
    Subscription createSubscription(Subscription subscription) throws SubscriptionValidationException, SubscriptionAccessException;
    Subscription createSubscription(Subscription subscription, Boolean overpopulateAttributes) throws SubscriptionValidationException, SubscriptionAccessException;
    Subscription updateSubscription(Subscription subscription) throws SubscriptionValidationException, NotFoundException, SubscriptionAccessException;
    void deleteSubscription(Long id) throws Exception;

    void reactivateAllSubscriptions();

    void triggerEvent(EventServiceEvent event);


    void processEvent(EventServiceListener listener, Event event);
    List<String> performJsonFilter(Subscription subscription, String jsonItem);

    Subscription activateSubscription(long id) throws NotFoundException;
    Subscription deactivateSubscription(long id) throws NotFoundException;

    Integer getSubscriptionDeliveriesCount(String projectId, Long subscriptionId, Boolean includeFilterMismatches);

    List<SubscriptionDeliverySummary> getSubscriptionDeliverySummary(String projectId);

    SubscriptionDelivery getSubscriptionDelivery(Long id, String projectId) throws NotFoundException;
    List<SubscriptionDelivery> getSubscriptionDeliveries(String projectId, Long subscriptionId, Boolean includeFilterMismatches, Boolean loadChildren);
    List<SubscriptionDelivery> getSubscriptionDeliveries(String projectId, Long subscriptionId, Boolean includeFilterMismatches, SubscriptionDeliveryEntityPaginatedRequest request, Boolean loadChildren);
    void deleteSubscriptionDeliveryPayloads(Integer keepRecentCount);

    String generateFilterRegEx(Map<String, JsonPathFilterNode> nodeFilters);
    void validateFilterJsonPathPredicate(String jsonPathPredicate);

    EventServiceComponentManager getComponentManager();

    EventServicePrefsBean getPrefs();
    EventServicePrefs getPrefsPojo();
    void updatePrefs(EventServicePrefs prefs);

    void syncReactorRegistrations();

}
