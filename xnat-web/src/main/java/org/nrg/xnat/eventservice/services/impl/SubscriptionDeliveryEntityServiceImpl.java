package org.nrg.xnat.eventservice.services.impl;

import com.google.common.base.Strings;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.eventservice.daos.SubscriptionDeliveryEntityDao;
import org.nrg.xnat.eventservice.entities.SubscriptionDeliveryEntity;
import org.nrg.xnat.eventservice.entities.SubscriptionEntity;
import org.nrg.xnat.eventservice.entities.TimedEventStatusEntity;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.nrg.xnat.eventservice.model.SubscriptionDelivery;
import org.nrg.xnat.eventservice.services.EventService;
import org.nrg.xnat.eventservice.services.SubscriptionDeliveryEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.EVENT_DETECTED;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.EVENT_TRIGGERED;

@Service
@Transactional
public class SubscriptionDeliveryEntityServiceImpl
        extends AbstractHibernateEntityService<SubscriptionDeliveryEntity, SubscriptionDeliveryEntityDao>
        implements SubscriptionDeliveryEntityService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionDeliveryEntityService.class);
    private EventService eventService;

    @Autowired
    public SubscriptionDeliveryEntityServiceImpl(@Lazy EventService eventService) {
        this.eventService = eventService;
    }

    @Transactional
    @Override

    public Long create(SubscriptionEntity subscription, EventServiceEvent event, EventServiceListener listener, String actionUserLogin, String projectId,
                       String actionInputs) {
        try {
            SubscriptionDeliveryEntity delivery = new SubscriptionDeliveryEntity(subscription, event.getEventUUID(), actionUserLogin, projectId, actionInputs);
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
        SubscriptionDeliveryEntity subscriptionDeliveryEntity = retrieve(deliveryId);
        if(subscriptionDeliveryEntity != null) {
            subscriptionDeliveryEntity.addTimedEventStatus(status, statusTimestamp, message);
            update(subscriptionDeliveryEntity);
            log.debug("Updated SubscriptionDeliveryEntity: {} to update with status: {}", deliveryId, status.toString());
        } else{
            log.error("Could not find SubscriptionDeliveryEntity: {} to update with status: {}", deliveryId, status.toString());
        }
    }

    @Override
    public List<SubscriptionDelivery> get(String projectId, Long subscriptionId) {
        if(subscriptionId == null){
            if(Strings.isNullOrEmpty(projectId)){ return toPojo(getAll()); }
            else { return toPojo(getDao().findByProjectId(projectId)); }
        } else {
            if(Strings.isNullOrEmpty(projectId)){ return toPojo(getDao().findBySubscriptionId(subscriptionId)); }
            else { return toPojo(getDao().findByProjectIdAndSubscriptionId(projectId, subscriptionId)); }
        }
    }

    private List<SubscriptionDelivery> toPojo(List<SubscriptionDeliveryEntity> entities){
        List<SubscriptionDelivery> deliveries = new ArrayList<>();
        if(entities != null) {
            for (SubscriptionDeliveryEntity sde : entities) {
                deliveries.add(toPojo(sde));
            }
        }
        return deliveries;
    }

    private SubscriptionDelivery toPojo(SubscriptionDeliveryEntity entity) {
        SubscriptionDelivery subscriptionDelivery = SubscriptionDelivery.builder()
                .id(entity.getId())
                .actionUser(entity.getActionUserLogin())
                .projectId(entity.getProjectId())
                .actionInputs(entity.getActionInputs())
                .timedEventStatuses(TimedEventStatusEntity.toPojo(entity.getTimedEventStatuses()))
                .subscription(entity.getSubscription().toPojo())
                .build();
        try{
            subscriptionDelivery = subscriptionDelivery.toBuilder()
                    .event(eventService.getEvent(entity.getEventUUID()))
                    .build();
        } catch (Exception e) {
            log.error("Exception while attempting to load Event for delivery display. {}" + e.getMessage());
        }
        return subscriptionDelivery;
    }


}
