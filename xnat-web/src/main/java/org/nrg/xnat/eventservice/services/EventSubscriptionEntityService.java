package org.nrg.xnat.eventservice.services;


import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnat.eventservice.entities.SubscriptionEntity;
import org.nrg.xnat.eventservice.exceptions.SubscriptionValidationException;
import org.nrg.xnat.eventservice.model.Subscription;

import javax.persistence.EntityNotFoundException;
import java.util.List;

public interface EventSubscriptionEntityService extends BaseHibernateService<SubscriptionEntity>{

    Subscription createSubscription(Subscription subscription) throws SubscriptionValidationException;
    Subscription validate(Subscription eventSubscription) throws SubscriptionValidationException;
    Subscription activate(Subscription eventSubscription);
    Subscription deactivate(Subscription eventSubscription) throws NotFoundException, EntityNotFoundException;
    Subscription save(Subscription subscription);

    void throwExceptionIfNameExists(final Subscription subscription) throws NrgServiceRuntimeException;

    Subscription update(Subscription subscription) throws NotFoundException, SubscriptionValidationException;
    void delete(Long subscriptionId) throws Exception;

    List<Subscription> getAllSubscriptions();
    List<Subscription> getSubscriptionsByKey(String key) throws NotFoundException;
    Subscription getSubscription(Long id) throws NotFoundException;
}
