package org.nrg.xnat.eventservice.daos;

import com.google.common.base.Strings;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.eventservice.entities.SubscriptionDeliveryEntity;
import org.nrg.xnat.eventservice.entities.TimedEventStatusEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SubscriptionDeliveryEntityDao extends AbstractHibernateDAO<SubscriptionDeliveryEntity> {

    //public List<SubscriptionDeliveryEntity> findByProjectId(String projectId){
    //    return findByProperty("projectId", projectId);
    //}
//
    //public List<SubscriptionDeliveryEntity> findBySubscriptionId(Long subscriptionId){
    //    return getSession()
    //            .createQuery("select sde from SubscriptionDeliveryEntity as sde where sde.subscription.id = :subscriptionId")
    //            .setLong("subscriptionId", subscriptionId)
    //            .list();
    //}
//
    //public List<SubscriptionDeliveryEntity> findByProjectIdAndSubscriptionId(String projectId, Long subscriptionId) {
    //    return getSession()
    //            .createQuery("select sde from SubscriptionDeliveryEntity as sde where sde.subscription.id = :subscriptionId and sde.projectId = :projectId")
    //            .setLong("subscriptionId", subscriptionId)
    //            .setString("projectId", projectId)
    //            .list();
    //}

    public List<SubscriptionDeliveryEntity> get(String projectId, Long subscriptionId, Integer firstResult, Integer maxResults, TimedEventStatusEntity.Status statusToExclude){
        Criteria cr = getSession().createCriteria(SubscriptionDeliveryEntity.class);
        if(!Strings.isNullOrEmpty(projectId)){
            cr.add(Restrictions.eq("projectId", projectId));
        }
        if (subscriptionId != null){
            cr.createAlias("subscription", "sub");
            cr.add(Restrictions.eq("sub.id", subscriptionId));
        }
        if(statusToExclude != null){
            cr.add(Restrictions.ne("status", statusToExclude));
        }

        if (firstResult == null || firstResult < 1){
            firstResult = 1;
        }
        if (maxResults != null && maxResults > 0){
            cr.setFirstResult(firstResult);
            cr.setMaxResults(maxResults);
        }
        cr.addOrder(Order.desc("id"));
        cr.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return cr.list();
    }

    public Integer count(String projectId, Long subscriptionId, TimedEventStatusEntity.Status statusToExclude) {
        Criteria cr = getSession().createCriteria(SubscriptionDeliveryEntity.class);
        if (!Strings.isNullOrEmpty(projectId)) {
            cr.add(Restrictions.eq("projectId", projectId));
        }
        if (subscriptionId != null){
            cr.createAlias("subscription", "sub");
            cr.add(Restrictions.eq("sub.id", subscriptionId));
        }
        if(statusToExclude != null){
            cr.add(Restrictions.ne("status", statusToExclude));
        }
        cr.setProjection(Projections.rowCount());
        Long count = (Long)cr.uniqueResult();
        return count != null ? count.intValue() : null;
    }
}
