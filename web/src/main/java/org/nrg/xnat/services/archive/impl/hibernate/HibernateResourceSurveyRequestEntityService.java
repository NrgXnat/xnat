package org.nrg.xnat.services.archive.impl.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.framework.generics.GenericUtils;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.DateUtils;
import org.nrg.xnat.entities.ResourceSurveyRequest;
import org.nrg.xnat.services.archive.ResourceSurveyRequestEntityService;
import org.nrg.xnat.services.archive.ResourceSurveyService;
import org.nrg.xnat.services.archive.impl.ResourceSurveyServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Hibernate-based implementation of the {@link ResourceSurveyRequestEntityService resource survey request entity
 * service}. For most purposes, you probably want the {@link ResourceSurveyService resource survey service} or {@link
 * ResourceSurveyServiceImpl its implementation}.
 */
@Service
@Transactional
@Slf4j
public class HibernateResourceSurveyRequestEntityService extends AbstractHibernateEntityService<ResourceSurveyRequest, ResourceSurveyRequestRepository> implements ResourceSurveyRequestEntityService {
    private static final String PARAM_IDS                                  = "ids";
    private static final String TEMPLATE_GENERATE_SURVEY_REQUESTS          =    "WITH requested_resources AS (SELECT ar.xnat_abstractresource_id                 AS resource_id, "
                                                                             + "                                    ar.label                                    AS resource_label, "
                                                                             + "                                    ar.xnat_imagescandata_xnat_imagescandata_id AS scan_id, "
                                                                             + "                                    ar.abstractresource_info "
                                                                             + "                             FROM xnat_abstractresource ar LEFT JOIN xnat_abstractresource_meta_data armd ON ar.abstractresource_info = armd.meta_data_id "
                                                                             + "                                      LEFT JOIN xnat_resource r ON ar.xnat_abstractresource_id = r.xnat_abstractresource_id "
                                                                             + "                                      LEFT JOIN xnat_imagescandata sc ON ar.xnat_imagescandata_xnat_imagescandata_id = sc.xnat_imagescandata_id "
                                                                             + "                                      LEFT JOIN xnat_experimentdata x ON sc.image_session_id = x.id "
                                                                             + "                             WHERE r.format = 'DICOM' "
                                                                             + "                               AND %s), "
                                                                             + "     open_requests AS (SELECT resource_id "
                                                                             + "                       FROM xhbm_resource_survey_request "
                                                                             + "                       WHERE closing_date IS NULL "
                                                                             + "                         AND resource_id IN (SELECT resource_id FROM requested_resources) "
                                                                             + "                       GROUP BY resource_id) "
                                                                             + "SELECT res.resource_id, "
                                                                             + "       res.resource_label, "
                                                                             + "       s.label                  AS subject_label, "
                                                                             + "       x.label                  AS experiment_label, "
                                                                             + "       sc.id                    AS scan_label, "
                                                                             + "       sc.series_description    AS scan_description, "
                                                                             + "       x.project                AS project_id, "
                                                                             + "       s.id                     AS subject_id, "
                                                                             + "       x.id                     AS experiment_id, "
                                                                             + "       e.element_name           AS xsi_type, "
                                                                             + "       sc.xnat_imagescandata_id AS scan_id, "
                                                                             + "       r.uri                    AS resource_uri, "
                                                                             + "       :" + PARAM_REQUESTER + "               AS requester, "
                                                                             + "       :" + PARAM_REQUEST_TIME + "             AS request_time "
                                                                             + "FROM requested_resources res "
                                                                             + "         LEFT JOIN open_requests orc ON res.resource_id = orc.resource_id "
                                                                             + "         LEFT JOIN xnat_resource r ON res.resource_id = r.xnat_abstractresource_id "
                                                                             + "         LEFT JOIN xnat_imagescandata sc ON res.scan_id = sc.xnat_imagescandata_id "
                                                                             + "         LEFT JOIN xnat_experimentdata x ON sc.image_session_id = x.id "
                                                                             + "         LEFT JOIN xdat_meta_element e ON x.extension = e.xdat_meta_element_id "
                                                                             + "         LEFT JOIN xnat_subjectassessordata sa ON x.id = sa.id "
                                                                             + "         LEFT JOIN xnat_subjectdata s ON sa.subject_id = s.id "
                                                                             + "WHERE res.resource_id NOT IN (SELECT resource_id FROM open_requests)";
    private static final String QUERY_GENERATE_PROJECT_SURVEY_REQUESTS     = String.format(TEMPLATE_GENERATE_SURVEY_REQUESTS, "coalesce(armd.last_modified, armd.insert_date) > :" + PARAM_SINCE_DATE + " AND x.project = :" + PARAM_PROJECT_ID);
    private static final String QUERY_GENERATE_RESOURCE_SURVEY_REQUEST     = String.format(TEMPLATE_GENERATE_SURVEY_REQUESTS, "ar.xnat_abstractresource_id IN (:" + PARAM_RESOURCE_IDS + ")");
    private static final String TEMPLATE_VERIFY_ID                         = "SELECT EXISTS(SELECT 1 FROM %1$s WHERE %2$s = :%3$s)";
    private static final String QUERY_VERIFY_REQUEST_ID                    = String.format(TEMPLATE_VERIFY_ID, "xhbm_resource_survey_request", "id", PARAM_REQUEST_ID);
    private static final String QUERY_VERIFY_RESOURCE_ID                   = String.format(TEMPLATE_VERIFY_ID, "xnat_abstractresource", "xnat_abstractresource_id", PARAM_RESOURCE_ID);
    private static final String QUERY_VERIFY_SESSION_ID                    = String.format(TEMPLATE_VERIFY_ID, "xnat_imagesessiondata", "id", PARAM_SESSION_ID);
    private static final String QUERY_VERIFY_REQUEST_TIME                  = String.format(TEMPLATE_VERIFY_ID, "xhbm_resource_survey_request", "request_time", PARAM_REQUEST_TIME);
    private static final String TEMPLATE_COUNT_IDS                         = "SELECT count(%1$s) FROM %2$s WHERE %1$s IN (:" + PARAM_IDS + ")";
    private static final String QUERY_COUNT_REQUEST_IDS                    = String.format(TEMPLATE_COUNT_IDS, "id", "xhbm_resource_survey_request");
    private static final String QUERY_COUNT_RESOURCE_IDS                   = String.format(TEMPLATE_COUNT_IDS, "xnat_abstractresource_id", "xnat_abstractresource");
    private static final String QUERY_GET_MISSING_REQUEST_IDS              = "SELECT * FROM (VALUES %s) AS request_ids(id) EXCEPT SELECT id FROM xhbm_resource_survey_request";
    private static final String QUERY_GET_MISSING_AND_INVALID_RESOURCE_IDS = "WITH resource_ids AS (SELECT id FROM (VALUES %s) AS resource_ids(id)), "
                                                                             + "     missing_ids AS (SELECT id FROM resource_ids EXCEPT SELECT xnat_abstractresource_id FROM xnat_abstractresource) "
                                                                             + "SELECT DISTINCT m.id, h.xnat_abstractresource_id FROM missing_ids m LEFT JOIN xnat_abstractresource_history h ON m.id = h.xnat_abstractresource_id";

