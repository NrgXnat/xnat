package org.nrg.xnat.eventservice.daos;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.eventservice.entities.SubscriptionDeliveryEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SubscriptionDeliveryEntityDao extends AbstractHibernateDAO<SubscriptionDeliveryEntity> {
    public List<SubscriptionDeliveryEntity> findByProjectId(String id){
        return findByProperty("projectId", id);
    }

    public List<SubscriptionDeliveryEntity> findBySubscriptionId(Long subscriptionId){
        return getSession()
                .createQuery("select sde from SubscriptionDeliveryEntity as sde where sde.subscription.id = :subscriptionId")
                .setLong("subscriptionId", subscriptionId)
                .list();
    }

    public List<SubscriptionDeliveryEntity> findByProjectIdAndSubscriptionId(String projectId, Long subscriptionId) {
        return getSession()
                .createQuery("select sde from SubscriptionDeliveryEntity as sde where sde.subscription.id = :subscriptionId and sde.projectId = :projectId")
                .setLong("subscriptionId", subscriptionId)
                .setString("projectId", projectId)
                .list();
    }
}
