package org.nrg.xnat.eventservice.daos;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.ajax.Filter;
import org.nrg.framework.ajax.hibernate.HibernateFilter;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.eventservice.entities.SubscriptionDeliveryEntity;
import org.nrg.xnat.eventservice.entities.SubscriptionDeliverySummaryEntity;
import org.nrg.xnat.eventservice.entities.TimedEventStatusEntity;
import org.nrg.xnat.eventservice.services.SubscriptionDeliveryEntityPaginatedRequest;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.nrg.framework.generics.GenericUtils.convertToTypedList;

@Repository
public class SubscriptionDeliveryEntityDao extends AbstractHibernateDAO<SubscriptionDeliveryEntity> {
    public List<SubscriptionDeliverySummaryEntity> getSummaryDeliveries(final String projectId) {
        final boolean hasProjectId = StringUtils.isNotBlank(projectId);
        final Query   query        = getSession().createQuery(hasProjectId ? QUERY_SUMMARY_DELIVERIES_BY_PROJECT : QUERY_SUMMARY_DELIVERIES);
        query.setInteger("statusToExclude", TimedEventStatusEntity.Status.OBJECT_FILTER_MISMATCH_HALT.ordinal());
        if (hasProjectId) {
            query.setString("projectId", projectId);
        }
        return convertToTypedList(query.list(), SubscriptionDeliverySummaryEntity.class);
    }

    public List<SubscriptionDeliveryEntity> get(final String projectId, final Long subscriptionId, final TimedEventStatusEntity.Status statusToExclude, final SubscriptionDeliveryEntityPaginatedRequest request) {
        final Map<String, Filter> filters = new HashMap<>();
        if (StringUtils.isNotBlank(projectId)) {
            filters.put("projectId", HibernateFilter.builder().operator(HibernateFilter.Operator.EQ).value(projectId).build());
        }
        if (subscriptionId != null) {
            filters.put("subscription.id", HibernateFilter.builder().operator(HibernateFilter.Operator.EQ).value(subscriptionId).build());
        }
        if (statusToExclude != null) {
            filters.put("status", HibernateFilter.builder().operator(HibernateFilter.Operator.NE).value(statusToExclude).build());
        }
        request.setFiltersMap(filters);
        // Previous code had this. May need to add code in PaginatedRequest handling to set fetch mode.
        // cr.setFetchMode("timedEventStatuses", FetchMode.SELECT);
        return findPaginated(request);
    }

    public Integer count(final String projectId, final Long subscriptionId, final TimedEventStatusEntity.Status statusToExclude) {
        final Criteria cr = getSession().createCriteria(SubscriptionDeliveryEntity.class);
        if (StringUtils.isNotBlank(projectId)) {
            cr.add(Restrictions.eq("projectId", projectId));
        }
        if (subscriptionId != null) {
            cr.createAlias("subscription", "sub");
            cr.add(Restrictions.eq("sub.id", subscriptionId));
        }
        if (statusToExclude != null) {
            cr.add(Restrictions.ne("status", statusToExclude));
        }
        cr.setProjection(Projections.rowCount());
        final Long count = (Long) cr.uniqueResult();
        return count != null ? count.intValue() : null;
    }

    private static final String BASE_QUERY_SUMMARY_DELIVERIES          = "SELECT NEW org.nrg.xnat.eventservice.entities.SubscriptionDeliverySummaryEntity(D.id, D.eventType, D.subscription.id, D.subscription.name, D.actionUserLogin, D.projectId, D.triggeringEventEntity.objectLabel, D.status, D.errorState, D.statusTimestamp) FROM SubscriptionDeliveryEntity as D WHERE D.status != :statusToExclude";
    private static final String BASE_QUERY_SUMMARY_DELIVERIES_ORDER_BY = " ORDER BY D.id ASC";
    private static final String QUERY_SUMMARY_DELIVERIES               = BASE_QUERY_SUMMARY_DELIVERIES + BASE_QUERY_SUMMARY_DELIVERIES_ORDER_BY;
    private static final String QUERY_SUMMARY_DELIVERIES_BY_PROJECT    = BASE_QUERY_SUMMARY_DELIVERIES + " AND D.projectId = :projectId" + BASE_QUERY_SUMMARY_DELIVERIES_ORDER_BY;
}
