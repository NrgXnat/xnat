package org.nrg.xnat.eventservice.services;

import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.entities.SubscriptionEntity;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.model.Action;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public interface EventServiceActionProvider {

    String getName();
    String getDisplayName();
    String getDescription();

    // Return all actions available to users on the system
    List<Action> getAllActions();

    // Return a list of org.nrg.xnat.eventservice.model.Action objects to describe actions available to the user //
    List<Action> getActions(UserI user);
    List<Action> getActions(String xnatType, UserI user);
    List<Action> getActions(String projectId, String xnatType, UserI user);

    Boolean isActionAvailable(String actionKey, String projectId, String xnatType, UserI user);

    // actionKey uniquely identifies an action across the system
    // format: <actionId:providerId>
    String actionKeyToActionId(String actionKey);
    // actionId uniquely identifies an action with a provider
    String actionIdToActionKey(String actionId);


    void processEvent(final EventServiceEvent event, SubscriptionEntity subscription, final UserI user);
}
