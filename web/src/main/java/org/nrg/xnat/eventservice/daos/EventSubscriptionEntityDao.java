package org.nrg.xnat.eventservice.daos;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.eventservice.entities.SubscriptionEntity;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class EventSubscriptionEntityDao extends AbstractHibernateDAO<SubscriptionEntity> {


    public SubscriptionEntity findByName(final String name) throws Exception {
        try {
                return findByUniqueProperty("name", name);
        } catch (Throwable e) {
            throw new Exception("Exception trying to generate alternative to " + name + "." + "\n" + e.getMessage());
        }
    }

    public List<SubscriptionEntity> findActiveSubscriptionsBySchedule(final String schedule) {
        final Criteria criteria = getCriteriaForType();
        criteria.createAlias("subscriptionEntity.eventServiceFilterEntity", "f")
                .add( Restrictions.eq("f.schedule", schedule) )
                .add( Restrictions.like("f.eventType", "%ScheduledEvent"))
                .add( Restrictions.eq("f.status", "CRON"))
                .add( Restrictions.eq("active", true));

        List<SubscriptionEntity> entities = criteria.list();
        return entities == null ? Collections.EMPTY_LIST : entities;
    }
}
