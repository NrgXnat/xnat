package org.nrg.xnat.eventservice.services;

import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.model.Action;
import org.nrg.xnat.eventservice.model.Subscription;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public interface EventServiceActionProvider {

    String getName();
    String getDisplayName();
    String getDescription();

    // Return all actions available to users on the system - attributes not required
    List<Action> getAllActions();

    // Return a list of org.nrg.xnat.eventservice.model.Action objects to describe actions available to the user //
    List<Action> getActions(String projectId, List<String> xnatTypes, UserI user);


    Boolean isActionAvailable(String actionKey, String projectId, UserI user);

    // actionKey uniquely identifies an action across the system
    // format: <actionId:providerId>
    String actionKeyToActionId(String actionKey);
    // actionId uniquely identifies an action with a provider
    String actionIdToActionKey(String actionId);


    void processEvent(final EventServiceEvent event, Subscription subscription, final UserI user, final Long deliveryId);
}
