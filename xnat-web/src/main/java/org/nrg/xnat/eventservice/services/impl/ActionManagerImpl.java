package org.nrg.xnat.eventservice.services.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.entities.SubscriptionEntity;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.model.Action;
import org.nrg.xnat.eventservice.services.*;
import org.nrg.xnat.utils.WorkflowUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.ACTION_CALLED;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.FAILED;

@Service
public class ActionManagerImpl implements ActionManager {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventServiceComponentManager componentManager;
    private final SubscriptionDeliveryEntityService subscriptionDeliveryEntityService;

    @Autowired
    public ActionManagerImpl(final EventServiceComponentManager componentManager, SubscriptionDeliveryEntityService subscriptionDeliveryEntityService) {
        this.componentManager = componentManager;
        this.subscriptionDeliveryEntityService = subscriptionDeliveryEntityService;
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
    public List<Action> getAllActions(){
        List<Action> actions = new ArrayList<>();
        for(EventServiceActionProvider provider:getActionProviders()) {
            List<Action> providerActions = provider.getAllActions();
            if(providerActions != null)
                actions.addAll(providerActions);
        }
        return actions;
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
    public boolean validateAction(String actionKey, String projectId, String xnatType, UserI user) {
        EventServiceActionProvider provider = getActionProviderByKey(actionKey);
        if(!provider.isActionAvailable(actionKey, projectId, xnatType, user)) {
            log.error("Action:{} validation failed for ProjectId:{}, XnatType:{}, User:{}", actionKey, projectId, xnatType, user.getLogin());
            return false;
        }
        return true;
    }

    @Override
    public boolean validateAction(String actionKey, List<String> projectIds, String xnatType, UserI user) {
        if(projectIds == null || projectIds.isEmpty()){
          return validateAction(actionKey, "", xnatType, user);
        } else {
            for (String projectId : projectIds) {
                if (!validateAction(actionKey, projectId, xnatType, user)) {
                    return false;
                }
            }
        }
        return true;
    }


    //PersistentWorkflowI workflow = WorkflowUtils.getOrCreateWorkflowData(someInt, user, xftItem, EventUtils.newEventInstance());
    //EventMetaI event = workflow.buildEvent();
    //try {
    //    WorkflowUtils.setStep(workflow, "1");
    //    WorkflowUtils.setStep(workflow, "2");
    //    WorkflowUtils.setStep(workflow, "3");
    //    WorkflowUtils.complete(workflow, event);
    //} catch (Exception e) {
    //    WorkflowUtils.fail(workflow, event);
    //}
    @Override
    public PersistentWorkflowI generateWorkflowEntryIfAppropriate(SubscriptionEntity subscription, EventServiceEvent esEvent, UserI user) {
        try {
            if(esEvent.getObject() instanceof BaseElement && ((BaseElement)esEvent.getObject()).getItem()instanceof XFTItem) {
                XFTItem eventXftItem = ((BaseElement)esEvent.getObject()).getItem();
                log.debug("Attempting to create workflow entry for " + esEvent.getObject().getClass().getSimpleName() + " in subscription" + subscription.getName() + ".");
                final PersistentWorkflowI workflow = WorkflowUtils.buildOpenWorkflow(user, eventXftItem,
                        EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.PROCESS,
                                subscription.getName(), "Event Service Action Called", subscription.getActionProvider()));
                WorkflowUtils.save(workflow, workflow.buildEvent());
                log.debug("Created workflow " + workflow.getId());
                return workflow;
            }
            else {
                log.debug("Skipping workflow entry creation. Not available for non-XFTItem event object in subscription " + subscription.getName() + ".");
            }
        }catch (Exception e){
            log.error("Failed to create workflow entry for " + esEvent.getId() + "\n" + e.getMessage());
        }
        return null;
    }

    @Override
    public EventServiceActionProvider getActionProviderByKey(String actionKey) {
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
    public void processEvent(SubscriptionEntity subscription, EventServiceEvent esEvent, final UserI user, final Long deliveryId) {
        log.debug("ActionManager.processEvent started on Thread: " + Thread.currentThread().getName());
        PersistentWorkflowI workflow = generateWorkflowEntryIfAppropriate(subscription, esEvent, user);
        EventServiceActionProvider provider = getActionProviderByKey(subscription.getActionKey());
        if(provider!= null) {
            log.debug("Passing event to Action Provider: " + provider.getName());
            subscriptionDeliveryEntityService.addStatus(deliveryId, ACTION_CALLED, new Date(), "Event passed to Action Provider: " + provider.getName());
            processAsync(provider, subscription, esEvent, user, deliveryId);
            if(workflow !=null){
                try {
                    WorkflowUtils.complete(workflow, workflow.buildEvent());
                    log.debug("Workflow {} complete.", workflow.getId());
                } catch (Exception e) {
                    log.error("Workflow completion exception for workflow:" + workflow.getId());
                    log.error(e.getMessage());
                }
            }
        } else {
            String errorMessage = "Could not find Action Provider for ActionKey: " + subscription.getActionKey();
            subscriptionDeliveryEntityService.addStatus(deliveryId, FAILED, new Date(), "Could not find Action Provider for ActionKey: " + subscription.getActionKey());
            workflow.setStatus(errorMessage);
            try {
                WorkflowUtils.fail(workflow, workflow.buildEvent());
            } catch (Exception e) {
                log.error("Workflow completion exception for workflow:" + workflow.getId());
                log.error(e.getMessage());
            }
            log.error(errorMessage);
        }
    }

    @Async
    @Override
    public void processAsync(EventServiceActionProvider provider, SubscriptionEntity subscription, EventServiceEvent esEvent, final UserI user, final Long deliveryId){
        log.debug("Started Async process on thread: {}", Thread.currentThread().getName());
        provider.processEvent(esEvent, subscription, user, deliveryId);
        log.debug("Ending Async process on thread: {}", Thread.currentThread().getName());
    }

}
