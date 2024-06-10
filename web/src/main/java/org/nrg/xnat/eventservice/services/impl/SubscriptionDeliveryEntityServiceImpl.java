package org.nrg.xnat.eventservice.services.impl;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.eventservice.daos.SubscriptionDeliveryEntityDao;
import org.nrg.xnat.eventservice.entities.EventServicePayloadEntity;
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
import org.nrg.xnat.eventservice.services.SubscriptionDeliveryEntityPaginatedRequest;
import org.nrg.xnat.eventservice.services.SubscriptionDeliveryEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.EVENT_DETECTED;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.EVENT_TRIGGERED;

@Slf4j
@Service
@Transactional
public class SubscriptionDeliveryEntityServiceImpl extends AbstractHibernateEntityService<SubscriptionDeliveryEntity, SubscriptionDeliveryEntityDao> implements SubscriptionDeliveryEntityService {
    private final EventService                   eventService;
    private final EventSubscriptionEntityService eventSubscriptionEntityService;

    @Autowired
    public SubscriptionDeliveryEntityServiceImpl(@Lazy EventService eventService, @Lazy EventSubscriptionEntityService eventSubscriptionEntityService) {
        this.eventService = eventService;
        this.eventSubscriptionEntityService = eventSubscriptionEntityService;
    }

    @Override
    public Long create(SubscriptionEntity subscription, final EventServiceEvent<?> event, EventServiceListener<?> listener, String actionUserLogin, String projectId, String actionInputs) {
        try {
            final SubscriptionDeliveryEntity delivery = new SubscriptionDeliveryEntity(subscription,
                    (event.getDisplayName() + " : " + event.getCurrentStatus().toString()),
                    actionUserLogin, projectId, actionInputs);
            log.debug("Created new SubscriptionDeliveryEntity for subscription: {} and eventUUID {}", subscription.getName(), event.getEventUUID());
            super.create(delivery);
            addStatus(delivery.getId(), EVENT_TRIGGERED, event.getEventTimestamp(), "Event triggered.");
            addStatus(delivery.getId(), EVENT_DETECTED, listener.getDetectedTimestamp(), "Event detected.");
            return delivery.getId();
        } catch (Exception e) {
            log.error("Could not create new SubscriptionDeliveryEntity for subscription: {} and eventUUID {}", subscription.getName(), event.getEventUUID(), e);
        }
        return null;
    }

    @Override
    public void addStatus(Long deliveryId, TimedEventStatusEntity.Status status, Date statusTimestamp, String message) {
        SubscriptionDeliveryEntity subscriptionDeliveryEntity = retrieve(deliveryId);
        if (subscriptionDeliveryEntity != null) {
            subscriptionDeliveryEntity.addTimedEventStatus(status, statusTimestamp, message);
            //update(subscriptionDeliveryEntity);
            log.debug("Updated SubscriptionDeliveryEntity: {} with status: {}", deliveryId, status.toString());
        } else {
            log.error("Could not find SubscriptionDeliveryEntity: {} with status: {}", deliveryId, status.toString());
        }
    }

    @Override
    public void addPayload(Long deliveryId, Object payload){
        SubscriptionDeliveryEntity subscriptionDeliveryEntity = retrieve(deliveryId);
        if (subscriptionDeliveryEntity != null) {
            subscriptionDeliveryEntity.setPayload(new EventServicePayloadEntity(payload, subscriptionDeliveryEntity));
            //update(subscriptionDeliveryEntity);
            log.debug("Updated SubscriptionDeliveryEntity: {} with payload.", deliveryId);
        } else {
            log.error("Could not find SubscriptionDeliveryEntity: {} to add payload", deliveryId);
        }
    }

    @Override
    public void deletePayloads(Integer keepRecentCount){
        List<SubscriptionDeliveryEntity> withPayload = getDao().excludeByProperty("payload", null);
        withPayload.sort(Comparator.comparing(AbstractHibernateEntity::getTimestamp));
        ListIterator<SubscriptionDeliveryEntity> deliveryIt = withPayload.listIterator(withPayload.size());
        while(deliveryIt.hasPrevious()){
            SubscriptionDeliveryEntity sde = deliveryIt.previous();
            if(keepRecentCount<=0 && sde.getPayload().getPayload() != null){
                sde.getPayload().clear();
            }
            else if(keepRecentCount > 0 && sde.getPayload().getPayload() != null){
                keepRecentCount--;
            }
        }
    }


