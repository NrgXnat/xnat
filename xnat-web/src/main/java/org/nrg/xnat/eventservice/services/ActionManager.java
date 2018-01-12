package org.nrg.xnat.eventservice.services;

import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.entities.SubscriptionEntity;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.model.Action;

import java.util.List;

public interface ActionManager {

    Action getActionByKey(String actionKey, UserI user);

    List<Action> getActions(UserI user);
    List<Action> getActions(String xnatType, UserI user);
    List<Action> getActions(String projectId, String xnatType, UserI user);

    List<Action> getActionsByProvider(String providerName, UserI user);
    List<Action> getActionsByProvider(EventServiceActionProvider provider, UserI user);
    List<Action> getActionsByObject(String operation);

    List<EventServiceActionProvider> getActionProviders();
    EventServiceActionProvider getActionProvider(String providerName);

    boolean validateAction(Action action, String projectId, String xnatType, UserI user);
    boolean validateAction(Action action, List<String> projectIds, String xnatType, UserI user);

    PersistentWorkflowI generateWorkflowEntryIfAppropriate(SubscriptionEntity subscription, EventServiceEvent esEvent, UserI user);
    void processEvent(SubscriptionEntity subscription, EventServiceEvent esEvent, UserI user);
}