    private final NamedParameterJdbcTemplate _template;

    @Autowired
    public HibernateResourceSurveyRequestEntityService(final NamedParameterJdbcTemplate template) {
        _template = template;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceSurveyRequest getRequest(final long requestId) throws NotFoundException {
        verifyRequestId(requestId);
        final ResourceSurveyRequest request = Optional.ofNullable(getDao().findById(requestId)).orElseThrow(() -> new NotFoundException(ResourceSurveyRequest.class.getSimpleName(), requestId));
        log.debug("Asked to retrieve resource survey request {}, found request for resource ID {}", requestId, request.getResourceId());
        return request;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceSurveyRequest> getRequests(final List<Long> requestIds) throws NotFoundException {
        verifyRequestIds(requestIds);
        return getDao().findByIds(requestIds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceSurveyRequest getRequestByResourceId(final int resourceId) throws NotFoundException {
        verifyResourceId(resourceId);
        return getDao().findByResourceId(resourceId).orElseThrow(() -> new NotFoundException("No resource survey request was found for resource ID " + resourceId));
    }

    @Override
    public List<ResourceSurveyRequest> getAllRequestsByResourceId(int resourceId) throws NotFoundException {
        verifyResourceId(resourceId);
        return getDao().findAllByResourceId(resourceId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceSurveyRequest> getRequestsByResourceIds(final List<Integer> resourceIds) throws NotFoundException {
        verifyResourceIds(resourceIds);
        return getDao().findByResourceIds(resourceIds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceSurveyRequest> getAllRequestsByProjectId(final String projectId) throws NotFoundException {
        verifyProjectId(projectId);
        return getDao().findAllByProjectId(projectId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceSurveyRequest> getOpenRequestsByProjectId(final String projectId) throws NotFoundException {
        verifyProjectId(projectId);
        return getDao().findOpenByProjectId(projectId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceSurveyRequest> getAllRequestsBySessionId(final String requestTime) throws NotFoundException {
        verifySessionIds(requestTime);
        return getDao().findAllBySessionId(requestTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceSurveyRequest> getOpenRequestsBySessionId(final String sessionId) throws NotFoundException {
        verifySessionIds(sessionId);
        return getDao().findOpenBySessionId(sessionId);
    }

    @Override
    public List<ResourceSurveyRequest> getAllRequestsByRequestTime(final String requestTime) throws NotFoundException {
        verifyRequestTime(requestTime);
        return getDao().findAllByRequestTime(requestTime);
    }

    @Override
    public List<ResourceSurveyRequest> getOpenRequestsByRequestTime(final String requestTime) throws NotFoundException {
        verifyRequestTime(requestTime);
        return getDao().findOpenByRequestTime(requestTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceSurveyRequest> getRequestsByProjectIdAndStatus(final String projectId, final ResourceSurveyRequest.Status... statuses) throws NotFoundException {
        return getRequestsByProjectIdAndStatus(projectId, Arrays.asList(statuses));
    }

    @Override
    public List<ResourceSurveyRequest> getRequestsByProjectIdAndStatus(String projectId, List<ResourceSurveyRequest.Status> statuses) throws NotFoundException {
        verifyProjectId(projectId);
        return getDao().findByProjectIdAndStatus(projectId, statuses);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceSurveyRequest getOrCreateRequestByResourceId(final UserI requester, final int resourceId) throws NotFoundException {
        final List<ResourceSurveyRequest> requests = getOrCreateRequestsByResourceId(requester, Collections.singletonList(resourceId));
        if (CollectionUtils.isEmpty(requests)) {
            return null;
        }
        if (requests.size() == 1) {
            return requests.get(0);
        }
        log.warn("User {} requested resource survey requests for resource {}, was expecting only one but found {} so returning the newest one.", requester.getUsername(), resourceId, requests.size());
        return requests.stream().max(ResourceSurveyRequest.REQUESTS_BY_DATE).orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceSurveyRequest> getOrCreateRequestsByResourceId(final UserI requester, final List<Integer> resourceIds) throws NotFoundException {
        verifyResourceIds(resourceIds);

        // Get all existing requests with status CREATED corresponding to the submitted resource IDs...
        final List<ResourceSurveyRequest> requests = getRequestsByResourceIds(resourceIds).stream().filter(request -> request.getRsnStatus() == ResourceSurveyRequest.Status.CREATED).collect(Collectors.toList());
        // Create requests for submitted resource IDs that aren't represented in the existing requests
        requests.addAll(create(requester, GenericUtils.convertToTypedList(CollectionUtils.subtract(resourceIds, requests.stream().map(ResourceSurveyRequest::getResourceId).collect(Collectors.toSet())), Integer.class)));
        return requests;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceSurveyRequest create(final UserI requester, final int resourceId) throws NotFoundException {
        verifyResourceId(resourceId);
        final ResourceSurveyRequest request = create(requester, Collections.singletonList(resourceId)).get(0);
        log.debug("User {} created resource survey request {} for resource {}", requester.getUsername(), request.getId(), resourceId);
        return request;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceSurveyRequest> create(final UserI requester, final List<Integer> resourceIds) throws NotFoundException {
        if (resourceIds.isEmpty()) {
            return Collections.emptyList();
        }

        verifyResourceIds(resourceIds);

        final String timestamp = DateUtils.getMsTimestamp();
        final List<ResourceSurveyRequest> requests = _template.query(QUERY_GENERATE_RESOURCE_SURVEY_REQUEST,
                                                                     new MapSqlParameterSource(PARAM_RESOURCE_IDS, resourceIds)
                                                                             .addValue(PARAM_REQUESTER, requester.getUsername())
                                                                             .addValue(PARAM_REQUEST_TIME, timestamp),
                                                                     ResourceSurveyRequest.ROW_MAPPER)
                                                              .stream()
                                                              .map(this::create)
                                                              .collect(Collectors.toList());

        // Check for resource IDs that didn't have requests generated: there's probably an open resource survey request for missing ones
        if (resourceIds.size() != requests.size()) {
            final List<Integer> missing = requests.stream().map(ResourceSurveyRequest::getResourceId).filter(resourceId -> !resourceIds.contains(resourceId)).collect(Collectors.toList());
            log.info("User {} asked to create survey requests for {} resources, but includes {} resource IDs that appear to be associated with resource survey requests that are currently open: {}", requester.getUsername(), resourceIds.size(), missing.size(), missing.stream().sorted().map(Object::toString).collect(Collectors.joining(", ")));
        }
        if (log.isDebugEnabled()) {
            log.debug("Created {} resource survey requests with timestamp {} for resources {}", requests.size(), timestamp, resourceIds.stream().map(Objects::toString).collect(Collectors.joining(", ")));
        }
        return requests;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceSurveyRequest> create(final UserI requester, final String projectId) throws NotFoundException {
        return create(requester, projectId, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceSurveyRequest> create(final UserI requester, final String projectId, final LocalDateTime since) throws NotFoundException {
        log.debug("User {} wants to create new resource survey requests for project {}", requester.getUsername(), projectId);

        final String timestamp = DateUtils.getMsTimestamp();
        final List<ResourceSurveyRequest> requests = _template.query(QUERY_GENERATE_PROJECT_SURVEY_REQUESTS,
                                                                     new MapSqlParameterSource(PARAM_PROJECT_ID, projectId)
                                                                             .addValue(PARAM_REQUESTER, requester.getUsername())
                                                                             .addValue(PARAM_REQUEST_TIME, timestamp)
                                                                             .addValue(PARAM_SINCE_DATE, Optional.ofNullable(since).orElse(LocalDateTime.MIN)),
                                                                     ResourceSurveyRequest.ROW_MAPPER)
                                                              .stream()
                                                              .map(this::create)
                                                              .collect(Collectors.toList());

        log.debug("Created {} resource survey requests with timestamp {} for project {}", requests.size(), timestamp, projectId);
        return requests;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pair<List<String>, List<Long>> resolveRequestProjects(final List<Long> requestIds) {
        return getDao().findRequestProjects(requestIds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pair<Map<Long, Integer>, List<Integer>> resolveResourceRequestIds(final List<Integer> resourceIds) {
        return getDao().findResourceRequestIds(resourceIds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Long> resolveProjectRequestIds(final String projectId) throws NotFoundException {
        verifyProjectId(projectId);
        return getDao().findResourceRequestIds(projectId);
    }

    private void verifyProjectId(final String projectId) throws NotFoundException {
        if (!Permissions.verifyProjectExists(_template, projectId)) {
            throw new NotFoundException(XnatProjectdata.SCHEMA_ELEMENT_NAME, projectId);
        }
    }

    private void verifySessionIds(final String sessionId) throws NotFoundException {
        if (!_template.queryForObject(QUERY_VERIFY_SESSION_ID, new MapSqlParameterSource(PARAM_SESSION_ID, sessionId), Boolean.class)) {
            throw new NotFoundException(XnatImagesessiondata.SCHEMA_ELEMENT_NAME, sessionId);
        }
    }

    private void verifyRequestTime(final String requestTime) throws NotFoundException {
        if (!_template.queryForObject(QUERY_VERIFY_REQUEST_TIME, new MapSqlParameterSource(PARAM_REQUEST_TIME, requestTime), Boolean.class)) {
            throw new NotFoundException("Got request for resource survey request(s) with request time of " + requestTime + " but no requests have that request time");
        }
    }

    private void verifyRequestIds(final List<Long> requestIds) throws NotFoundException {
        if (validateIds(QUERY_COUNT_REQUEST_IDS, requestIds)) {
            return;
        }
        final List<Long> missingIds = _template.queryForList(getMissingIdsQuery(QUERY_GET_MISSING_REQUEST_IDS, requestIds), EmptySqlParameterSource.INSTANCE, Long.class);
        if (!missingIds.isEmpty()) {
            throw new NotFoundException("Multiple resource survey request IDs were provided, but " + missingIds.size() + " of them don't exist: " + missingIds.stream().sorted().map(Objects::toString).collect(Collectors.joining(", ")));
        }
    }

    private void verifyResourceIds(final List<Integer> resourceIds) throws NotFoundException {
        if (validateIds(QUERY_COUNT_RESOURCE_IDS, resourceIds)) {
            return;
        }
        final List<Integer> missingIds = new ArrayList<>();
        final List<Integer> invalidIds = new ArrayList<>();
        _template.query(getMissingIdsQuery(QUERY_GET_MISSING_AND_INVALID_RESOURCE_IDS, resourceIds), results -> {
            final Integer historyId = results.getObject("xnat_abstractresource_id", Integer.class);
            if (Objects.isNull(historyId)) {
                invalidIds.add(results.getInt("id"));
            } else {
                missingIds.add(historyId);
            }
        });
        if (invalidIds.isEmpty()) {
            log.info("Got a request to verify {} resource IDs: all were valid but {} were associated with deleted resources", resourceIds.size(), missingIds.size());
            return;
        }
        throw new NotFoundException("Multiple unacceptable resource IDs were provided:\n"
                                    + missingIds.size() + " of them are for deleted resources:\n * " + missingIds.stream().sorted().map(Objects::toString).collect(Collectors.joining("\n * ")) + "\n"
                                    + invalidIds.size() + " of them are invalid (not associated with any existing or deleted resources):\n * " + invalidIds.stream().sorted().map(Objects::toString).collect(Collectors.joining("\n * ")));
    }

    private void verifyRequestId(final long requestId) throws NotFoundException {
        verifyId(QUERY_VERIFY_REQUEST_ID, PARAM_REQUEST_ID, ResourceSurveyRequest.class.getSimpleName(), requestId);
    }

    private void verifyResourceId(final int resourceId) throws NotFoundException {
        verifyId(QUERY_VERIFY_RESOURCE_ID, PARAM_RESOURCE_ID, XnatAbstractresource.SCHEMA_ELEMENT_NAME, resourceId);
    }

    private boolean validateIds(final String countQuery, final List<? extends Number> ids) {
        return _template.queryForObject(countQuery, new MapSqlParameterSource(PARAM_IDS, ids), Integer.class) == ids.size();
    }

    private void verifyId(final String query, final String parameterName, final String type, final long id) throws NotFoundException {
        if (!_template.queryForObject(query, new MapSqlParameterSource(parameterName, id), Boolean.class)) {
            throw new NotFoundException(type, id);
        }
    }

    private static String getMissingIdsQuery(final String query, final List<? extends Number> ids) {
        return String.format(query, ids.stream().map(id -> "(" + id + ")").collect(Collectors.joining(", ")));
    }
}
