package org.nrg.xnat.eventservice.services;


import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnat.eventservice.entities.SubscriptionEntity;
import org.nrg.xnat.eventservice.exceptions.SubscriptionValidationException;
import org.nrg.xnat.eventservice.model.Subscription;

import javax.annotation.Nonnull;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Transient;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface EventSubscriptionEntityService extends BaseHibernateService<SubscriptionEntity> {


    Subscription createSubscription(Subscription subscription) throws SubscriptionValidationException;

    Subscription validate(Subscription eventSubscription) throws SubscriptionValidationException;

    Subscription activate(Subscription eventSubscription);

    Subscription deactivate(Subscription eventSubscription) throws NotFoundException, EntityNotFoundException;

    Subscription save(Subscription subscription);

    Subscription update(Subscription subscription) throws NotFoundException, SubscriptionValidationException;

    void delete(Long subscriptionId) throws Exception;

    List<Subscription> getAllSubscriptions();

    List<Subscription> getSubscriptions(String projectId);

    Subscription getSubscription(Long id) throws NotFoundException;

    @Transient
    List<Subscription> getSubscriptionsByListenerId(UUID listenerId) throws NotFoundException;

    @Transient
    Subscription toPojo(SubscriptionEntity entity);

    @Nonnull
    @Transient
    List<Subscription> toPojo(List<SubscriptionEntity> subscriptionEntities);

    @Transient
    UUID getListenerId(Long subscriptionId);

    @Transient
    Set getActiveRegistrationSubscriptionIds();

    @Transient
    void removeActiveRegistration(Long subscriptionId);
}
