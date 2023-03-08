package org.nrg.xnat.services.archive;

import org.apache.commons.lang3.tuple.Pair;
import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xapi.exceptions.ConflictedStateException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.entities.ResourceSurveyRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ResourceSurveyRequestEntityService extends BaseHibernateService<ResourceSurveyRequest> {
    String PARAM_REQUESTER    = "requester";
    String PARAM_REQUEST_TIME = "requestTime";
    String PARAM_PROJECT_ID   = "projectId";
    String PARAM_SESSION_ID   = "sessionId";
    String PARAM_RESOURCE_ID  = "resourceId";
    String PARAM_RESOURCE_IDS = "resourceIds";
    String PARAM_REQUEST_ID   = "requestId";
    String PARAM_REQUEST_IDS  = "requestIds";
    String PARAM_SINCE_DATE   = "sinceDate";

    /**
     * Gets the request with the specified ID.
     *
     * @param requestId The ID of the request to retrieve.
     *
     * @return The request with the specified ID, if it exists.
     *
     * @throws NotFoundException When the request doesn't exist.
     */
    ResourceSurveyRequest getRequest(final long requestId) throws NotFoundException;

    /**
     * Gets the requests with the specified IDs.
     *
     * @param requestIds The IDs of the requests to retrieve.
     *
     * @return The requests with the specified IDs, if they exist.
     *
     * @throws NotFoundException When one or more of the requests doesn't exist.
     */
    List<ResourceSurveyRequest> getRequests(final List<Long> requestIds) throws NotFoundException;

    /**
     * Gets the latest request with the specified resource ID.
     *
     * @param resourceId The ID of the resource with which the request is associated.
     *
     * @return The latest request with the specified resource ID, if it exists.
     *
     * @throws NotFoundException When the resource doesn't exist.
     */
    ResourceSurveyRequest getRequestByResourceId(final int resourceId) throws NotFoundException;

    /**
     * Gets all requests with the specified resource ID.
     *
     * @param resourceId The ID of the resource with which the request is associated.
     *
     * @return All requests with the specified resource ID, if it exists.
     *
     * @throws NotFoundException When the resource doesn't exist.
     */
    List<ResourceSurveyRequest> getAllRequestsByResourceId(int resourceId) throws NotFoundException;

    /**
     * Gets the latest requests with the specified resource ID.
     *
     * @param resourceIds The IDs of the resources with which the requests are associated.
     *
     * @return The latest requests with the specified resource IDs.
     *
     * @throws NotFoundException When one or more of the resources doesn't exist.
     */
    List<ResourceSurveyRequest> getRequestsByResourceIds(final List<Integer> resourceIds) throws NotFoundException;

    /**
     * Gets all requests for the project regardless of whether the requests are open or not. If you only want to get
     * open requests, call {@link #getOpenRequestsByProjectId(String)} instead.
     *
     * @param projectId The ID of the project with which the requests are associated.
     *
     * @return All requests associated with the project.
     *
     * @throws NotFoundException When the project doesn't exist.
     */
    List<ResourceSurveyRequest> getAllRequestsByProjectId(final String projectId) throws NotFoundException;

    /**
     * Gets all open requests (i.e. {@link ResourceSurveyRequest#getClosingDate()} is not set) for the project.  If
     * you want to get all requests for the project regardless of status, use {@link #getAllRequestsByProjectId(String)}
     * instead.
     *
     * @param projectId The ID of the project with which the requests are associated.
     *
     * @return All requests associated with the project.
     *
     * @throws NotFoundException When the project doesn't exist.
     */
    List<ResourceSurveyRequest> getOpenRequestsByProjectId(final String projectId) throws NotFoundException;

    /**
     * Gets all requests for the image session regardless of whether the requests are open or not. If you only want to
     * get open requests, call {@link #getOpenRequestsBySessionId(String)} instead.
     *
     * @param sessionId The ID of the image session with which the requests are associated.
     *
     * @return All requests associated with the image session.
     *
     * @throws NotFoundException When the image session doesn't exist.
     */
    List<ResourceSurveyRequest> getAllRequestsBySessionId(final String sessionId) throws NotFoundException;

    /**
     * Gets all open requests (i.e. {@link ResourceSurveyRequest#getClosingDate()} is not set) for the image session. If
     * you want to get all requests for the session regardless of status, use {@link #getAllRequestsBySessionId(String)}
     * instead.
     *
     * @param sessionId The ID of the image session with which the requests are associated.
     *
     * @return All open requests associated with the image session.
     *
     * @throws NotFoundException When the session doesn't exist.
     */
    List<ResourceSurveyRequest> getOpenRequestsBySessionId(final String sessionId) throws NotFoundException;

    /**
     * Gets all requests with the indicated {@link ResourceSurveyRequest#getRequestTime() request time} regardless of
     * whether the requests are open or not. If you only want to get open requests, call {@link
     * #getOpenRequestsByRequestTime(String)} instead.
     *
     * @param requestTime The {@link ResourceSurveyRequest#getRequestTime() request time} with which the requests are associated.
     *
     * @return All requests associated with the specified {@link ResourceSurveyRequest#getRequestTime() request time}.
     *
     * @throws NotFoundException When the {@link ResourceSurveyRequest#getRequestTime() request time} doesn't exist.
     */
    List<ResourceSurveyRequest> getAllRequestsByRequestTime(final String requestTime) throws NotFoundException;

    /**
     * Gets all open requests (i.e. {@link ResourceSurveyRequest#getClosingDate()} is not set) for the indicated {@link
     * ResourceSurveyRequest#getRequestTime() request time}. If you want to get all requests for the request time
     * regardless of status, use {@link #getAllRequestsByRequestTime(String)} instead.
     *
     * @param requestTime The {@link ResourceSurveyRequest#getRequestTime() request time} with which the requests are associated.
     *
     * @return All open requests associated with the {@link ResourceSurveyRequest#getRequestTime() request time}.
     *
     * @throws NotFoundException When the {@link ResourceSurveyRequest#getRequestTime() request time} doesn't exist.
     */
    List<ResourceSurveyRequest> getOpenRequestsByRequestTime(final String requestTime) throws NotFoundException;

    /**
     * Gets all requests with the specified status(es) for the project.
     *
     * @param projectId The ID of the project with which the requests are associated.
     * @param statuses  One or more statuses on which to search.
     *
     * @return All requests associated with the project with the specified status(es).
     *
     * @throws NotFoundException When the project doesn't exist.
     */
    @SuppressWarnings("unused")
    List<ResourceSurveyRequest> getRequestsByProjectIdAndStatus(final String projectId, final ResourceSurveyRequest.Status... statuses) throws NotFoundException;

    /**
     * Gets all requests with the specified status(es) for the project.
     *
     * @param projectId The ID of the project with which the requests are associated.
     * @param statuses  One or more statuses on which to search.
     *
     * @return All requests associated with the project with the specified status(es).
     *
     * @throws NotFoundException When the project doesn't exist.
     */
    List<ResourceSurveyRequest> getRequestsByProjectIdAndStatus(final String projectId, final List<ResourceSurveyRequest.Status> statuses) throws NotFoundException;

    /**
     * Gets an existing resource survey request with the status {@link ResourceSurveyRequest.Status#CREATED} for the
     * specified resource ID or, if one doesn't exist, creates a new resource survey request for the resource.
     *
     * @param resourceId The ID of the resource for retrieving or creating the survey request.
     *
     * @return A resource survey request with the status {@link ResourceSurveyRequest.Status#CREATED}.
     *
     * @throws NotFoundException When the specified resource ID doesn't exist.
     */
    ResourceSurveyRequest getOrCreateRequestByResourceId(final UserI requester, final int resourceId) throws NotFoundException;

    /**
     * Gets existing resource survey requests with the status {@link ResourceSurveyRequest.Status#CREATED} for each of
     * the specified resource IDs or, if one doesn't exist, creates a new resource survey request for the resource.
     *
     * @param resourceIds The IDs of the resource for retrieving or creating survey requests.
     *
     * @return A list of resource survey requests for the specified resource IDs with the status {@link ResourceSurveyRequest.Status#CREATED}.
     *
     * @throws NotFoundException When one or more of the specified resource IDs doesn't exist.
     */
    List<ResourceSurveyRequest> getOrCreateRequestsByResourceId(final UserI requester, final List<Integer> resourceIds) throws NotFoundException;

    /**
     * Creates a resource survey request for the specified resource.
     *
     * @param requester  The user creating the survey request.
     * @param resourceId The ID of the resource for retrieving or creating the survey request.
     *
     * @return A newly created {@link ResourceSurveyRequest resource survey request} for the specified resource.
     *
     * @throws NotFoundException        When the specified resource ID doesn't exist.
     * @throws ConflictedStateException When an open resource survey request already exists for the specified resource.
     */
    ResourceSurveyRequest create(final UserI requester, final int resourceId) throws NotFoundException, ConflictedStateException;

    /**
     * Creates resource survey requests for the specified resources.
     *
     * @param requester   The user creating the survey requests.
     * @param resourceIds The IDs of the resource for retrieving or creating survey requests.
     *
     * @return Newly created {@link ResourceSurveyRequest resource survey requests} for the specified resources.
     *
     * @throws NotFoundException        When the specified resource ID doesn't exist.
     * @throws ConflictedStateException When an open resource survey request already exists for the specified resource.
     */
    List<ResourceSurveyRequest> create(final UserI requester, final List<Integer> resourceIds) throws NotFoundException, ConflictedStateException;

    /**
     * Creates resource survey requests for DICOM resources in the specified project.
     *
     * @param requester The user creating the survey requests.
     * @param projectId The ID of the project for which survey requests should be created.
     *
     * @return Newly created {@link ResourceSurveyRequest resource survey requests} for DICOM resources in the specified project.
     *
     * @throws NotFoundException When the specified project ID doesn't exist.
     */
    List<ResourceSurveyRequest> create(final UserI requester, final String projectId) throws NotFoundException;

    /**
     * Creates resource survey requests for DICOM resources in the specified project that have been updated since the
     * specified date.
     *
     * @param requester The user creating the survey requests.
     * @param projectId The ID of the project for which survey requests should be created.
     * @param since     The date after which resources must have been updated for survey requests to be created.
     *
     * @return Newly created {@link ResourceSurveyRequest resource survey requests} for DICOM resources in the specified project.
     *
     * @throws NotFoundException When the specified project ID doesn't exist.
     */
    List<ResourceSurveyRequest> create(final UserI requester, final String projectId, final LocalDateTime since) throws NotFoundException;

    /**
     * Returns a pair that contains a list of distinct project IDs containing the submitted request IDs, plus a list of
     * longs containing request IDs that were not found (i.e. that are invalid request IDs).
     *
     * @param requestIds The list of request IDs to resolve.
     *
     * @return A pair with a list of distinct project IDs and a list of any invalid request IDs.
     */
    Pair<List<String>, List<Long>> resolveRequestProjects(final List<Long> requestIds);

    /**
     * Returns a pair that contains a map of resource and request IDs, plus a list of resource IDs that were not found
     * (i.e. that are invalid resource IDs).
     *
     * @param resourceIds The list of resource IDs to resolve.
     *
     * @return A pair with a map of resource and request IDs and a list of any invalid resource IDs.
     */
    Pair<Map<Long, Integer>, List<Integer>> resolveResourceRequestIds(final List<Integer> resourceIds);

    /**
     * Returns a request IDs for open requests in the specified project.
     *
     * @param projectId The ID of the project with which the requests are associated.
     *
     * @return A list of IDs for open requests in the specified project.
     *
     * @throws NotFoundException When the specified project ID doesn't exist.
     */
    List<Long> resolveProjectRequestIds(final String projectId) throws NotFoundException;
}
