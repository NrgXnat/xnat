package org.nrg.xnat.eventservice.daos;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
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

    public List<SubscriptionDeliveryEntity> get(final String projectId, final Long subscriptionId, final TimedEventStatusEntity.Status statusToExclude, final SubscriptionDeliveryEntityPaginatedRequest paginatedRequest) {

        final Map<String, Filter> newFilters = new HashMap();
        final Map<String, Filter> requestFilters = paginatedRequest.getFiltersMap();

        // Method parameter filters
        if (StringUtils.isNotBlank(projectId)) {
            newFilters.put("projectId", HibernateFilter.builder().operator(HibernateFilter.Operator.EQ).value(projectId).build());
        }
        if (subscriptionId != null) {
            newFilters.put("subscriptionId", HibernateFilter.builder().operator(HibernateFilter.Operator.EQ).value(subscriptionId).build());
        }
        if (statusToExclude != null) {
            newFilters.put("status", HibernateFilter.builder().operator(HibernateFilter.Operator.NE).value(statusToExclude).build());
        }

        // Request filters
        if (paginatedRequest.hasFilters()) {

            // Method projectId parameter supersedes request project filter
            if (projectId == null && requestFilters != null && requestFilters.containsKey("project")){
                newFilters.put("projectId", requestFilters.get("project"));
            }
            if (paginatedRequest.getFiltersMap().containsKey("subscription")){
                newFilters.put("description", requestFilters.get("subscription"));
            }
            if (requestFilters != null && requestFilters.containsKey("eventtype")){
                newFilters.put("eventType", requestFilters.get("eventtype"));
            }
            if (requestFilters != null && requestFilters.containsKey("user")){
                newFilters.put("actionUserLogin", requestFilters.get("user"));
            }
            if (requestFilters != null && requestFilters.containsKey("status")){
                newFilters.put("statusMessage", requestFilters.get("status"));
            }
        }

        paginatedRequest.setFiltersMap(newFilters);

        return findPaginated(paginatedRequest);

    }


    private List<SubscriptionDeliveryEntity> findPaginated(SubscriptionDeliveryEntityPaginatedRequest paginatedRequest) {
        final Criteria criteria = getCriteriaForType();
        if (paginatedRequest.hasFilters()) {
            ClassMetadata classMetadata = getSession().getSessionFactory().getClassMetadata(getParameterizedType());
            for (Criterion criterion : paginatedRequest.getCriterion(classMetadata)) {
                criteria.add(criterion);
            }
        }

        // Override findPaginated to add fetchMode to criteria
        criteria.setFetchMode("timedEventStatuses", FetchMode.SELECT);

        criteria.addOrder(paginatedRequest.getOrder());
        criteria.setMaxResults(paginatedRequest.getPageSize());
        criteria.setFirstResult(paginatedRequest.getOffset());
        return criteria.list();
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
