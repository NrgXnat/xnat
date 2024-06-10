package org.nrg.xnat.eventservice.services.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.entities.SubscriptionDeliveryEntity;
import org.nrg.xnat.eventservice.entities.TimedEventStatusEntity;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.model.Action;
import org.nrg.xnat.eventservice.model.Subscription;
import org.nrg.xnat.eventservice.services.ActionManager;
import org.nrg.xnat.eventservice.services.EventPropertyService;
import org.nrg.xnat.eventservice.services.EventServiceActionProvider;
import org.nrg.xnat.eventservice.services.EventServiceComponentManager;
import org.nrg.xnat.eventservice.services.SubscriptionDeliveryEntityService;
import org.nrg.xnat.utils.WorkflowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.ACTION_CALLED;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.ACTION_ERROR;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.ACTION_FAILED;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.FAILED;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.RESOLVING_ATTRIBUTES;

@Slf4j
@Service
@EnableAsync
public class ActionManagerImpl implements ActionManager {

    private final EventServiceComponentManager componentManager;
    private final SubscriptionDeliveryEntityService subscriptionDeliveryEntityService;
    private EventPropertyService eventPropertyService;

    @Autowired
    public ActionManagerImpl(final EventServiceComponentManager componentManager, final SubscriptionDeliveryEntityService subscriptionDeliveryEntityService, final EventPropertyService eventPropertyService) {
        this.componentManager = componentManager;
        this.subscriptionDeliveryEntityService = subscriptionDeliveryEntityService;
        this.eventPropertyService = eventPropertyService;
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
        List<Action> actions = getAllActions();
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
            Optional.ofNullable(provider.getAllActions()).ifPresent(actions::addAll);

        }
        return actions;
    }

    @Override
    @Deprecated
    public List<Action> getActions(UserI user) {
        return getActions(null, user);
    }

    @Override
    public List<Action> getActions(List<String> xnatTypes, UserI user){
        List<Action> actions = new ArrayList<>();
        for(EventServiceActionProvider provider:getActionProviders()) {
            Optional.ofNullable(provider.getActions(null, xnatTypes, user)).ifPresent(actions::addAll);
        }
        return actions;
    }

    @Override
    public List<Action> getActions(@Nonnull String projectId, List<String> xnatTypes, UserI user) {
        List<String> projectIds;
        projectId = projectId.replaceAll("\\s+", "");
        // If PID is a comma separated list, return the intersection of available actions
        if(projectId.contains(",")) {
            projectIds = Arrays.asList(projectId.split(","));
        } else {
            projectIds = Arrays.asList(projectId);
        }

        List<Action> actions = null;
        for(String pid : projectIds) {
            List<Action> providerActions = new ArrayList<>();
            getActionProviders().stream().forEach(provider ->
                    Optional.ofNullable(provider.getActions(pid, xnatTypes, user))
                            .ifPresent(providerActions::addAll));

            // If actions is already populated, find the intersection of list elements
            actions = actions == null ? providerActions :
                    actions.stream().filter(a -> providerActions.contains(a)).collect(Collectors.toList());
        }

        return actions;
    }

    @Override
    public List<Action> getActionsByProvider(String providerName, UserI user) {
        for(EventServiceActionProvider provider : componentManager.getActionProviders()){
            if(provider != null && provider.getName() != null && provider.getName().contentEquals(providerName)) {
                return provider.getActions(null, null, user);
            }
        }
        return new ArrayList<>();
    }

    @Override
    public List<Action> getActionsByProvider(EventServiceActionProvider provider, UserI user) {
        return provider.getActions(null, null, user);
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
    public boolean validateAction(String actionKey, String projectId, UserI user) {
        EventServiceActionProvider provider = getActionProviderByKey(actionKey);
        if(!provider.isActionAvailable(actionKey, projectId, user)) {
            log.error("Action:{} validation failed for ProjectId:{}, User:{}", actionKey, projectId, user.getLogin());
            return false;
        }
        return true;
    }

    @Override
    public boolean validateAction(String actionKey, List<String> projectIds, UserI user) {
        if(projectIds == null || projectIds.isEmpty()){
          return validateAction(actionKey, "", user);
        } else {
            for (String projectId : projectIds) {
                if (!validateAction(actionKey, projectId, user)) {
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
    public PersistentWorkflowI generateWorkflowEntryIfAppropriate(Subscription subscription, Long deliveryId, EventServiceEvent esEvent, UserI user) {
        try {
            Object eventObject = esEvent.getObject(user);

            if(eventObject instanceof BaseElement && ((BaseElement)eventObject).getItem() instanceof XFTItem) {
                XFTItem eventXftItem = getRootWorkflowObject(((BaseElement)eventObject).getItem(), user);
                String workflowActionLabel =
                        subscription.name().replaceAll("[^a-zA-Z0-9_ -]", "_");
                String workflowReasonLabel = "Event Service triggered.";
                String workflowComment = "";
                EventDetails eventDetails = EventUtils.newEventInstance(
                        EventUtils.CATEGORY.DATA,
                        EventUtils.TYPE.PROCESS,
                        workflowActionLabel,
                        workflowReasonLabel,
                        workflowComment
                );
                if (log.isDebugEnabled()) {
                    log.debug("Attempting to create workflow entry for " + esEvent.getObjectClass() + " in subscription" + subscription.name() + ".");
                }
                final PersistentWorkflowI workflow = WorkflowUtils.buildOpenWorkflow(user, eventXftItem, eventDetails);

                if(workflow != null) {
                    Boolean successfulSave = false;
                    AtomicInteger saveAttempts = new AtomicInteger(0);
                    Exception wrkflwException = new Exception();
                    // TODO: There must be a better way to create workflow entries with unique timestamps
                    while (!successfulSave && saveAttempts.incrementAndGet() < 100)
                    try {
                        workflow.setLaunchTime(Calendar.getInstance().getTime());
                        EventMetaI eventMetaI = workflow.buildEvent();
                        WorkflowUtils.save(workflow, eventMetaI);
                        successfulSave = true;
                        log.debug("Created workflow " + workflow.getId());
                    } catch (Exception e) {
                        wrkflwException = e;
                        log.debug("Event Service workflow save failed. Trying again.");
                    }
                    if (successfulSave) { return workflow; }
                    else { throw wrkflwException; }
                } else {
                    log.error("Unable to create PersistentWorkflow entry for ES Event: " + esEvent.getDisplayName());
                }

            }
            else {
                log.debug("Skipping workflow entry creation. Not available for non-XFTItem event object in subscription " + subscription.name() + ".");
            }
        }catch (Throwable e){
            log.error("Failed to create workflow entry for " + esEvent.getType() + "\n" + e.getMessage());
        }
        return null;
    }

    private XFTItem getRootWorkflowObject(XFTItem eventObject, UserI user){
        if(eventObject != null && (eventObject instanceof XnatImagescandataI)){
            // If the event object is a scan, the workflow will not show up anywhere.
            // If possible, we use its parent session as the root object instead.
            // Note that if we simply use  eventXftItem.getParent() as the parent session, xsiType is not retained
            if (eventObject.getParent() != null){
                try {
                     return XnatExperimentdata.getXnatExperimentdatasById(
                             ((XnatImagescandataI)eventObject).getImageSessionId(),
                             user,
                             false)
                                              .getItem();
                }catch (Throwable t){
                    // just ignore and use the original item.
                }
            }
        }
        return eventObject;
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
    public void processEvent(Subscription subscription, EventServiceEvent esEvent, final UserI user, final Long deliveryId) {
        log.debug("ActionManager.processEvent started on Thread: " + Thread.currentThread().getName());
        PersistentWorkflowI workflow = generateWorkflowEntryIfAppropriate(subscription, deliveryId, esEvent, user);
        EventServiceActionProvider provider = getActionProviderByKey(subscription.actionKey());
        if(provider!= null) {
            if(workflow !=null){
                try {
                    WorkflowUtils.setStep(workflow, (provider.getDisplayName() != null ? provider.getDisplayName() : "Provider") + " action called.");
                } catch (Exception e) {
                    log.error("Workflow completion exception for workflow:" + workflow.getId());
                    log.error(e.getMessage());
                }
            }
            processAsync(provider, subscription, esEvent, user, deliveryId, workflow);
        } else {
            String errorMessage = "Could not find Action Provider for ActionKey: " + subscription.actionKey();
            subscriptionDeliveryEntityService.addStatus(deliveryId, FAILED, new Date(), "Could not find Action Provider for ActionKey: " + subscription.actionKey());
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
    public void processAsync(EventServiceActionProvider provider, Subscription subscription, EventServiceEvent esEvent, final UserI user, final Long deliveryId, final PersistentWorkflowI workflow){
        log.debug("Started Async process on thread: {}", Thread.currentThread().getName());
        try {
            log.debug("Resolving subscription event/action attributes.");
            subscriptionDeliveryEntityService.addStatus(deliveryId, RESOLVING_ATTRIBUTES, new Date(), "Resolving subscription event/action attributes.");
            Subscription resolvedSubscription = eventPropertyService.resolveEventPropertyVariables(subscription, esEvent, user, deliveryId);
            try{
                log.debug("Passing event/action processing off to action provider : " + provider.getName());
                subscriptionDeliveryEntityService.addStatus(deliveryId, ACTION_CALLED, new Date(), "Event passed to Action Provider: " + provider.getDisplayName());
                provider.processEvent(esEvent, resolvedSubscription, user, deliveryId);
                SubscriptionDeliveryEntity delivery = subscriptionDeliveryEntityService.get(deliveryId);
                if (workflow != null && delivery != null){
                    TimedEventStatusEntity.Status status = delivery.getStatus();
                    workflow.setComments(status.name());
                    if(status.equals(ACTION_FAILED) || status.equals(ACTION_ERROR) || status.equals(FAILED)){
                        WorkflowUtils.fail(workflow, workflow.buildEvent());
                    } else {
                        WorkflowUtils.complete(workflow, workflow.buildEvent());
                    }
                }
            } catch (Throwable e){
                log.error("Exception thrown calling provider processEvent\n" + e.getMessage(),e);
                if (workflow != null) WorkflowUtils.fail(workflow, workflow.buildEvent());
                subscriptionDeliveryEntityService.addStatus(deliveryId, ACTION_FAILED, new Date(), "Exception thrown calling provider processEvent.  " + provider.getName());

            }
        } catch (Throwable e){
            log.error("Exception thrown while resolving attributes on " + subscription.name() + "\nFor: " + subscription.attributes().toString());
            subscriptionDeliveryEntityService.addStatus(deliveryId, FAILED, new Date(), "Exception thrown while resolving attributes on " + subscription.name());
        }
        log.debug("Ending Async process on thread: {}", Thread.currentThread().getName());
    }

}
