package org.nrg.xnat.eventservice.services.impl;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.eventservice.daos.SubscriptionDeliveryEntityDao;
import org.nrg.xnat.eventservice.entities.SubscriptionDeliveryEntity;
import org.nrg.xnat.eventservice.entities.SubscriptionDeliverySummaryEntity;
import org.nrg.xnat.eventservice.entities.SubscriptionEntity;
import org.nrg.xnat.eventservice.entities.TimedEventStatusEntity;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.nrg.xnat.eventservice.model.SubscriptionDelivery;
import org.nrg.xnat.eventservice.model.SubscriptionDeliverySummary;
import org.nrg.xnat.eventservice.services.EventService;
import org.nrg.xnat.eventservice.services.EventSubscriptionEntityService;
import org.nrg.xnat.eventservice.services.SubscriptionDeliveryEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.EVENT_DETECTED;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.EVENT_TRIGGERED;

@Slf4j
@Service
@Transactional
public class SubscriptionDeliveryEntityServiceImpl
        extends AbstractHibernateEntityService<SubscriptionDeliveryEntity, SubscriptionDeliveryEntityDao>
        implements SubscriptionDeliveryEntityService {

    private EventService eventService;
    private EventSubscriptionEntityService eventSubscriptionEntityService;

    @Autowired
    public SubscriptionDeliveryEntityServiceImpl(@Lazy EventService eventService, @Lazy EventSubscriptionEntityService eventSubscriptionEntityService) {
        this.eventService = eventService;
        this.eventSubscriptionEntityService = eventSubscriptionEntityService;
    }

    @Override
    public Long create(SubscriptionEntity subscription, EventServiceEvent event, EventServiceListener listener, String actionUserLogin, String projectId,
                       String actionInputs) {
        try {

            SubscriptionDeliveryEntity delivery = new SubscriptionDeliveryEntity(subscription, event.getType(), actionUserLogin, projectId, actionInputs);
            if (delivery != null) {
                log.debug("Created new SubscriptionDeliveryEntity for subscription: {} and eventUUID {}", subscription.getName(), event.getEventUUID());
                super.create(delivery);
                addStatus(delivery.getId(), EVENT_TRIGGERED, event.getEventTimestamp(), "Event triggered.");
                addStatus(delivery.getId(), EVENT_DETECTED, listener.getDetectedTimestamp(), "Event detected.");
                return delivery.getId();
            }
        } catch (Exception e) {
            log.error("Could not create new SubscriptionDeliveryEntity for subscription: {} and eventUUID {}", subscription.getName(), event.getEventUUID());
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public void addStatus(Long deliveryId, TimedEventStatusEntity.Status status, Date statusTimestamp, String message) {
        addStatus(deliveryId, status, statusTimestamp, message, null);
    }

    @Override
    public void addStatus(Long deliveryId, TimedEventStatusEntity.Status status, Date statusTimestamp, String message, Object payload) {
        SubscriptionDeliveryEntity subscriptionDeliveryEntity = retrieve(deliveryId);
        if(subscriptionDeliveryEntity != null) {
            subscriptionDeliveryEntity.addTimedEventStatus(status, statusTimestamp, message, payload);
            update(subscriptionDeliveryEntity);
            log.debug("Updated SubscriptionDeliveryEntity: {} with status: {}", deliveryId, status.toString());
        } else{
            log.error("Could not find SubscriptionDeliveryEntity: {} with status: {}", deliveryId, status.toString());
        }
    }

    @Override
    public void setTriggeringEvent(Long deliveryId, String eventName, Boolean isXsiType, String xnatType, String xsiUri, String objectLabel){
        SubscriptionDeliveryEntity subscriptionDeliveryEntity = retrieve(deliveryId);
        if(subscriptionDeliveryEntity != null) {
            subscriptionDeliveryEntity.addTriggeringEventEntity(eventName,isXsiType,xnatType,xsiUri,objectLabel);
            update(subscriptionDeliveryEntity);
            log.debug("Updated SubscriptionDeliveryEntity: {} with triggering event object: {}", deliveryId, eventName);
        } else{
            log.error("Could not find SubscriptionDeliveryEntity: {} to update with triggering event", deliveryId);
        }
    }

    @Override
    public Integer count(String projectId, Long subscriptionId, Boolean includeFilterMismatches) {
        return getDao().count(
                projectId,
                subscriptionId,
                (includeFilterMismatches == null || includeFilterMismatches == false
                        ? TimedEventStatusEntity.Status.OBJECT_FILTER_MISMATCH_HALT
                        : null));
    }

    //@Override
    //public List<SubscriptionDelivery> get(String projectId, Long subscriptionId, Boolean includeFilterMismatches) {
    //    List<SubscriptionDeliveryEntity> deliveryEntities = null;
    //    if(subscriptionId == null){
    //        if(Strings.isNullOrEmpty(projectId)){ deliveryEntities = getAll(); }
    //        else { deliveryEntities = getDao().findByProjectId(projectId); }
    //    } else {
    //        if(Strings.isNullOrEmpty(projectId)){ deliveryEntities = getDao().findBySubscriptionId(subscriptionId); }
    //        else { deliveryEntities = getDao().findByProjectIdAndSubscriptionId(projectId, subscriptionId); }
    //    }
    //    if(deliveryEntities == null) {
    //        return new ArrayList<>();
    //    } else if(includeFilterMismatches != null && includeFilterMismatches == true){
    //        return toPojo(deliveryEntities);
    //    } else {
    //        return toPojo(
    //                deliveryEntities.stream()
    //                                .filter(de -> de.getStatus() != TimedEventStatusEntity.Status.OBJECT_FILTER_MISMATCH_HALT)
    //                                .collect(Collectors.toList()));
    //    }
    //}


    @Override
    public List<SubscriptionDeliverySummary> getSummaries(String projectId) {
        return toPojos(getDao().getSummaryDeliveries(projectId));
    }

    @Override
    public SubscriptionDelivery get(Long id, String projectId) throws NotFoundException {
        SubscriptionDeliveryEntity deliveryEntity = get(id);
        if(!Strings.isNullOrEmpty(projectId) && !projectId.contentEquals(deliveryEntity.getProjectId())){
            throw new NotFoundException("No history item with matching id and projectID");
        }
        return toPojo(deliveryEntity);
    }

    @Override
    public List<SubscriptionDelivery> get(String projectId, Long subscriptionId, @Nonnull Boolean includeFilterMismatches,
                                          Integer firstResult, Integer maxResults) {
        return toPojo(getDao().get(projectId, subscriptionId, firstResult, maxResults,
                (includeFilterMismatches == null || includeFilterMismatches == false
                        ? TimedEventStatusEntity.Status.OBJECT_FILTER_MISMATCH_HALT
                        : null)));
    }

    private List<SubscriptionDeliverySummary> toPojos(List<SubscriptionDeliverySummaryEntity> entities){
        List<SubscriptionDeliverySummary> summaries = new ArrayList<>();
        if(entities != null) {
            for (SubscriptionDeliverySummaryEntity sde : entities) {
                SubscriptionDeliverySummary summary = toPojo(sde);
                if (summary != null) summaries.add(summary);
            }
        }
        return summaries;
    }

    private SubscriptionDeliverySummary toPojo(SubscriptionDeliverySummaryEntity entity) {
        SubscriptionDeliverySummary summary = null;
        if (entity != null) {
            String eventName = entity.getEventName();
            try {
                if (!Strings.isNullOrEmpty(eventName)) {
                    eventName = eventService.getEvent(eventName, false).displayName();
                }
            } catch (Exception e) {
                log.error("Exception while attempting to load Event for delivery display. {}" + e.getMessage());
            }

            summary = SubscriptionDeliverySummary.builder()
                                                 .id(entity.getId())
                                                 .eventName(eventName)
                                                 .subscriptionName(entity.getSubscriptionName())
                                                 .actionUser(entity.getActionUser())
                                                 .projectId(entity.getProjectId())
                                                 .triggerLabel(entity.getTriggerLabel())
                                                 .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                                                 .timestamp(entity.getTimestamp())
                                                 .build();        }
        return summary;
    }


    private List<SubscriptionDelivery> toPojo(List<SubscriptionDeliveryEntity> entities){
        List<SubscriptionDelivery> deliveries = new ArrayList<>();
        if(entities != null) {
            for (SubscriptionDeliveryEntity sde : entities) {
                SubscriptionDelivery delivery = toPojo(sde);
                if (delivery != null) deliveries.add(delivery);
            }
        }
        return deliveries;
    }

    private SubscriptionDelivery toPojo(SubscriptionDeliveryEntity entity) {
        SubscriptionDelivery subscriptionDelivery = null;
        if (entity != null) {
            subscriptionDelivery = SubscriptionDelivery.builder()
                                                                            .id(entity.getId())
                                                                            .actionUser(entity.getActionUserLogin())
                                                                            .projectId(entity.getProjectId())
                                                                            .actionInputs(entity.getActionInputs())
                                                                            .triggeringEvent(entity.getTriggeringEventEntity() != null ? entity.getTriggeringEventEntity().toPojo() : null)
                                                                            .timedEventStatuses(TimedEventStatusEntity.toPojo(entity.getTimedEventStatuses()))
                                                                            .subscription(eventSubscriptionEntityService.toPojo(entity.getSubscription()))
                                                                            .build();
            try {
                if (!Strings.isNullOrEmpty(entity.getEventType())) {
                    subscriptionDelivery = subscriptionDelivery.toBuilder()
                                                               .event(eventService.getEvent(entity.getEventType(), false))
                                                               .build();
                } else {
                    log.error("EventType not stored is subscription delivery history table. Skipping creation of event for display.");
                }
            } catch (Exception e) {
                log.error("Exception while attempting to load Event for delivery display. {}" + e.getMessage());
            }
        }
        return subscriptionDelivery;
    }


}
