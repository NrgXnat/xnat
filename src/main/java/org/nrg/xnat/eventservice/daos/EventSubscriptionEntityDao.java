package org.nrg.xnat.eventservice.daos;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.framework.orm.hibernate.QueryBuilder;
import org.nrg.xnat.eventservice.entities.SubscriptionEntity;
import org.springframework.stereotype.Repository;

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
        QueryBuilder<SubscriptionEntity> builder = newQueryBuilder();
        builder.join("eventServiceFilterEntity", "f");
        builder.where(builder.and(builder.eq("f.schedule", schedule),
                                  builder.like("f.eventType", "%ScheduledEvent"),
                                  builder.eq("f.status", "CRON"),
                                  builder.eq("active", true)));
        return builder.getResults();
    }
}