    @Override
    public void setTriggeringEvent(Long deliveryId, String eventName, String status, Boolean isXsiType, String xnatType, String xsiUri, String objectLabel) {
        SubscriptionDeliveryEntity subscriptionDeliveryEntity = retrieve(deliveryId);
        if (subscriptionDeliveryEntity != null) {
            subscriptionDeliveryEntity.addTriggeringEventEntity(eventName, status, isXsiType, xnatType, xsiUri, objectLabel);
            update(subscriptionDeliveryEntity);
            log.debug("Updated SubscriptionDeliveryEntity: {} with triggering event object: {}", deliveryId, eventName);
        } else {
            log.error("Could not find SubscriptionDeliveryEntity: {} to update with triggering event", deliveryId);
        }
    }

    @Override
    public Integer count(String projectId, Long subscriptionId, Boolean includeFilterMismatches) {
        return getDao().count(projectId, subscriptionId, includeFilterMismatches == null || !includeFilterMismatches ? TimedEventStatusEntity.Status.OBJECT_FILTER_MISMATCH_HALT : null);
    }

    @Override
    public List<SubscriptionDeliverySummary> getSummaries(String projectId) {
        return toSummaries(getDao().getSummaryDeliveries(projectId));
    }

    @Override
    public SubscriptionDelivery get(Long id, String projectId) throws NotFoundException {
        SubscriptionDeliveryEntity deliveryEntity = get(id);
        if (!Strings.isNullOrEmpty(projectId) && !projectId.contentEquals(deliveryEntity.getProjectId())) {
            throw new NotFoundException("No history item with matching id and projectID");
        }
        return toPojo(deliveryEntity, true);
    }

    @Override
    public List<SubscriptionDelivery> get(final String projectId, final Long subscriptionId, final @Nonnull Boolean includeFilterMismatches, final SubscriptionDeliveryEntityPaginatedRequest request, Boolean loadChildren) {
        return toDeliveries(getDao().get(projectId, subscriptionId, !includeFilterMismatches ? TimedEventStatusEntity.Status.OBJECT_FILTER_MISMATCH_HALT : null, request), loadChildren);
    }

    private List<SubscriptionDeliverySummary> toSummaries(final List<SubscriptionDeliverySummaryEntity> entities) {
        return entities != null ? entities.stream().map(this::toPojo).filter(Objects::nonNull).collect(Collectors.toList()) : Collections.emptyList();
    }

    private List<SubscriptionDelivery> toDeliveries(final List<SubscriptionDeliveryEntity> entities, Boolean loadChildren) {
        return entities != null ? entities.stream()
                                          .map(ent -> this.toPojo(ent, loadChildren == null ? false : loadChildren))
                                          .filter(Objects::nonNull).collect(Collectors.toList()) : Collections.emptyList();
    }

    private SubscriptionDeliverySummary toPojo(final SubscriptionDeliverySummaryEntity entity) {
        if (entity == null) {
            return null;
        }
        return SubscriptionDeliverySummary.builder()
                                          .id(entity.getId())
                                          .eventName(getEventDisplayName(entity))
                                          .subscriptionId(entity.getSubscriptionId())
                                          .subscriptionName(entity.getSubscriptionName())
                                          .actionUser(entity.getActionUser())
                                          .projectId(entity.getProjectId())
                                          .triggerLabel(entity.getTriggerLabel())
                                          .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                                          .timestamp(entity.getTimestamp())
                                          .build();
    }


    private SubscriptionDelivery toPojo(SubscriptionDeliveryEntity entity, Boolean loadChildren) {
        SubscriptionDelivery subscriptionDelivery = null;
        if (entity != null) {
            subscriptionDelivery = SubscriptionDelivery.builder()
                                                       .id(entity.getId())
                                                       .eventType(entity.getEventType())
                                                       .timestamp(entity.getStatusTimestamp())
                                                       .actionUser(entity.getActionUserLogin())
                                                       .projectId(entity.getProjectId())
                                                       .actionInputs(entity.getActionInputs())
                                                       .triggeringEvent(entity.getTriggeringEventEntity() != null ?
                                                               entity.getTriggeringEventEntity().toPojo() : null)
                                                       .timedEventStatuses(loadChildren ?
                                                               TimedEventStatusEntity.toPojo(entity.getTimedEventStatuses()) :
                                                               null)
                                                       .statusMessage(entity.getStatusMessage())
                                                       .subscription(eventSubscriptionEntityService.toPojo(entity.getSubscription()))
                                                       .errorState(entity.getErrorState())
                                                       .serializablePayload(loadChildren ? entity.getPayloadObject() : null)
                                                       .build();
        }
        return subscriptionDelivery;
    }

    private String getEventDisplayName(final SubscriptionDeliverySummaryEntity entity) {
        final String eventName = entity.getEventName();
        try {
            if (StringUtils.isNotBlank(eventName)) {
                return eventService.getEvent(eventName, false).displayName();
            }
        } catch (Exception e) {
            log.error("Exception while attempting to load Event for delivery display. {}" + e.getMessage());
        }
        return null;
    }
}
