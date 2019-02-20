package org.nrg.xnat.eventservice.services;

import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.model.Action;
import org.nrg.xnat.eventservice.model.Subscription;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

public interface ActionManager {

    Action getActionByKey(String actionKey, UserI user);

    List<Action> getAllActions();

    @Deprecated
    List<Action> getActions(UserI user);

    List<Action> getActions(String xnatType, UserI user);
    List<Action> getActions(String projectId, String xnatType, UserI user);

    List<Action> getActionsByProvider(String providerName, UserI user);
    public EventServiceActionProvider getActionProviderByKey(String actionKey);
    List<Action> getActionsByProvider(EventServiceActionProvider provider, UserI user);
    List<Action> getActionsByObject(String operation);

    List<EventServiceActionProvider> getActionProviders();
    EventServiceActionProvider getActionProvider(String providerName);

    boolean validateAction(String actionKey, String projectId, UserI user);
    boolean validateAction(String actionKey, List<String> projectIds, UserI user);

    PersistentWorkflowI generateWorkflowEntryIfAppropriate(Subscription subscription, EventServiceEvent esEvent, UserI user);
    void processEvent(Subscription subscription, EventServiceEvent esEvent, UserI user, Long deliveryId);

    @Async
    void processAsync(EventServiceActionProvider provider, Subscription subscription, EventServiceEvent esEvent,
                      UserI user, Long deliveryId, PersistentWorkflowI workflow);
}
