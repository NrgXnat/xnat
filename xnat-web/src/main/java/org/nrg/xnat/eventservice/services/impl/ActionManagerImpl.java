package org.nrg.xnat.eventservice.services.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.entities.SubscriptionEntity;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.model.Action;
import org.nrg.xnat.eventservice.services.ActionManager;
import org.nrg.xnat.eventservice.services.EventService;
import org.nrg.xnat.eventservice.services.EventServiceActionProvider;
import org.nrg.xnat.eventservice.services.EventServiceComponentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ActionManagerImpl implements ActionManager {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventServiceComponentManager componentManager;

    @Autowired
    public ActionManagerImpl(final EventServiceComponentManager componentManager) {
        this.componentManager = componentManager;
    }


    // ** actionKey should be formatted as "actionProvider:actionLabel" ** //
    @Override
    public Action getActionByKey(String actionKey, UserI user) {
        String actionProvider = null;
        String actionId = null;
        Iterable<String> key = Splitter.on(':')
                                         .trimResults()
                                         .omitEmptyStrings()
                                         .split(actionKey);
        ImmutableList<String> keyList = ImmutableList.copyOf(key);
        if(!keyList.isEmpty()) {
            actionProvider = actionId = keyList.get(0);
            if(keyList.size()>1){
                actionId = keyList.get(1);
            }
        }
        List<Action> actions = getActionsByProvider(actionProvider, user);
        for(Action action : actions) {
            if(action.actionKey().contentEquals(actionKey)) {
                return action;
            }
        }
        return null;
    }

    @Override
    public List<Action> getActions(UserI user) {
        List<Action> actions = new ArrayList<>();
        for(EventServiceActionProvider provider:getActionProviders()) {
            List<Action> providerActions = provider.getActions(user);
            if(providerActions != null)
                actions.addAll(providerActions);
        }
        return actions;
    }

    @Override
    public List<Action> getActions(String xnatType, UserI user) {
        List<Action> actions = new ArrayList<>();
        for(EventServiceActionProvider provider:getActionProviders()) {
            actions.addAll(provider.getActions(xnatType, user));
        }
        return actions;
    }

    @Override
    public List<Action> getActions(String projectId, String xnatType, UserI user) {
        List<Action> actions = new ArrayList<>();
        for(EventServiceActionProvider provider:getActionProviders()) {
            actions.addAll(provider.getActions(projectId, xnatType, user));
        }
        return actions;
    }

    @Override
    public List<Action> getActionsByProvider(String providerName, UserI user) {
        for(EventServiceActionProvider provider : componentManager.getActionProviders()){
            if(provider != null && provider.getName() != null && provider.getName().contentEquals(providerName)) {
                return provider.getActions(user);
            }
        }
        return new ArrayList<>();
    }

    @Override
    public List<Action> getActionsByProvider(EventServiceActionProvider provider, UserI user) {
        return provider.getActions(user);
    }

    @Override
    public List<Action> getActionsByObject(String operation) {
//        List<EventServiceAction> actions = null;
//        for(EventServiceAction action : getActionsByProvider()){
//            if(action.)
//        }
        return null;
    }

    @Override
    public List<EventServiceActionProvider> getActionProviders() {
        return componentManager.getActionProviders();
    }

    @Override
    public EventServiceActionProvider getActionProvider(String providerName) {
        List<EventServiceActionProvider> providers = getActionProviders();
        for(EventServiceActionProvider provider : providers){
            if(provider.getName().contentEquals(providerName)){
                return provider;
            }
        }
        return null;
    }

    @Override
    public boolean validateAction(Action action, UserI user) {
        List<Action> actions = getActionsByProvider(action.provider(), user);
        if(actions != null && actions.contains(action)) {
            return true;
        }
        return false;
    }

    private EventServiceActionProvider getActionProviderByKey(String actionKey) {
        String providerId;
        Iterable<String> key = Splitter.on(':')
                                       .trimResults()
                                       .omitEmptyStrings()
                                       .split(actionKey);
        ImmutableList<String> keyList = ImmutableList.copyOf(key);
        if(!keyList.isEmpty()) {
            providerId = keyList.get(0);
            return getActionProvider(providerId);
        }
        return null;
    }

    @Override
    public void processEvent(SubscriptionEntity subscription, EventServiceEvent esEvent, final UserI user) {
        EventServiceActionProvider provider = getActionProviderByKey(subscription.getActionKey());
        if(provider!= null) {
            log.debug("Passing event to Action Provider: " + provider.getName());
            provider.processEvent(esEvent, subscription, user);
        } else {
            log.error("Could not find Action Provider for ActionKey: " + subscription.getActionKey());
        }
    }

}
