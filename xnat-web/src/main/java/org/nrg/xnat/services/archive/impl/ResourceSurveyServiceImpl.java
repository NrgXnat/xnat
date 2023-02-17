package org.nrg.xnat.services.archive.impl;

import com.google.common.collect.ArrayListMultimap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.dcm4che2.io.StopTagInputHandler;
import org.jetbrains.annotations.NotNull;
import org.nrg.action.ServerException;
import org.nrg.dcm.DicomFileNamer;
import org.nrg.dicomtools.utilities.DicomUtils;
import org.nrg.framework.generics.GenericUtils;
import org.nrg.framework.services.SerializerService;
import org.nrg.xapi.exceptions.ConflictedStateException;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NoContentException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.entities.ResourceSurveyRequest;
import org.nrg.xnat.services.archive.ResourceMitigationReport;
import org.nrg.xnat.services.archive.ResourceSurveyReport;
import org.nrg.xnat.services.archive.ResourceSurveyRequestEntityService;
import org.nrg.xnat.services.archive.ResourceSurveyService;
import org.nrg.xnat.services.archive.impl.hibernate.ResourceMitigationHelper;
import org.nrg.xnat.services.archive.impl.hibernate.ResourceSurveyHelper;
import org.nrg.xnat.utils.WorkflowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ResourceSurveyServiceImpl implements ResourceSurveyService {
    private static final String CSV_HEADER     = "projectId,subjectId,experimentId,scanId,resourceId,resourceLabel,resourceUri,requestStatus,surveyDate,totalEntries,totalUids,totalDuplicates,totalBadFiles,totalMismatchedFiles";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final  String QUEUED         = "queued";
    public static final  String OK             = "ok";
    public static final  String FORBIDDEN      = "forbidden";
    public static final  String INVALID        = "invalid";
    public static final  String MISSING        = "missing";
    public static final  String ERRORS         = "errors";

    private static final String QUERY_VALIDATE_RESOURCE_IDS = "SELECT xnat_abstractresource_id "
                                                              + "FROM xnat_abstractresource "
                                                              + "WHERE xnat_abstractresource_id IN (:" + ResourceSurveyRequestEntityService.PARAM_RESOURCE_IDS + ") "
                                                              + "ORDER BY xnat_abstractresource_id";
    private static final String QUERY_GET_RESOURCE_PROJECT  = "SELECT x.project "
                                                              + "FROM xnat_abstractresource ar "
                                                              + "         JOIN xnat_imagescandata s ON ar.xnat_imagescandata_xnat_imagescandata_id = s.xnat_imagescandata_id "
                                                              + "         JOIN xnat_experimentdata x ON s.image_session_id = x.id "
                                                              + "WHERE ar.xnat_abstractresource_id = :" + ResourceSurveyRequestEntityService.PARAM_RESOURCE_ID;
    private static final String QUERY_GET_RESOURCE_PROJECTS = "SELECT DISTINCT x.project "
                                                              + "FROM xnat_abstractresource ar "
                                                              + "         JOIN xnat_imagescandata s ON ar.xnat_imagescandata_xnat_imagescandata_id = s.xnat_imagescandata_id "
                                                              + "         JOIN xnat_experimentdata x ON s.image_session_id = x.id "
                                                              + "WHERE ar.xnat_abstractresource_id IN (:" + ResourceSurveyRequestEntityService.PARAM_RESOURCE_IDS + ")";

    private final ResourceSurveyRequestEntityService _entityService;
    private final SerializerService                  _serializer;
    private final DicomFileNamer                     _dicomFileNamer;
    private final StopTagInputHandler                _stopTagInputHandler;
    private final SiteConfigPreferences              _preferences;
    private final NamedParameterJdbcTemplate         _jdbcTemplate;
    private final JmsTemplate                        _jmsTemplate;

    @Autowired
    public ResourceSurveyServiceImpl(final ResourceSurveyRequestEntityService entityService, final SerializerService serializer, final DicomFileNamer dicomFileNamer, final SiteConfigPreferences preferences, final NamedParameterJdbcTemplate jdbcTemplate, final JmsTemplate jmsTemplate) {
        log.debug("Initializing the resource survey service");
        _entityService       = entityService;
        _serializer          = serializer;
        _dicomFileNamer      = dicomFileNamer;
        _stopTagInputHandler = DicomUtils.getMaxStopTagInputHandler();
        _preferences         = preferences;
        _jdbcTemplate        = jdbcTemplate;
        _jmsTemplate         = jmsTemplate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceSurveyRequest getById(final UserI requester, final long requestId) throws NotFoundException, InsufficientPrivilegesException {
        final ResourceSurveyRequest request = _entityService.getRequest(requestId);
        validateResourceAccess(requester, request.getResourceId());
        log.debug("User {} requested resource survey request {} for resource {}", requester.getUsername(), request.getId(), request);
        return request;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceSurveyRequest> getAllByResourceId(final UserI requester, final int resourceId) throws NotFoundException, InsufficientPrivilegesException {
        validateResourceAccess(requester, resourceId);
        final List<ResourceSurveyRequest> requests = _entityService.getAllRequestsByResourceId(resourceId);
        if (log.isTraceEnabled()) {
            log.trace("User {} requested resource survey requests for resource {} and got {} instances: {}", requester.getUsername(), resourceId, requests.size(), requests.stream().map(ResourceSurveyRequest::getId).map(Objects::toString).collect(Collectors.joining(", ")));
        } else {
            log.debug("User {} requested resource survey requests for resource {} and got {} instances", requester.getUsername(), resourceId, requests.size());
        }
        return requests;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceSurveyRequest getByResourceId(final UserI requester, final int resourceId) throws NotFoundException, InsufficientPrivilegesException {
        validateResourceAccess(requester, resourceId);
        final ResourceSurveyRequest request = _entityService.getRequestByResourceId(resourceId);
        log.debug("User {} requested resource survey request {} for resource {}", requester.getUsername(), request.getId(), resourceId);
        return request;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceSurveyRequest> getAllByProjectId(final UserI requester, final String projectId) throws InsufficientPrivilegesException, NotFoundException {
        validateProjectAccess(requester, projectId);
        final List<ResourceSurveyRequest> requests = _entityService.getAllRequestsByProjectId(projectId);
        if (log.isTraceEnabled()) {
            log.trace("User {} requested all resource survey requests for project {} and got {} instances: {}", requester.getUsername(), projectId, requests.size(), requests.stream().map(ResourceSurveyRequest::getId).map(Objects::toString).collect(Collectors.joining(", ")));
        } else {
            log.debug("User {} requested all resource survey requests for project {} and got {} instances", requester.getUsername(), projectId, requests.size());
        }
        return requests;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceSurveyRequest> getOpenByProjectId(final UserI requester, final String projectId) throws InsufficientPrivilegesException, NotFoundException {
        validateProjectAccess(requester, projectId);
        final List<ResourceSurveyRequest> requests = _entityService.getOpenRequestsByProjectId(projectId);
        if (log.isTraceEnabled()) {
            log.trace("User {} requested all open resource survey requests for project {} and got {} instances: {}", requester.getUsername(), projectId, requests.size(), requests.stream().map(ResourceSurveyRequest::getId).map(Objects::toString).collect(Collectors.joining(", ")));
        } else {
            log.debug("User {} requested all open resource survey requests for project {} and got {} instances", requester.getUsername(), projectId, requests.size());
        }
        return requests;
    }

    @Override
    public List<ResourceSurveyRequest> getAllBySessionId(final UserI requester, final String sessionId) throws InsufficientPrivilegesException, NotFoundException {
        validateSessionAccess(requester, sessionId);
        final List<ResourceSurveyRequest> requests = _entityService.getAllRequestsBySessionId(sessionId);
        if (log.isTraceEnabled()) {
            log.trace("User {} requested all resource survey requests for session {} and got {} instances: {}", requester.getUsername(), sessionId, requests.size(), requests.stream().map(ResourceSurveyRequest::getId).map(Objects::toString).collect(Collectors.joining(", ")));
        } else {
            log.debug("User {} requested all resource survey requests for session {} and got {} instances", requester.getUsername(), sessionId, requests.size());
        }
        return requests;
    }

    @Override
    public List<ResourceSurveyRequest> getOpenBySessionId(final UserI requester, final String sessionId) throws InsufficientPrivilegesException, NotFoundException {
        validateSessionAccess(requester, sessionId);
        final List<ResourceSurveyRequest> requests = _entityService.getOpenRequestsBySessionId(sessionId);
        if (log.isTraceEnabled()) {
            log.trace("User {} requested all open resource survey requests for session {} and got {} instances: {}", requester.getUsername(), sessionId, requests.size(), requests.stream().map(ResourceSurveyRequest::getId).map(Objects::toString).collect(Collectors.joining(", ")));
        } else {
            log.debug("User {} requested all open resource survey requests for session {} and got {} instances", requester.getUsername(), sessionId, requests.size());
        }
        return requests;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceSurveyRequest> getByProjectIdAndStatus(final UserI requester, final String projectId, final ResourceSurveyRequest.Status... statuses) throws InsufficientPrivilegesException, NotFoundException {
        return getByProjectIdAndStatus(requester, projectId, Arrays.asList(statuses));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceSurveyRequest> getByProjectIdAndStatus(final UserI requester, final String projectId, final List<ResourceSurveyRequest.Status> statuses) throws InsufficientPrivilegesException, NotFoundException {
        validateProjectAccess(requester, projectId);
        final List<ResourceSurveyRequest> requests = _entityService.getRequestsByProjectIdAndStatus(projectId, statuses);
        if (log.isTraceEnabled()) {
            log.trace("User {} requested resource survey requests for project {} with status(es) \"{}\" and got {} instances: {}", requester.getUsername(), projectId, statuses.stream().map(Objects::toString).collect(Collectors.joining("\", \"")), requests.size(), requests.stream().map(ResourceSurveyRequest::getId).map(Objects::toString).collect(Collectors.joining(", ")));
        } else {
            log.debug("User {} requested resource survey requests for project {} with status(es) \"{}\" and got {} instances", requester.getUsername(), projectId, statuses.stream().map(Objects::toString).collect(Collectors.joining("\", \"")), requests.size());
        }
        return requests;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long queueResourceSurvey(final UserI requester, final int resourceId) throws NotFoundException, ConflictedStateException, InsufficientPrivilegesException {
        return queueResourceSurvey(requester, resourceId, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long queueResourceSurvey(final UserI requester, final int resourceId, final String reason, final String comment) throws NotFoundException, ConflictedStateException, InsufficientPrivilegesException {
        validateResourceAccess(requester, resourceId);
        return queueSurveyRequest(requester, _entityService.getOrCreateRequestByResourceId(requester, resourceId), reason, comment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Collection<Long>> queueResourceSurveys(final UserI requester, final List<Integer> resourceIds) throws NotFoundException {
        return queueResourceSurveys(requester, resourceIds, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Collection<Long>> queueResourceSurveys(final UserI requester, final List<Integer> resourceIds, final String reason, final String comment) throws NotFoundException {
        return queueSurveyRequests(requester, _entityService.getRequestsByResourceIds(resourceIds), reason, comment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Long> queueProjectSurveys(final UserI requester, final String projectId) throws NotFoundException, InsufficientPrivilegesException {
        return queueProjectSurveys(requester, projectId, null, null, null);
    }

    @Override
    public List<Long> queueProjectSurveys(final UserI requester, final String projectId, final LocalDateTime since) throws NotFoundException, InsufficientPrivilegesException {
        return queueProjectSurveys(requester, projectId, since, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Long> queueProjectSurveys(final UserI requester, final String projectId, final String reason, final String comment) throws NotFoundException, InsufficientPrivilegesException {
        return queueProjectSurveys(requester, projectId, null, reason, comment);
    }

    @Override
    public List<Long> queueProjectSurveys(final UserI requester, final String projectId, final LocalDateTime since, final String reason, final String comment) throws NotFoundException, InsufficientPrivilegesException {
        validateProjectAccess(requester, projectId);

        final List<ResourceSurveyRequest> requests = _entityService.create(requester, projectId, since);
        log.debug("User {} wants to survey resources in project {}, so created {} requests", requester.getUsername(), projectId, requests.size());

        final Map<String, Collection<Long>> results = queueSurveyRequests(requester, requests, reason, comment);
        return results.containsKey(QUEUED) ? new ArrayList<>(results.get(QUEUED)) : Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long queueSurvey(final UserI requester, final long requestId) throws NotFoundException, ConflictedStateException, InsufficientPrivilegesException {
        return queueSurvey(requester, requestId, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long queueSurvey(final UserI requester, final long requestId, final String reason, final String comment) throws NotFoundException, ConflictedStateException, InsufficientPrivilegesException {
        return queueSurveyRequest(requester, _entityService.getRequest(requestId), reason, comment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Collection<Long>> queueSurveys(final UserI requester, final List<Long> requestIds) throws NotFoundException {
        return queueSurveys(requester, requestIds, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Collection<Long>> queueSurveys(final UserI requester, final List<Long> requestIds, final String reason, final String comment) throws NotFoundException {
        return queueSurveyRequests(requester, _entityService.getRequests(requestIds), reason, comment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceSurveyRequest surveyResource(final ResourceSurveyRequest request) throws NotFoundException, ConflictedStateException, InsufficientPrivilegesException {
        // TODO: This should really be implemented as an aspect, similar to XapiRequestMappingAspect.
        validateResourceAccess(getRequester(request), request.getResourceId());

        log.debug("User {} wants to survey the resource {} so created new resource survey request {}", request.getRequester(), request.getResourceId(), request.getId());
        return runSurveyForRequest(request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceSurveyRequest> surveyResources(final UserI requester, final String projectId) throws InsufficientPrivilegesException, NotFoundException {
        // TODO: This should really be implemented as an aspect, similar to XapiRequestMappingAspect.
        validateProjectAccess(requester, projectId);

        final List<ResourceSurveyRequest> requests = getByProjectIdAndStatus(requester, projectId, ResourceSurveyRequest.Status.QUEUED_FOR_SURVEY);
        log.info("Got a request from user {} to queue resource survey on project {}, found {} created requests for that project", requester.getUsername(), projectId, requests.size());

        final Map<ResourceSurveyRequest, ConflictedStateException> exceptions = new HashMap<>();
        try {
            return requests.stream().map(request -> {
                               try {
                                   return runSurveyForRequest(request);
                               } catch (ConflictedStateException e) {
                                   exceptions.put(request, e);
                                   return null;
                               }
                           })
                           .filter(Objects::nonNull)
                           .collect(Collectors.toList());
        } finally {
            if (!exceptions.isEmpty()) {
                log.error("{} errors occurred trying to queue mitigation requests for project {}", exceptions.size(), projectId);
                exceptions.forEach((request, e) -> log.error("Resource ID {} for subject {} and experiment {} (ID: {}), resource URI: {}", request.getResourceId(), request.getSubjectLabel(), request.getExperimentLabel(), request.getExperimentId(), request.getResourceUri(), e));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, ResourceSurveyRequest.Status> getRequestStatus(final UserI requester, final String projectId) throws InsufficientPrivilegesException, NotFoundException {
        validateProjectAccess(requester, projectId);
        return _entityService.getOpenRequestsByProjectId(projectId).stream().collect(toSortedMap(ResourceSurveyRequest::getResourceId, ResourceSurveyRequest::getRsnStatus));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceSurveyRequest.Status getRequestStatus(final UserI requester, final int resourceId) throws InsufficientPrivilegesException, NotFoundException {
        validateResourceAccess(requester, resourceId);
        return Optional.ofNullable(_entityService.getRequestByResourceId(resourceId)).orElseThrow(() -> new NotFoundException("No resource survey request found for resource ID " + resourceId)).getRsnStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, ResourceSurveyRequest.Status> getRequestStatus(final UserI requester, final List<Integer> resourceIds) throws InsufficientPrivilegesException, NotFoundException {
        validateResourceAccess(requester, resourceIds);
        return _entityService.getRequestsByResourceIds(resourceIds).stream().collect(toSortedMap(ResourceSurveyRequest::getResourceId, ResourceSurveyRequest::getRsnStatus));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceSurveyRequest.Status getRequestStatus(final UserI requester, final long requestId) throws InsufficientPrivilegesException, NotFoundException {
        validateRequestAccess(requester, requestId);
        return Optional.ofNullable(_entityService.getRequest(requestId)).orElseThrow(() -> new NotFoundException("No resource survey request found for request ID " + requestId)).getRsnStatus();
    }

    @Override
    public List<ResourceSurveyRequest> getAllResourceSurveyRequests(final UserI requester) throws InsufficientPrivilegesException, InitializationException {
        if (!Optional.ofNullable(Roles.getRoleService()).orElseThrow(() -> new InitializationException("No instance of the RoleService available")).isSiteAdmin(requester)) {
            throw new InsufficientPrivilegesException("Only the Site admin can get all the survey request");
        }
        return _entityService.getAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long queueResourceMitigation(final UserI requester, final int resourceId) throws NotFoundException, ConflictedStateException, InsufficientPrivilegesException {
        return queueResourceMitigation(requester, resourceId, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long queueResourceMitigation(final UserI requester, final int resourceId, final String reason, final String comment) throws NotFoundException, ConflictedStateException, InsufficientPrivilegesException {
        validateResourceAccess(requester, resourceId);
        return queueMitigationRequest(requester, _entityService.getRequestByResourceId(resourceId), reason, comment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Collection<Long>> queueResourceMitigation(final UserI requester, final List<Integer> resourceIds) throws NotFoundException {
        return queueResourceMitigation(requester, resourceIds, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Collection<Long>> queueResourceMitigation(final UserI requester, final List<Integer> resourceIds, final String reason, final String comment) throws NotFoundException {
        return queueMitigationRequests(requester, _entityService.getRequestsByResourceIds(resourceIds), reason, comment);
    }

    @Override
    public List<Long> queueProjectMitigation(final UserI requester, final String projectId) throws NotFoundException, InsufficientPrivilegesException {
        return queueProjectMitigation(requester, projectId, null, null);
    }

    @Override
    public List<Long> queueProjectMitigation(final UserI requester, final String projectId, final String reason, final String comment) throws NotFoundException, InsufficientPrivilegesException {
        validateProjectAccess(requester, projectId);
        final List<ResourceSurveyRequest> requests = getByProjectIdAndStatus(requester, projectId, ResourceSurveyRequest.Status.DIVERGENT);

        log.info("Got a request from user {} to queue resource mitigation on project {}, found {} divergent requests for that project", requester.getUsername(), projectId, requests.size());
        final Map<String, Collection<Long>> results = queueMitigationRequests(requester, requests, reason, comment);
        return results.containsKey(QUEUED) ? results.get(QUEUED).stream().sorted().collect(Collectors.toList()) : Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long queueMitigation(final UserI requester, final long requestId) throws NotFoundException, ConflictedStateException, InsufficientPrivilegesException {
        return queueMitigation(requester, requestId, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long queueMitigation(final UserI requester, final long requestId, final String reason, final String comment) throws NotFoundException, ConflictedStateException, InsufficientPrivilegesException {
        return queueMitigationRequest(requester, _entityService.getRequest(requestId), reason, comment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Collection<Long>> queueMitigations(final UserI requester, final List<Long> requestIds) throws NotFoundException {
        return queueMitigations(requester, requestIds, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Collection<Long>> queueMitigations(final UserI requester, final List<Long> requestIds, final String reason, final String comment) throws NotFoundException {
        return queueMitigationRequests(requester, _entityService.getRequests(requestIds), reason, comment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mitigateResource(final ResourceSurveyRequest request) throws NotFoundException, InsufficientPrivilegesException, InitializationException {
        final UserI requester = getRequester(request);
        validateResourceAccess(requester, request.getResourceId());

        if (isInvalidStatus(request, ResourceSurveyRequest.Status.QUEUED_FOR_MITIGATION)) {
            if (request.getRsnStatus() == ResourceSurveyRequest.Status.CREATED) {
                log.info("Got request from user {} to mitigate resource survey request {} for resource {} but the status for that request is \"CREATED\": you should survey this resource first", requester.getUsername(), request.getId(), request.getResourceId());
            } else {
                log.info("Got request from user {} to mitigate resource survey request {} for resource {} but the status for that request is {} (should be \"QUEUED_FOR_MITIGATION\")", requester.getUsername(), request.getId(), request.getResourceId(), request.getRsnStatus());
            }
            return;
        }

        log.debug("Got request from user {} to mitigate resource survey request {} for resource {}", requester.getUsername(), request.getId(), request.getResourceId());
        final WrkWorkflowdata workflow = WrkWorkflowdata.getWrkWorkflowdatasByWrkWorkflowdataId(request.getWorkflowId(), requester, false);
        setStatus(request, workflow, ResourceSurveyRequest.Status.MITIGATING);

        final ResourceMitigationHelper helper = new ResourceMitigationHelper(request, workflow, requester, _preferences);
        final ResourceMitigationReport report = helper.call();
        request.setMitigationReport(report);
        ResourceSurveyRequest.Status status = StringUtils.isNotBlank(report.getCatalogWriteError()) || StringUtils.isNotBlank(report.getResourceSaveError()) || report.getTotalFileErrors() > 0 ? ResourceSurveyRequest.Status.ERROR : ResourceSurveyRequest.Status.CONFORMING;
        setStatus(request, workflow, status);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long cancelRequest(final UserI requester, final long requestId) throws InsufficientPrivilegesException, NotFoundException {
        final List<Long> results = cancelRequests(requester, Collections.singletonList(requestId));
        if (results.isEmpty()) {
            throw new NotFoundException("The specified ID does not exist or the request can not be canceled.");
        }
        return results.get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Long> cancelRequests(final UserI requester, final List<Long> requestIds) throws InsufficientPrivilegesException, NotFoundException {
        validateRequestAccess(requester, requestIds);
        return _entityService.getRequests(requestIds).stream()
                             .map(request -> {
                                 if (!request.isOpen()) {
                                     return 0L;
                                 }
                                 request.setRsnStatus(ResourceSurveyRequest.Status.CANCELED);
                                 _entityService.update(request);
                                 return request.getId();
                             })
                             .filter(requestId -> requestId != 0)
                             .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long cancelRequestByResourceId(final UserI requester, final int resourceId) throws InsufficientPrivilegesException, NotFoundException {
        validateResourceAccess(requester, resourceId);
        return cancelRequest(requester, _entityService.getRequestByResourceId(resourceId).getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, Long> cancelRequestsByResourceId(final UserI requester, final List<Integer> resourceIds) throws InsufficientPrivilegesException, NotFoundException {
        validateResourceAccess(requester, resourceIds);
        final Pair<Map<Long, Integer>, List<Integer>> resolved = _entityService.resolveResourceRequestIds(resourceIds);
        return cancelRequests(requester, new ArrayList<>(resolved.getKey().keySet())).stream().collect(toSortedMap(requestId -> resolved.getKey().get(requestId), Function.identity()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Long> cancelRequestsByProjectId(final UserI requester, final String projectId) throws InsufficientPrivilegesException, NotFoundException {
        validateProjectAccess(requester, projectId);
        return cancelRequests(requester, _entityService.resolveProjectRequestIds(projectId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanResourceReports(final UserI requester, final int resourceId) throws InsufficientPrivilegesException, NotFoundException {
        validateResourceAccess(requester, resourceId);
        _entityService.getAllRequestsByResourceId(resourceId).forEach(this::cleanReports);
    }

    @Override
    public void cleanRequestReports(final UserI requester, final long requestId) throws InsufficientPrivilegesException, NotFoundException {
        validateRequestAccess(requester, requestId);
        cleanReports(_entityService.getRequest(requestId));
    }

    @NotNull
    @Override
    public String convertRequestsToCsv(List<ResourceSurveyRequest> requests) {
        return CSV_HEADER + LINE_SEPARATOR +
               requests.stream().map(this::requestToCsvRow)
                       .collect(Collectors.joining(LINE_SEPARATOR));
    }

    @Override
    public List<Integer> getResourceIds(MultipartFile csv) throws NoContentException, DataFormatException, ServerException {
        List<Integer> resourceIds = parseCsvFile(csv);
        if (resourceIds.isEmpty()) {
            throw new NoContentException("There was no csv provided with the request.");
        }
        return resourceIds;
    }

    private ResourceMitigationReport getMitigationReportWithoutDetails(final ResourceMitigationReport mitigationReport) {
        return new ResourceMitigationReport(
                mitigationReport.getResourceSurveyRequestId(),
                mitigationReport.getCachePath(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                "",
                "",
                mitigationReport.getTotalMovedFiles(),
                mitigationReport.getTotalRemovedFiles(),
                mitigationReport.getTotalFileErrors());
    }

    private void cleanReports(final ResourceSurveyRequest request) {
        boolean              isDirty      = false;
        ResourceSurveyReport surveyReport = request.getSurveyReport();
        if (surveyReport != null) {
            request.setSurveyReport(getSurveyReportWithoutDetails(surveyReport));
            isDirty = true;
        }
        ResourceMitigationReport mitigationReport = request.getMitigationReport();
        if (mitigationReport != null) {
            request.setMitigationReport(getMitigationReportWithoutDetails(mitigationReport));
            isDirty = true;
        }
        if (isDirty) {
            _entityService.update(request);
        }
    }

    private ResourceSurveyReport getSurveyReportWithoutDetails(final ResourceSurveyReport surveyReport) {
        return new ResourceSurveyReport(
                surveyReport.getResourceSurveyRequestId(),
                surveyReport.getSurveyDate(),
                surveyReport.getTotalEntries(),
                surveyReport.getTotalUids(),
                surveyReport.getTotalBadFiles(),
                surveyReport.getTotalMismatchedFiles(),
                surveyReport.getTotalDuplicates(),
                Collections.emptyMap(),
                Collections.emptyList(),
                Collections.emptyMap(),
                Collections.emptyMap());
    }

    private long queueSurveyRequest(final UserI requester, final ResourceSurveyRequest request, final String reason, final String comment) throws ConflictedStateException, InsufficientPrivilegesException, NotFoundException {
        return translateFromResultsMap(requester, request, "survey", queueSurveyRequests(requester, Collections.singletonList(request), reason, comment));
    }

    private Map<String, Collection<Long>> queueSurveyRequests(final UserI requester, final List<ResourceSurveyRequest> requests, final String reason, final String comment) {
        log.debug("Queuing {} requests for survey at the request of user {} for reason of \"{}\": {}", requests.size(), requester.getUsername(), reason, comment);

        return processRequests(requester, requests, "survey", ResourceSurveyRequest.Status.CREATED, request -> {
            request.setRsnStatus(ResourceSurveyRequest.Status.QUEUED_FOR_SURVEY);
            _entityService.update(request);
            return true;
        });
    }

    private long queueMitigationRequest(final UserI requester, final ResourceSurveyRequest request, final String reason, final String comment) throws ConflictedStateException, InsufficientPrivilegesException, NotFoundException {
        return translateFromResultsMap(requester, request, "mitigation", queueMitigationRequests(requester, Collections.singletonList(request), reason, comment));
    }

    private Map<String, Collection<Long>> queueMitigationRequests(final UserI requester, final List<ResourceSurveyRequest> requests, final String reason, final String comment) {
        log.debug("Queuing {} requests for mitigation at the request of user {} for reason of \"{}\": {}", requests.size(), requester.getUsername(), reason, comment);

        return processRequests(requester, requests, "mitigation", ResourceSurveyRequest.Status.DIVERGENT, request -> {
            try {
                final PersistentWorkflowI workflow = buildMitigationWorkflow(requester, request, reason, comment);
                log.debug("Created mitigation workflow {} for resource survey request {} for resource {}", workflow.getWorkflowId(), request.getId(), request.getResourceId());
                return true;
            } catch (InitializationException e) {
                log.error("An error occurred trying to build mitigation workflow for resource survey request {} for resource {}", request.getId(), request.getResourceId(), e);
                return false;
            }
        });
    }

    @NotNull
    private ResourceSurveyRequest runSurveyForRequest(final ResourceSurveyRequest request) throws ConflictedStateException {
        if (isInvalidStatus(request, ResourceSurveyRequest.Status.CREATED, ResourceSurveyRequest.Status.QUEUED_FOR_SURVEY)) {
            throw new ConflictedStateException("The resource survey request for resource " + request.getRequester() + " should be \"CREATED\" or \"QUEUED_FOR_SURVEY\" but instead is \"" + request.getRsnStatus() + "\"");
        }

        log.debug("Now running survey for resource survey request {} for resource {}", request.getId(), request.getResourceId());
        request.setRsnStatus(ResourceSurveyRequest.Status.SURVEYING);
        _entityService.update(request);

        final ResourceSurveyHelper helper = new ResourceSurveyHelper(_entityService, request, _serializer, _dicomFileNamer, _stopTagInputHandler);
        log.debug("Created a resource survey helper for resource survey request {} on resource {}, getting ready to start mitigation", request.getId(), request.getResourceId());
        final ResourceSurveyReport report = helper.call();
        log.info("Finished running resource survey helper mitigation for resource survey request {} on resource {}, report says operation handled {} duplicates, {} mismatched files, and {} bad files", request.getId(), request.getResourceId(), report.getTotalDuplicates(), report.getTotalMismatchedFiles(), report.getTotalBadFiles());

        request.setSurveyReport(report);
        request.setRsnStatus(NumberUtils.max(report.getTotalBadFiles(), report.getTotalMismatchedFiles(), report.getTotalDuplicates()) > 0 ? ResourceSurveyRequest.Status.DIVERGENT : ResourceSurveyRequest.Status.CONFORMING);
        _entityService.update(request);
        return request;
    }

    private Map<String, Collection<Long>> processRequests(final UserI requester, final List<ResourceSurveyRequest> requests, final String action, final ResourceSurveyRequest.Status validStatus, final Predicate<ResourceSurveyRequest> processor) {
        final ArrayListMultimap<String, Long> results = ArrayListMultimap.create();

        final Map<HttpStatus, List<ResourceSurveyRequest>> validated = requests.stream().collect(Collectors.groupingBy(new RequestValidator(requester, validStatus)));

        final List<ResourceSurveyRequest> invalid = validated.get(HttpStatus.BAD_REQUEST);
        if (CollectionUtils.isNotEmpty(invalid)) {
            final List<Long> invalidRequestIds = invalid.stream().map(ResourceSurveyRequest::getId).collect(Collectors.toList());
            log.warn("User {} wants to queue {} resource survey requests for {} but {} of those requests have invalid statuses (should be \"{}\"): {}", requester.getUsername(), requests.size(), action, invalidRequestIds.size(), validStatus, invalid.stream().map(request -> request.getRsnStatus().toString()).distinct().collect(Collectors.joining(", ")));
            results.putAll(INVALID, invalidRequestIds);
        }

        final List<ResourceSurveyRequest> missing = validated.get(HttpStatus.NOT_FOUND);
        if (CollectionUtils.isNotEmpty(missing)) {
            final List<Long> missingRequestIds = missing.stream().map(ResourceSurveyRequest::getId).collect(Collectors.toList());
            log.warn("User {} wants to queue {} resource survey requests for {} but {} of those requests don't seem to exist: {}", requester.getUsername(), requests.size(), action, missingRequestIds.size(), missingRequestIds.stream().map(Objects::toString).collect(Collectors.joining(", ")));
            results.putAll(MISSING, missingRequestIds);
        }

        final List<ResourceSurveyRequest> inaccessible = validated.get(HttpStatus.FORBIDDEN);
        if (CollectionUtils.isNotEmpty(inaccessible)) {
            final List<Long> inaccessibleRequestIds = inaccessible.stream().map(ResourceSurveyRequest::getId).collect(Collectors.toList());
            log.warn("User {} wants to queue {} resource survey requests for {} but {} of those requests are inaccessible to the user: {}", requester.getUsername(), requests.size(), action, inaccessibleRequestIds.size(), inaccessibleRequestIds.stream().map(Objects::toString).collect(Collectors.joining(", ")));
            results.putAll(FORBIDDEN, inaccessibleRequestIds);
        }

        final List<ResourceSurveyRequest> valid = Optional.ofNullable(validated.get(HttpStatus.OK)).orElse(Collections.emptyList());
        if (CollectionUtils.isNotEmpty(valid)) {
            if (log.isInfoEnabled()) {
                log.info("User {} wants to queue {} resource survey requests for {}, found {} requests with valid status that are accessible to the user: {}", requester.getUsername(), requests.size(), action, valid.size(), valid.stream().map(ResourceSurveyRequest::getId).map(Object::toString).collect(Collectors.joining(", ")));
            } else {
                log.debug("User {} wants to queue {} resource survey requests for {}, found {} requests with valid status that are accessible to the user", requester.getUsername(), requests.size(), action, valid.size());
            }
        }

        final Map<Boolean, List<ResourceSurveyRequest>> queueable = valid.stream().collect(Collectors.partitioningBy(processor));

        results.putAll(ERRORS, queueable.get(false).stream().map(ResourceSurveyRequest::getId).collect(Collectors.toList()));
        results.putAll(QUEUED, queueable.get(true).stream().map(this::queueRequest).collect(Collectors.toList()));

        return results.asMap();
    }

    private long queueRequest(final ResourceSurveyRequest request) {
        XDAT.sendJmsRequest(_jmsTemplate, request);
        return request.getId();
    }

    private PersistentWorkflowI buildMitigationWorkflow(final UserI requester, final ResourceSurveyRequest request, final String reason, final String comment) throws InitializationException {
        try {
            final PersistentWorkflowI workflow = Optional.ofNullable(PersistentWorkflowUtils.buildOpenWorkflow(requester, request.getXsiType(), request.getExperimentId(), request.getScanLabel(), request.getProjectId(),
                                                                                                               EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.REST, ResourceMitigationHelper.FILE_MITIGATION, reason, comment)))
                                                         .orElseThrow(() -> new InitializationException("Failed to create a workflow entry for mitigation operation for resource survey request " + request.getId() + " on resource " + request.getResourceId()));
            workflow.setStatus(ResourceSurveyRequest.Status.QUEUED_FOR_MITIGATION.toString());
            workflow.setSrc(Integer.toString(request.getResourceId()));
            final EventMetaI event = workflow.buildEvent();
            PersistentWorkflowUtils.save(workflow, event);

            request.setWorkflowId(workflow.getWorkflowId());
            request.setRsnStatus(ResourceSurveyRequest.Status.QUEUED_FOR_MITIGATION);
            _entityService.update(request);

            return workflow;
        } catch (PersistentWorkflowUtils.JustificationAbsent e) {
            throw new InitializationException("You must provide a justification/reason for the resource mitigation operation on resource " + request.getResourceId());
        } catch (PersistentWorkflowUtils.ActionNameAbsent e) {
            throw new InitializationException("You must provide an action for the resource mitigation operation on resource " + request.getResourceId());
        } catch (Exception e) {
            throw new InitializationException("An unknown error occurred trying to create a workflow for resource mitigation operation on resource " + request.getResourceId(), e);
        }
    }

    /**
     * Checks whether the request's current status matches any of the valid statuses. If not, this method refreshes the
     * Hibernate object and checks again.
     *
     * @param request       The request for which you want to validate the status.
     * @param validStatuses One or more statuses to validate against.
     *
     * @return Returns <b>true</b> if the request's status matches one of the valid statuses, <b>false</b> otherwise.
     */
    private boolean isInvalidStatus(final ResourceSurveyRequest request, final ResourceSurveyRequest.Status... validStatuses) {
        if (validStatuses.length == 0) {
            return false;
        }
        final String validStatusesText = Arrays.stream(validStatuses).map(Object::toString).collect(Collectors.joining(", "));
        log.debug("Current status for resource survey request {} for resource {} is {}, valid statuses are {}", request.getId(), request.getResourceId(), request.getRsnStatus(), validStatusesText);
        if (!ArrayUtils.contains(validStatuses, request.getRsnStatus())) {
            log.debug("Current status for resource survey request {} for resource {} is {}, which doesn't match any of the valid statuses, refreshing: {}", request.getId(), request.getResourceId(), request.getRsnStatus(), validStatusesText);
            _entityService.refresh(request);
        }
        final boolean isValid = ArrayUtils.contains(validStatuses, request.getRsnStatus());
        log.debug("Current status for resource survey request {} for resource {} is {}, this is {}, valid statuses are {}", request.getId(), request.getResourceId(), request.getRsnStatus(), isValid ? "valid" : INVALID, validStatusesText);
        return !isValid;
    }

    private void setStatus(final ResourceSurveyRequest request, final WrkWorkflowdata workflow, final ResourceSurveyRequest.Status status) {
        request.setRsnStatus(status);
        _entityService.update(request);

        // Workflow status is already complete if status is conforming, so only set if not conforming.
        if (status != ResourceSurveyRequest.Status.CONFORMING) {
            workflow.setStatus(status.toString());
            try {
                WorkflowUtils.save(workflow, workflow.buildEvent());
            } catch (Exception e) {
                log.error("An error occurred trying to update the status of workflow {} for resource survey request {} for resource {}", workflow.getWrkWorkflowdataId(), request.getId(), request.getResourceId(), e);
            }
        }
    }

    private void validateRequestAccess(final UserI requester, final long requestId) throws NotFoundException, InsufficientPrivilegesException {
        validateRequestAccess(requester, Collections.singletonList(requestId));
    }

    private void validateRequestAccess(final UserI requester, final List<Long> requestIds) throws NotFoundException, InsufficientPrivilegesException {
        final Pair<List<String>, List<Long>> resolved = _entityService.resolveRequestProjects(requestIds);
        if (!resolved.getValue().isEmpty()) {
            throw new NotFoundException("Couldn't locate " + resolved.getValue().size() + " requests: " + resolved.getValue().stream().map(Objects::toString).collect(Collectors.joining(", ")));
        }
        for (final String projectId : resolved.getKey()) {
            validateProjectAccess(requester, projectId);
        }
    }

    /**
     * Verifies that the specified resource exists and that the user has sufficient access to the project containing the
     * resource.
     *
     * @param requester  The user to check
     * @param resourceId The resource to check
     */
    private void validateResourceAccess(final UserI requester, final int resourceId) throws NotFoundException, InsufficientPrivilegesException {
        validateProjectAccess(requester, getResourceProject(resourceId));
    }

    /**
     * Verifies that the specified resource exists and that the user has sufficient access to the project containing the
     * resource.
     *
     * @param requester   The user to check
     * @param resourceIds The resources to check
     */
    private void validateResourceAccess(final UserI requester, final List<Integer> resourceIds) throws NotFoundException, InsufficientPrivilegesException {
        final List<String> projectIds = getResourceProjects(resourceIds);
        for (final String projectId : projectIds) {
            validateProjectAccess(requester, projectId);
        }
    }

    /**
     * Verifies that the specified image session exists and that the user has sufficient access to the project containing
     * the session.
     *
     * @param requester The user to check
     * @param sessionId The session to check
     *
     * @throws InsufficientPrivilegesException When the specified user does not have sufficient privileges on the project
     */
    private void validateSessionAccess(final UserI requester, final String sessionId) throws InsufficientPrivilegesException {
        if (Permissions.verifyAccessToSessions(_jdbcTemplate, requester, Collections.singleton(sessionId)).isEmpty()
            || !Permissions.canDeleteProject(requester, sessionId)) {
            log.warn("User {} tried to do something with resource survey requests for session ID {} but was denied", requester.getUsername(), sessionId);
            throw new InsufficientPrivilegesException(requester.getUsername(), sessionId);
        }
    }

    /**
     * Verifies that the specified project exists and that the user has sufficient access to that project.
     *
     * @param requester The user to check
     * @param projectId The project to check
     *
     * @throws NotFoundException               When the specified project does not exist
     * @throws InsufficientPrivilegesException When the specified user does not have sufficient privileges on the project
     */
    private void validateProjectAccess(final UserI requester, final String projectId) throws NotFoundException, InsufficientPrivilegesException {
        // TODO: This should really be implemented as an aspect, similar to XapiRequestMappingAspect.
        if (!Permissions.verifyProjectExists(_jdbcTemplate, projectId)) {
            log.warn("User {} tried to do something with resource survey requests in project {} but that doesn't exist", requester.getUsername(), projectId);
            throw new NotFoundException(XnatProjectdata.SCHEMA_ELEMENT_NAME, projectId);
        }
        if (!Permissions.canDeleteProject(requester, projectId)) {
            log.warn("User {} tried to do something with resource survey requests in project {} but was denied", requester.getUsername(), projectId);
            throw new InsufficientPrivilegesException(requester.getUsername(), projectId);
        }
    }

    /**
     * Gets the project with which a particular resource is associated.
     *
     * @param resourceId The ID resource to evaluate
     *
     * @return The ID of the associated project if available.
     */
    private String getResourceProject(final int resourceId) throws NotFoundException {
        try {
            return _jdbcTemplate.queryForObject(QUERY_GET_RESOURCE_PROJECT, new MapSqlParameterSource(ResourceSurveyRequestEntityService.PARAM_RESOURCE_ID, resourceId), String.class);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(XnatAbstractresource.SCHEMA_ELEMENT_NAME, resourceId);
        }
    }

    /**
     * Gets the project(s) with which one or more resources are associated.
     *
     * @param resourceIds The IDs of the resources to evaluate
     *
     * @return The ID(s) of the associated project(s) if available.
     */
    private List<String> getResourceProjects(final List<Integer> resourceIds) throws NotFoundException {
        final List<Integer>         unique     = resourceIds.stream().distinct().sorted().collect(Collectors.toList());
        final MapSqlParameterSource parameters = new MapSqlParameterSource(ResourceSurveyRequestEntityService.PARAM_RESOURCE_IDS, unique);

        final List<Integer> found = _jdbcTemplate.queryForList(QUERY_VALIDATE_RESOURCE_IDS, parameters, Integer.class);
        if (ListUtils.isEqualList(unique, found)) {
            return _jdbcTemplate.queryForList(QUERY_GET_RESOURCE_PROJECTS, parameters, String.class);
        }
        throw new NotFoundException("The following resources were requested but don't exist: " + GenericUtils.convertToTypedList(ListUtils.subtract(unique, found), Integer.class).stream().map(Objects::toString).collect(Collectors.joining(", ")));
    }

    private class RequestValidator implements Function<ResourceSurveyRequest, HttpStatus> {
        private final Map<String, HttpStatus> _checkedProjects = new HashMap<>();

        private final UserI                        _requester;
        private final ResourceSurveyRequest.Status _validStatus;

        RequestValidator(final UserI requester, final ResourceSurveyRequest.Status validStatus) {
            _requester   = requester;
            _validStatus = validStatus;
        }

        @Override
        public HttpStatus apply(final ResourceSurveyRequest request) {
            return _validStatus == request.getRsnStatus()
                   ? _checkedProjects.computeIfAbsent(request.getProjectId(), projectId -> {
                try {
                    validateProjectAccess(_requester, projectId);
                    return HttpStatus.OK;
                } catch (NotFoundException e) {
                    return HttpStatus.NOT_FOUND;
                } catch (InsufficientPrivilegesException e) {
                    return HttpStatus.FORBIDDEN;
                }
            })
                   : HttpStatus.BAD_REQUEST;
        }
    }

    @NotNull
    private static <K, S, T> Collector<S, ?, Map<K, T>> toSortedMap(final Function<S, K> mapKeyFunction, final Function<S, T> mapValueFunction) {
        return Collectors.toMap(mapKeyFunction, mapValueFunction, (k1, k2) -> k1, TreeMap::new);
    }

    private static long translateFromResultsMap(final UserI requester, final ResourceSurveyRequest request, final String operation, final Map<String, Collection<Long>> results) throws ConflictedStateException, InsufficientPrivilegesException, NotFoundException {
        if (results.size() > 1) {
            log.warn("Something weird happened queuing resource survey request {} for resource {} for {}, because I have multiple results for one request: {}", request.getId(), request.getResourceId(), operation, results);
        }
        if (results.containsKey(INVALID)) {
            throw new ConflictedStateException("The resource survey request for resource " + request.getRequester() + " should be \"QueuedFor" + StringUtils.capitalize(operation) + "\" but instead is \"" + request.getRsnStatus() + "\"");
        }
        if (results.containsKey(FORBIDDEN)) {
            throw new InsufficientPrivilegesException(requester.getUsername(), request.getProjectId());
        }
        if (results.containsKey(QUEUED)) {
            final Optional<Long> requestId = results.get(QUEUED).stream().findAny();
            if (requestId.isPresent()) {
                return requestId.get();
            }
        }
        throw new NotFoundException(XnatAbstractresource.SCHEMA_ELEMENT_NAME, request.getResourceId());

    }

    private String requestToCsvRow(ResourceSurveyRequest request) {
        String s1 = String.join(",",
                                request.getProjectId(),
                                request.getSubjectId(),
                                request.getExperimentId(),
                                Integer.toString(request.getScanId()),
                                Integer.toString(request.getResourceId()),
                                request.getResourceLabel(),
                                request.getResourceUri(),
                                request.getRsnStatus().toString());

        ResourceSurveyReport report = request.getSurveyReport();
        if (report == null) {
            return String.join(",", s1, "", "", "", "", "", "");
        } else {
            return String.join(",",
                               s1,
                               report.getSurveyDate().toString(),
                               Integer.toString(report.getTotalEntries()),
                               Integer.toString(report.getTotalUids()),
                               Integer.toString(report.getTotalDuplicates()),
                               Integer.toString(report.getTotalBadFiles()),
                               Integer.toString(report.getTotalMismatchedFiles()));
        }
    }

    private List<Integer> parseCsvFile(MultipartFile csv) throws DataFormatException, ServerException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csv.getInputStream()))) {
            String       line;
            int          targetColumn = -1;
            Set<Integer> resourceIds  = new HashSet<>();
            Set<Integer> duplicates   = new HashSet<>();
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (targetColumn >= 0) {
                    int resourceId = Integer.parseInt(fields[targetColumn]);
                    if (!resourceIds.add(resourceId)) {
                        duplicates.add(resourceId);
                    }
                } else {
                    targetColumn = getTargetColumn(fields);
                }
            }
            if (!duplicates.isEmpty()) {
                throw new DataFormatException("The uploaded CSV contains " + duplicates.size() + " duplicate resource IDs: " + duplicates.stream().sorted().map(Objects::toString).collect(Collectors.joining(", ")));
            }
            return new ArrayList<>(resourceIds);
        } catch (IOException e) {
            throw new ServerException("An error occurred trying to open the uploaded CSV as an input stream.", e);
        } catch (NumberFormatException e) {
            throw new DataFormatException("Resource Id must be Integer.");
        }
    }

    private int getTargetColumn(String[] firstLine) throws DataFormatException {
        int targetColumn = -1;
        for (int i = 0; i < firstLine.length; i++) {
            if ("resourceId".equalsIgnoreCase(firstLine[i].trim())) {
                targetColumn = i;
                break;
            }
        }
        if (targetColumn < 0) {
            throw new DataFormatException("Can not find the resourceId column in the uploaded file");
        }
        return targetColumn;
    }

    private UserI getRequester(final ResourceSurveyRequest request) throws NotFoundException {
        try {
            return Users.getUser(request.getRequester());
        } catch (UserInitException | UserNotFoundException e) {
            throw new NotFoundException(XDATUser.SCHEMA_ELEMENT_NAME, request.getRequester());
        }
    }
}
