package org.nrg.xnat.services.archive.impl.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.generics.GenericUtils;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.entities.ResourceSurveyRequest;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class ResourceSurveyRequestRepository extends AbstractHibernateDAO<ResourceSurveyRequest> {
    private static final String PROPERTY_PROJECT_ID    = "projectId";
    private static final String PROPERTY_EXPERIMENT_ID = "experimentId";
    private static final String PROPERTY_REQUEST_TIME  = "requestTime";

    public ResourceSurveyRequestRepository() {
        super();
        log.debug("Creating the resource survey request repository");
    }

    public List<ResourceSurveyRequest> findByIds(final List<Long> requestIds) {
        final Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.in("id", requestIds));
        return GenericUtils.convertToTypedList(criteria.list(), getParameterizedType());
    }

    public Optional<ResourceSurveyRequest> findByResourceId(final int resourceId) {
        return findByResourceIdAndStatus(resourceId, null);
    }

    public List<ResourceSurveyRequest> findAllByResourceId(final int resourceId) {
        return findAllByResourceIdAndStatus(resourceId, null);
    }

    public List<ResourceSurveyRequest> findByResourceIds(final List<Integer> resourceIds) {
        final Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.in("resourceId", resourceIds));
        criteria.add(Restrictions.isNull("closingDate"));
        return GenericUtils.convertToTypedList(criteria.list(), getParameterizedType());
    }

    public Optional<ResourceSurveyRequest> findByResourceIdAndStatus(final int resourceId, final ResourceSurveyRequest.Status status) {
        final Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("resourceId", resourceId));
        if (status != null) {
            criteria.add(Restrictions.eq("rsn_status", status));
        }
        criteria.addOrder(Order.desc("timestamp")).setMaxResults(1);
        return Optional.ofNullable((ResourceSurveyRequest) criteria.uniqueResult());
    }

    public List<ResourceSurveyRequest> findAllByResourceIdAndStatus(final int resourceId, final ResourceSurveyRequest.Status status) {
        final Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("resourceId", resourceId));
        if (status != null) {
            criteria.add(Restrictions.eq("rsn_status", status));
        }
        criteria.addOrder(Order.desc("timestamp"));
        return GenericUtils.convertToTypedList(criteria.list(), ResourceSurveyRequest.class);
    }

    public List<ResourceSurveyRequest> findAllByProjectId(final String projectId) {
        return findByObjectIdAndStatusAndOpen(PROPERTY_PROJECT_ID, projectId, false, null);
    }

    public List<ResourceSurveyRequest> findOpenByProjectId(final String projectId) {
        return findByObjectIdAndStatusAndOpen(PROPERTY_PROJECT_ID, projectId, true, null);
    }

    public List<ResourceSurveyRequest> findByProjectIdAndStatus(final String projectId, final List<ResourceSurveyRequest.Status> statuses) {
        return findByObjectIdAndStatusAndOpen(PROPERTY_PROJECT_ID, projectId, false, statuses);
    }

    public List<ResourceSurveyRequest> findAllBySessionId(final String sessionId) {
        return findByObjectIdAndStatusAndOpen(PROPERTY_EXPERIMENT_ID, sessionId, false, null);
    }

    public List<ResourceSurveyRequest> findOpenBySessionId(final String sessionId) {
        return findByObjectIdAndStatusAndOpen(PROPERTY_EXPERIMENT_ID, sessionId, true, null);
    }

    @SuppressWarnings("unused")
    public List<ResourceSurveyRequest> findBySessionIdAndStatus(final String sessionId, final List<ResourceSurveyRequest.Status> statuses) {
        return findByObjectIdAndStatusAndOpen(PROPERTY_EXPERIMENT_ID, sessionId, false, statuses);
    }

    public List<ResourceSurveyRequest> findAllByRequestTime(final String requestTime) {
        return findByObjectIdAndStatusAndOpen(PROPERTY_REQUEST_TIME, requestTime, false, null);
    }

    public List<ResourceSurveyRequest> findOpenByRequestTime(final String requestTime) {
        return findByObjectIdAndStatusAndOpen(PROPERTY_REQUEST_TIME, requestTime, true, null);
    }

    /**
     * Returns a pair that contains a list of distinct project IDs containing the submitted request IDs, plus a list of
     * longs containing request IDs that were not found (i.e. that are invalid request IDs).
     *
     * @param requestIds The list of request IDs to resolve.
     *
     * @return A pair with a list of distinct project IDs and a list of any invalid request IDs.
     */
    public Pair<List<String>, List<Long>> findRequestProjects(final List<Long> requestIds) {
        final Map<Long, String> resources = GenericUtils.convertToTypedList(getSession().getNamedQuery("findRequestIdAndProject").setParameterList("requestIds", requestIds).list(), Object.class)
                                                        .stream()
                                                        .collect(Collectors.toMap(object -> (Long) ((Object[]) object)[0], object -> (String) ((Object[]) object)[1]));
        return Pair.of(resources.values().stream().distinct().sorted().collect(Collectors.toList()), requestIds.stream().distinct().filter(requestId -> !resources.containsKey(requestId)).collect(Collectors.toList()));
    }

    /**
     * Returns a pair that contains a list of request IDs corresponding to the submitted resource IDs, plus a list of
     * resource IDs that were not found (i.e. that are invalid resource IDs).
     *
     * @param resourceIds The list of resource IDs to resolve.
     *
     * @return A pair with a list of request IDs and a list of any invalid resource IDs.
     */
    public Pair<Map<Long, Integer>, List<Integer>> findResourceRequestIds(final List<Integer> resourceIds) {
        final Map<Long, Integer> resources = GenericUtils.convertToTypedList(getSession().getNamedQuery("findRequestAndResourceId").setParameterList("resourceIds", resourceIds).list(), Object.class)
                                                         .stream()
                                                         .collect(Collectors.toMap(object -> (Long) ((Object[]) object)[0], object -> (Integer) ((Object[]) object)[1]));
        return Pair.of(resources, resourceIds.stream().distinct().filter(resourceId -> !resources.containsValue(resourceId)).collect(Collectors.toList()));
    }

    /**
     * Returns a list of IDs for open requests in the specified project.
     *
     * @param projectId The ID of the project.
     *
     * @return A list of IDs for open requests in the specified project.
     */
    public List<Long> findResourceRequestIds(final String projectId) {
        return GenericUtils.convertToTypedList(getSession().getNamedQuery("findRequestsForProject").setString("projectId", projectId).list(), Long.class);
    }

    private List<ResourceSurveyRequest> findByObjectIdAndStatusAndOpen(final String property, final String objectId, final boolean openOnly, final List<ResourceSurveyRequest.Status> statuses) {
        final Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq(property, objectId));
        if (openOnly) {
            criteria.add(Restrictions.isNull("closingDate"));
        }
        switch (Optional.ofNullable(statuses).orElseGet(Collections::emptyList).size()) {
            case 0:
                if (log.isDebugEnabled()) {
                    log.debug("No statuses specified, returning {} requests where {} == {}", openOnly ? "all open" : "all", property, objectId);
                }
                break;
            case 1:
                if (log.isDebugEnabled()) {
                    log.debug("Returning {} requests with status {}  where {} == {}", openOnly ? "all open" : "all", statuses.get(0), property, objectId);
                }
                criteria.add(Restrictions.eq("rsnStatus", statuses.get(0)));
                break;
            default:
                if (log.isDebugEnabled()) {
                    log.debug("Returning {} requests with statuses {}  where {} == {}", openOnly ? "all open" : "all", statuses.stream().map(Objects::toString).collect(Collectors.joining(", ")), property, objectId);
                }
                criteria.add(Restrictions.in("rsnStatus", statuses));
        }
        return GenericUtils.convertToTypedList(criteria.list(), getParameterizedType());
    }
}
