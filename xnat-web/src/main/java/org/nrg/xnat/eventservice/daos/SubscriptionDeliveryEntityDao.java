package org.nrg.xnat.eventservice.daos;

import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.ajax.Filter;
import org.nrg.framework.ajax.hibernate.HibernateFilter;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.framework.orm.hibernate.QueryBuilder;
import org.nrg.xnat.eventservice.entities.SubscriptionDeliveryEntity;
import org.nrg.xnat.eventservice.entities.SubscriptionDeliverySummaryEntity;
import org.nrg.xnat.eventservice.entities.TimedEventStatusEntity;
import org.nrg.xnat.eventservice.services.SubscriptionDeliveryEntityPaginatedRequest;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.nrg.framework.generics.GenericUtils.convertToTypedList;

@Repository
public class SubscriptionDeliveryEntityDao extends AbstractHibernateDAO<SubscriptionDeliveryEntity> {

    private static final String PROJECT_ID = "projectId";
    private static final String SUBSCRIPTION_ID = "subscriptionId";
    private static final String STATUS = "status";
    private static final String ACTION_USER_LOGIN = "actionUserLogin";
    private static final String EVENT_TYPE = "eventType";
    private static final String DESCRIPTION = "description";
    private static final String PROJECT = "project";
    private static final String SUBSCRIPTION = "subscription";
    private static final String STATUS_MESSAGE = "statusMessage";
    private static final String REQUEST_PARAM_EVENTTYPE = "eventtype";
    private static final String USER = "user";

    public List<SubscriptionDeliverySummaryEntity> getSummaryDeliveries(final String projectId) {
        final boolean hasProjectId = StringUtils.isNotBlank(projectId);
        final Query   query        = getSession().createQuery(hasProjectId ? QUERY_SUMMARY_DELIVERIES_BY_PROJECT : QUERY_SUMMARY_DELIVERIES);
        query.setParameter("statusToExclude", TimedEventStatusEntity.Status.OBJECT_FILTER_MISMATCH_HALT.ordinal());
        if (hasProjectId) {
            query.setParameter(PROJECT_ID, projectId);
        }
        return convertToTypedList(query.getResultList(), SubscriptionDeliverySummaryEntity.class);
    }

    public List<SubscriptionDeliveryEntity> get(final String projectId, final Long subscriptionId, final TimedEventStatusEntity.Status statusToExclude, SubscriptionDeliveryEntityPaginatedRequest paginatedRequest) {
        paginatedRequest = paginatedRequest != null ? paginatedRequest : new SubscriptionDeliveryEntityPaginatedRequest();

        final Map<String, Filter> newFilters     = new HashMap<>();
        final Map<String, Filter> requestFilters = paginatedRequest.getFiltersMap();

        // Method parameter filters
        if (StringUtils.isNotBlank(projectId)) {
            newFilters.put(PROJECT_ID, HibernateFilter.builder().operator(HibernateFilter.Operator.EQ).value(projectId).build());
        }
        if (subscriptionId != null) {
            newFilters.put(SUBSCRIPTION_ID, HibernateFilter.builder().operator(HibernateFilter.Operator.EQ).value(subscriptionId).build());
        }
        if (statusToExclude != null) {
            newFilters.put(STATUS, HibernateFilter.builder().operator(HibernateFilter.Operator.NE).value(statusToExclude).build());
        }

        // Request filters
        if (paginatedRequest.hasFilters()) {

            // Method projectId parameter supersedes request project filter
            if (projectId == null && requestFilters.containsKey(PROJECT)) {
                newFilters.put(PROJECT_ID, requestFilters.get(PROJECT));
            }
            if (paginatedRequest.getFiltersMap().containsKey(SUBSCRIPTION)) {
                newFilters.put(DESCRIPTION, requestFilters.get(SUBSCRIPTION));
            }
            if (requestFilters.containsKey(REQUEST_PARAM_EVENTTYPE)) {
                newFilters.put(EVENT_TYPE, requestFilters.get(REQUEST_PARAM_EVENTTYPE));
            }
            if (requestFilters.containsKey(USER)) {
                newFilters.put(ACTION_USER_LOGIN, requestFilters.get(USER));
            }
            if (requestFilters.containsKey(STATUS)) {
                newFilters.put(STATUS_MESSAGE, requestFilters.get(STATUS));
            }
        }
        paginatedRequest.setFiltersMap(newFilters);

        //Sort column
        if (paginatedRequest.getSortColumn() != null && !paginatedRequest.getSortColumn().isEmpty()) {
            String sortColumn = paginatedRequest.getSortColumn();
            if (sortColumn.contentEquals(USER)) {
                sortColumn = ACTION_USER_LOGIN;
            } else if (sortColumn.contentEquals(STATUS)) {
                sortColumn = STATUS_MESSAGE;
            } else if (sortColumn.contentEquals(PROJECT)) {
                sortColumn = PROJECT_ID;
            } else if (sortColumn.contentEquals(REQUEST_PARAM_EVENTTYPE)) {
                sortColumn = EVENT_TYPE;
            }
            paginatedRequest.setSortColumn(sortColumn);
        }

        return findPaginated(paginatedRequest);
    }

    public List<SubscriptionDeliveryEntity> excludeByProperty(final String property, final Object value) {
        QueryBuilder<SubscriptionDeliveryEntity> builder = newQueryBuilder();
        builder.where(value == null ? builder.isNotNull(property) : builder.ne(property, value));
        return builder.getResults();
    }

    public long count(final String projectId, final Long subscriptionId, final TimedEventStatusEntity.Status statusToExclude) {
        QueryBuilder<SubscriptionDeliveryEntity> builder = newQueryBuilder();

        final List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.isNotBlank(projectId)) {
            predicates.add(builder.eq(PROJECT_ID, projectId));
        }
        if (subscriptionId != null) {
            builder.join(SUBSCRIPTION, "sub");
            predicates.add(builder.eq("sub.id", subscriptionId));
        }
        if (statusToExclude != null) {
            predicates.add(builder.ne(STATUS, statusToExclude));
        }
        builder.where(predicates.toArray(new Predicate[0]));
        List<SubscriptionDeliveryEntity> results =builder.getResults();
        return results.size();
    }

    private static final String BASE_QUERY_SUMMARY_DELIVERIES          = "SELECT NEW org.nrg.xnat.eventservice.entities.SubscriptionDeliverySummaryEntity(D.id, D.eventType, D.subscription.id, D.subscription.name, D.actionUserLogin, D.projectId, D.triggeringEventEntity.objectLabel, D.status, D.errorState, D.statusTimestamp) FROM SubscriptionDeliveryEntity as D WHERE D.status != :statusToExclude";
    private static final String BASE_QUERY_SUMMARY_DELIVERIES_ORDER_BY = " ORDER BY D.id ASC";
    private static final String QUERY_SUMMARY_DELIVERIES               = BASE_QUERY_SUMMARY_DELIVERIES + BASE_QUERY_SUMMARY_DELIVERIES_ORDER_BY;
    private static final String QUERY_SUMMARY_DELIVERIES_BY_PROJECT    = BASE_QUERY_SUMMARY_DELIVERIES + " AND D.projectId = :projectId" + BASE_QUERY_SUMMARY_DELIVERIES_ORDER_BY;
}
