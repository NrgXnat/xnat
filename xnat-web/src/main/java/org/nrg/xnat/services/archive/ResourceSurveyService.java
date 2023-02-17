package org.nrg.xnat.services.archive;

import org.nrg.action.ServerException;
import org.nrg.xapi.exceptions.ConflictedStateException;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NoContentException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.entities.ResourceSurveyRequest;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ResourceSurveyService {
    /**
     * Returns the {@link ResourceSurveyRequest resource survey request} with the specified ID.
     *
     * @param requester The user requesting the resource survey request.
     * @param requestId The ID of the request to retrieve.
     *
     * @return The resource survey request with the specified ID.
     *
     * @throws InsufficientPrivilegesException When the requesting user has insufficient permissions to request resource surveys.
     * @throws NotFoundException               When the specified request doesn't exist.
     */
    ResourceSurveyRequest getById(final UserI requester, final long requestId) throws NotFoundException, InsufficientPrivilegesException;

    /**
     * Returns all {@link ResourceSurveyRequest resource survey requests} for the specified resource ID. If you just want to
     * get the latest request for the resource, call {@link #getByResourceId(UserI, int)} instead.
     *
     * @param requester  The user requesting the resource survey request.
     * @param resourceId The ID of the resource to survey.
     *
     * @return The resource survey request for the specified resource.
     *
     * @throws InsufficientPrivilegesException When the requesting user has insufficient permissions to request resource surveys.
     * @throws NotFoundException               When the specified resource doesn't exist.
     * @see #getByResourceId(UserI, int)
     */
    List<ResourceSurveyRequest> getAllByResourceId(final UserI requester, final int resourceId) throws NotFoundException, InsufficientPrivilegesException;

    /**
     * Returns the latest {@link ResourceSurveyRequest resource survey request} for the specified resource ID. The
     * request may or may not be open, but, if there is more than one request for the resource, will be the latest
     * request associated with the resource ID. To retrieve all requests associated with the resource ID, call {@link
     * #getAllByResourceId(UserI, int)} instead.
     *
     * @param requester  The user requesting the resource survey request.
     * @param resourceId The ID of the resource to survey.
     *
     * @return The latest resource survey request for the specified resource.
     *
     * @throws InsufficientPrivilegesException When the requesting user has insufficient permissions to request resource surveys.
     * @throws NotFoundException               When the specified resource doesn't exist.
     * @see #getAllByResourceId(UserI, int)
     */
    ResourceSurveyRequest getByResourceId(final UserI requester, final int resourceId) throws NotFoundException, InsufficientPrivilegesException;

    /**
     * Gets all resource survey requests for the specified project. Note that this includes all requests regardless of
     * status and whether the request is open, canceled, etc. If you want to retrieve only "open" requests (i.e. where
     * {@link ResourceSurveyRequest#getClosingDate()} has not been set), call {@link #getOpenByProjectId(UserI,
     * String)} instead.
     *
     * @param requester The user requesting the resource survey requests.
     * @param projectId The project to be surveyed.
     *
     * @return A list of all resource survey requests for the specified project.
     *
     * @throws InsufficientPrivilegesException When the requesting user has insufficient permissions to request resource surveys.
     * @throws NotFoundException               When the specified project doesn't exist.
     */
    List<ResourceSurveyRequest> getAllByProjectId(final UserI requester, final String projectId) throws InsufficientPrivilegesException, NotFoundException;

    /**
     * Gets all open resource survey requests (i.e. where {@link ResourceSurveyRequest#getClosingDate()} has been
     * set) for the specified project. If you want to retrieve all requests regardless of status, call {@link
     * #getAllByProjectId(UserI, String)} instead.
     *
     * @param requester The user requesting the resource survey requests.
     * @param projectId The project to be surveyed.
     *
     * @return A list of all open resource survey requests for the specified project.
     *
     * @throws InsufficientPrivilegesException When the requesting user has insufficient permissions to request resource surveys.
     * @throws NotFoundException               When the specified project doesn't exist.
     */
    @SuppressWarnings("unused")
    List<ResourceSurveyRequest> getOpenByProjectId(final UserI requester, final String projectId) throws InsufficientPrivilegesException, NotFoundException;

    /**
     * Gets all resource survey requests for the specified session. Note that this includes all requests regardless of
     * status and whether the request is open, canceled, etc. If you want to retrieve only "open" requests (i.e. where
     * {@link ResourceSurveyRequest#getClosingDate()} has not been set), call {@link #getOpenBySessionId(UserI, String)}
     * instead.
     *
     * @param requester The user requesting the resource survey requests.
     * @param sessionId The session to be surveyed.
     *
     * @return A list of all resource survey requests for the specified session.
     *
     * @throws InsufficientPrivilegesException When the requesting user has insufficient permissions to request resource surveys.
     * @throws NotFoundException               When the specified session doesn't exist.
     */
    @SuppressWarnings("unused")
    List<ResourceSurveyRequest> getAllBySessionId(final UserI requester, final String sessionId) throws InsufficientPrivilegesException, NotFoundException;

    /**
     * Gets all open resource survey requests (i.e. where {@link ResourceSurveyRequest#getClosingDate()} has been
     * set) for the specified image session. If you want to retrieve all requests regardless of status, call {@link
     * #getAllBySessionId(UserI, String)} instead.
     *
     * @param requester The user requesting the resource survey requests.
     * @param sessionId The session to be surveyed.
     *
     * @return A list of all open resource survey requests for the specified session.
     *
     * @throws InsufficientPrivilegesException When the requesting user has insufficient permissions to request resource surveys.
     * @throws NotFoundException               When the specified session doesn't exist.
     */
    @SuppressWarnings("unused")
    List<ResourceSurveyRequest> getOpenBySessionId(final UserI requester, final String sessionId) throws InsufficientPrivilegesException, NotFoundException;

    /**
     * Gets all resource survey requests for the specified project with status set to one specified in the call. If no
     * statuses are specified, the return from this method is the sa,e
     *
     * @param requester The user requesting the resource survey requests.
     * @param projectId The project to be surveyed.
     * @param statuses  More statuses to match.
     *
     * @return A list of resource survey requests matching the specified status or statuses for the specified project.
     *
     * @throws InsufficientPrivilegesException When the requesting user has insufficient permissions to request resource surveys.
     * @throws NotFoundException               When the specified project doesn't exist.
     */
    List<ResourceSurveyRequest> getByProjectIdAndStatus(final UserI requester, final String projectId, final ResourceSurveyRequest.Status... statuses) throws InsufficientPrivilegesException, NotFoundException;

    /**
     * Gets all resource survey requests for the specified project with status set to one specified in the call. If no
     * statuses are specified, the return from this method is the sa,e
     *
     * @param requester The user requesting the resource survey requests.
     * @param projectId The project to be surveyed.
     * @param statuses  More statuses to match.
     *
     * @return A list of resource survey requests matching the specified status or statuses for the specified project.
     *
     * @throws InsufficientPrivilegesException When the requesting user has insufficient permissions to request resource surveys.
     * @throws NotFoundException               When the specified project doesn't exist.
     */
    List<ResourceSurveyRequest> getByProjectIdAndStatus(final UserI requester, final String projectId, final List<ResourceSurveyRequest.Status> statuses) throws InsufficientPrivilegesException, NotFoundException;

    /**
     * Queues a request to survey and generate a survey report for the resource with the specified ID. The
     * requesting user <i>must</i> have sufficient permissions to delete data in the project containing the specified
     * resource.
     *
     * @param requester  The user requesting the resource survey.
     * @param resourceId The ID of the resource to be surveyed.
     *
     * @return The ID for the queued request.
     */
    long queueResourceSurvey(final UserI requester, final int resourceId) throws NotFoundException, ConflictedStateException, InsufficientPrivilegesException;

    /**
     * Queues a request to survey and generate a survey report for the resource with the specified ID. The
     * requesting user <i>must</i> have sufficient permissions to delete data in the project containing the specified
     * resource.
     *
     * @param requester  The user requesting the resource survey.
     * @param resourceId The ID of the resource to be surveyed.
     * @param reason     The reason for running the resource survey.
     * @param comment    A comment for the resource survey.
     *
     * @return The ID for the queued request.
     */
    long queueResourceSurvey(final UserI requester, final int resourceId, final String reason, final String comment) throws NotFoundException, ConflictedStateException, InsufficientPrivilegesException;

    /**
     * Queues a request to survey and generate a survey report for the resource with the specified ID. The
     * requesting user <i>must</i> have sufficient permissions to delete data in the project containing the specified
     * resource.
     *
     * @param requester   The user requesting the resource survey.
     * @param resourceIds The IDs of the resources to be surveyed.
     *
     * @return A map with a list of IDs for queued requests as "queued" and non-queued requests under "invalid" and "forbidden".
     */
    @SuppressWarnings("unused")
    Map<String, Collection<Long>> queueResourceSurveys(final UserI requester, final List<Integer> resourceIds) throws NotFoundException;

    /**
     * Queues requests to survey and generate survey reports for the resources with the specified IDs. The
     * requesting user <i>must</i> have sufficient permissions to delete data in the project containing the specified
     * resource.
     *
     * @param requester   The user requesting the resource survey.
     * @param resourceIds The IDs of the resources to be surveyed.
     * @param reason      The reason for running the resource survey.
     * @param comment     A comment for the resource survey.
     *
     * @return A map with a list of IDs for queued requests as "queued" and non-queued requests under "invalid" and "forbidden".
     */
    Map<String, Collection<Long>> queueResourceSurveys(final UserI requester, final List<Integer> resourceIds, final String reason, final String comment) throws NotFoundException;

    /**
     * Queues requests to survey and generate survey reports for the resources with the specified IDs. The
     * requesting user <i>must</i> have sufficient permissions to delete data in the project containing the specified
     * resource.
     *
     * @param requester The user requesting the resource survey.
     * @param projectId The ID of the project containing resources to be surveyed.
     *
     * @return A list of IDs for queued requests.
     */
    @SuppressWarnings("unused")
    List<Long> queueProjectSurveys(final UserI requester, final String projectId) throws NotFoundException, InsufficientPrivilegesException;

    /**
     * Queues requests to survey and generate survey reports for the resources with the specified IDs. The
     * requesting user <i>must</i> have sufficient permissions to delete data in the project containing the specified
     * resource.
     *
     * @param requester The user requesting the resource survey.
     * @param projectId The ID of the project containing resources to be surveyed.
     *
     * @return A list of IDs for queued requests.
     */
    List<Long> queueProjectSurveys(final UserI requester, final String projectId, final LocalDateTime since) throws NotFoundException, InsufficientPrivilegesException;

    /**
     * Queues requests to survey and generate survey reports for the resources with the specified IDs. The
     * requesting user <i>must</i> have sufficient permissions to delete data in the project containing the specified
     * resource.
     *
     * @param requester The user requesting the resource survey.
     * @param projectId The ID of the project containing resources to be surveyed.
     * @param reason    The reason for running the resource survey.
     * @param comment   A comment for the resource survey.
     *
     * @return A list of IDs for queued requests.
     */
    @SuppressWarnings("unused")
    List<Long> queueProjectSurveys(final UserI requester, final String projectId, final String reason, final String comment) throws NotFoundException, InsufficientPrivilegesException;

    /**
     * Queues requests to survey and generate survey reports for the resources with the specified IDs. The
     * requesting user <i>must</i> have sufficient permissions to delete data in the project containing the specified
     * resource.
     *
     * @param requester The user requesting the resource survey.
     * @param projectId The ID of the project containing resources to be surveyed.
     * @param reason    The reason for running the resource survey.
     * @param comment   A comment for the resource survey.
     *
     * @return A list of IDs for queued requests.
     */
    List<Long> queueProjectSurveys(final UserI requester, final String projectId, final LocalDateTime since, final String reason, final String comment) throws NotFoundException, InsufficientPrivilegesException;

    /**
     * Queues a request to survey and generate survey reports for the resources with the specified IDs. The requesting user
     * <i>must</i> have sufficient permissions to delete data in the project containing the specified resource.
     *
     * @param requester The user requesting the resource survey.
     * @param requestId The ID of the resource survey request to be surveyed.
     *
     * @return The ID for the queued request.
     */
    @SuppressWarnings("unused")
    long queueSurvey(final UserI requester, final long requestId) throws NotFoundException, ConflictedStateException, InsufficientPrivilegesException;

    /**
     * Queues a request to survey and generate survey reports for the resources with the specified IDs. The requesting user
     * <i>must</i> have sufficient permissions to delete data in the project containing the specified resource.
     *
     * @param requester The user requesting the resource survey.
     * @param requestId The ID of the resource survey request to be surveyed.
     * @param reason    The reason for running the resource survey.
     * @param comment   A comment for the resource survey.
     *
     * @return The ID for the queued request.
     */
    long queueSurvey(final UserI requester, final long requestId, final String reason, final String comment) throws NotFoundException, ConflictedStateException, InsufficientPrivilegesException;

    /**
     * Queues requests to survey and generate survey reports for the resources with the specified IDs. The requesting user
     * <i>must</i> have sufficient permissions to delete data in the project containing the specified resource.
     *
     * @param requester  The user requesting the resource survey.
     * @param requestIds The IDs of the resource survey requests to be surveyed.
     *
     * @return A map with a list of IDs for queued requests as "queued" and non-queued requests under "invalid" and "forbidden".
     */
    @SuppressWarnings("unused")
    Map<String, Collection<Long>> queueSurveys(final UserI requester, final List<Long> requestIds) throws NotFoundException, InsufficientPrivilegesException, ConflictedStateException;

    /**
     * Queues requests to survey and generate survey reports for the resources with the specified IDs. The requesting user
     * <i>must</i> have sufficient permissions to delete data in the project containing the specified resource.
     *
     * @param requester  The user requesting the resource survey.
     * @param requestIds The IDs of the resource survey requests to be surveyed.
     * @param reason     The reason for running the resource survey.
     * @param comment    A comment for the resource survey.
     *
     * @return A map with a list of IDs for queued requests as "queued" and non-queued requests under "invalid" and "forbidden".
     */
    Map<String, Collection<Long>> queueSurveys(final UserI requester, final List<Long> requestIds, final String reason, final String comment) throws NotFoundException;

    /**
     * Runs a survey on the catalog and files in the specified resource. The requesting user <i>must</i> have sufficient
     * permissions to delete data in the project containing the specified resource.
     *
     * @param request The resource survey request to survey.
     *
     * @return A report on the resource survey results.
     *
     * @throws ConflictedStateException        When no resource survey exists for the resource or the resource survey request status is not queued.
     * @throws InsufficientPrivilegesException When the requesting user has insufficient permissions to request a resource survey.
     * @throws NotFoundException               When the specified resource doesn't exist.
     */
    ResourceSurveyRequest surveyResource(final ResourceSurveyRequest request) throws NotFoundException, ConflictedStateException, InsufficientPrivilegesException;

    /**
     * Runs survey on the catalog and files for resource survey requests with status "CREATED" in the specified project.
     * The requesting user <i>must</i> have sufficient permissions to delete data in the project containing the specified
     * resource.
     *
     * @param requester The user requesting the resource survey.
     * @param projectId The ID of the project containing resources to be surveyed.
     *
     * @return Report containing the survey results.
     *
     * @throws InsufficientPrivilegesException When the requesting user has insufficient permissions to request a resource survey.
     * @throws NotFoundException               When the specified resource doesn't exist.
     */
    @SuppressWarnings("unused")
    List<ResourceSurveyRequest> surveyResources(final UserI requester, final String projectId) throws InsufficientPrivilegesException, NotFoundException;

    /**
     * Gets the status of survey operations for DICOM resources in the specified project. The requesting user <i>must</i>
     * have sufficient permissions to delete data in the project.
     *
     * @param requester The user requesting the survey status.
     * @param projectId The ID of the project being surveyed.
     *
     * @return A map containing the resource IDs along with the current status for each request.
     *
     * @throws InsufficientPrivilegesException When the requesting user has insufficient permissions to request a resource survey.
     * @throws NotFoundException               When the specified resource doesn't exist.
     */
    Map<Integer, ResourceSurveyRequest.Status> getRequestStatus(final UserI requester, final String projectId) throws InsufficientPrivilegesException, NotFoundException;

    /**
     * Gets the status of the survey operation for the specified resource. The requesting user <i>must</i> have sufficient
     * permissions to delete data in the project.
     *
     * @param requester  The user requesting the resource survey.
     * @param resourceId The ID of the resource being surveyed.
     *
     * @return The current survey status for the specified resource.
     *
     * @throws InsufficientPrivilegesException When the requesting user has insufficient permissions to request a resource survey.
     * @throws NotFoundException               When the specified resource doesn't exist.
     */
    ResourceSurveyRequest.Status getRequestStatus(final UserI requester, final int resourceId) throws InsufficientPrivilegesException, NotFoundException;

    /**
     * Gets the status of the survey operation for the specified resources. The requesting user <i>must</i> have
     * sufficient permissions to delete data in the project.
     *
     * @param requester   The user requesting the resource survey.
     * @param resourceIds The IDs of the resources being surveyed.
     *
     * @return The current survey status for the specified resources.
     *
     * @throws InsufficientPrivilegesException When the requesting user has insufficient permissions to request a resource survey.
     * @throws NotFoundException               When the specified resource doesn't exist.
     */
    Map<Integer, ResourceSurveyRequest.Status> getRequestStatus(final UserI requester, final List<Integer> resourceIds) throws InsufficientPrivilegesException, NotFoundException;

    /**
     * Gets the status of the specified resource survey request. The requesting user <i>must</i> have sufficient
     * permissions to delete data in the project.
     *
     * @param requester The user requesting the resource survey.
     * @param requestId The ID of the resource survey request.
     *
     * @return The current survey status for the specified resource survey request.
     *
     * @throws InsufficientPrivilegesException When the requesting user has insufficient permissions to request a resource survey.
     * @throws NotFoundException               When the specified resource survey request doesn't exist.
     */
    ResourceSurveyRequest.Status getRequestStatus(final UserI requester, final long requestId) throws InsufficientPrivilegesException, NotFoundException;

    /**
     * Gets all resource survey requests on the server
     *
     * @param requester The user requesting the resource survey requests.
     *
     * @return A list of resource survey requests.
     *
     * @throws InsufficientPrivilegesException When the requesting user has insufficient permissions to request resource surveys. Only site Admin can get all the resource survey requests.
     */
    List<ResourceSurveyRequest> getAllResourceSurveyRequests(final UserI requester) throws InsufficientPrivilegesException, InitializationException;

    /**
     * Queues a request to mitigate issues from the resource survey report for the resource with the specified ID. The
     * requesting user <i>must</i> have sufficient permissions to delete data in the project containing the specified
     * resource.
     *
     * @param requester  The user requesting resource mitigation.
     * @param resourceId The ID of the resource to be mitigated.
     *
     * @return The ID for the queued request.
     */
    long queueResourceMitigation(final UserI requester, final int resourceId) throws NotFoundException, ConflictedStateException, InsufficientPrivilegesException;

    /**
     * Queues a request to mitigate issues from the resource survey report for the resource with the specified ID. The
     * requesting user <i>must</i> have sufficient permissions to delete data in the project containing the specified
     * resource.
     *
     * @param requester  The user requesting the resource mitigation.
     * @param resourceId The ID of the resource to be mitigated.
     * @param reason     The reason for mitigation.
     * @param comment    A comment for mitigation.
     *
     * @return The ID for the queued request.
     */
    long queueResourceMitigation(final UserI requester, final int resourceId, final String reason, final String comment) throws NotFoundException, ConflictedStateException, InsufficientPrivilegesException;

    /**
     * Queues requests to mitigate issues from the resource survey reports for resources with the specified IDs. The
     * requesting user <i>must</i> have sufficient permissions to delete data in the project containing the specified
     * resource.
     *
     * @param requester   The user requesting the resource mitigation.
     * @param resourceIds The IDs of the resources to be mitigated.
     *
     * @return A map with a list of IDs for queued requests as "queued" and non-queued requests under "invalid" and "forbidden".
     */
    Map<String, Collection<Long>> queueResourceMitigation(final UserI requester, final List<Integer> resourceIds) throws NotFoundException;

    /**
     * Queues requests to mitigate issues from the resource survey reports for resources with the specified IDs. The
     * requesting user <i>must</i> have sufficient permissions to delete data in the project containing the specified
     * resource.
     *
     * @param requester   The user requesting the resource mitigation.
     * @param resourceIds The IDs of the resources to be mitigated.
     * @param reason      The reason for mitigation.
     * @param comment     A comment for mitigation.
     *
     * @return A map with a list of IDs for queued requests as "queued" and non-queued requests under "invalid" and "forbidden".
     */
    Map<String, Collection<Long>> queueResourceMitigation(final UserI requester, final List<Integer> resourceIds, final String reason, final String comment) throws NotFoundException;

    /**
     * Queues requests to mitigate issues from the resource survey reports for resources in the specified project. The
     * requesting user <i>must</i> have sufficient permissions to delete data in the project containing the specified
     * resource.
     *
     * @param requester The user requesting the resource mitigation.
     * @param projectId The ID of the project containing resources to be mitigated.
     *
     * @return A list of IDs for queued requests.
     */
    List<Long> queueProjectMitigation(final UserI requester, final String projectId) throws NotFoundException, InsufficientPrivilegesException;

    /**
     * Queues requests to mitigate issues from the resource survey reports for resources in the specified project. The
     * requesting user <i>must</i> have sufficient permissions to delete data in the project containing the specified
     * resource.
     *
     * @param requester The user requesting the resource mitigation.
     * @param projectId The ID of the project containing resources to be mitigated.
     * @param reason    The reason for mitigation.
     * @param comment   A comment for mitigation.
     *
     * @return A list of IDs for queued requests.
     */
    List<Long> queueProjectMitigation(final UserI requester, final String projectId, final String reason, final String comment) throws NotFoundException, InsufficientPrivilegesException;

    /**
     * Queues a request to mitigate issues from the resource survey report with the specified ID. The requesting user
     * <i>must</i> have sufficient permissions to delete data in the project containing the specified resource.
     *
     * @param requester The user requesting the resource mitigation.
     * @param requestId The ID of the resource survey request to be mitigated.
     *
     * @return The ID for the queued request.
     */
    @SuppressWarnings("unused")
    long queueMitigation(final UserI requester, final long requestId) throws NotFoundException, ConflictedStateException, InsufficientPrivilegesException;

    /**
     * Queues a request to mitigate issues from the resource survey report with the specified ID. The requesting user
     * <i>must</i> have sufficient permissions to delete data in the project containing the specified resource.
     *
     * @param requester The user requesting the resource mitigation.
     * @param requestId The ID of the resource survey request to be mitigated.
     * @param reason    The reason for mitigation.
     * @param comment   A comment for mitigation.
     *
     * @return The ID for the queued request.
     */
    long queueMitigation(final UserI requester, final long requestId, final String reason, final String comment) throws NotFoundException, ConflictedStateException, InsufficientPrivilegesException;

    /**
     * Queues requests to mitigate issues from the resource survey reports with the specified IDs. The requesting user
     * <i>must</i> have sufficient permissions to delete data in the project(s) containing the specified resources.
     *
     * @param requester  The user requesting the resource mitigation.
     * @param requestIds The IDs of the resource survey requests to be mitigated.
     *
     * @return A map with a list of IDs for queued requests as "queued" and non-queued requests under "invalid" and "forbidden".
     */
    @SuppressWarnings("unused")
    Map<String, Collection<Long>> queueMitigations(final UserI requester, final List<Long> requestIds) throws NotFoundException, InsufficientPrivilegesException, ConflictedStateException;

    /**
     * Queues the request to use resource survey reports for resources in the specified project to mitigate mismatched
     * file names and duplicate DICOM instances. The requesting user <i>must</i> have sufficient permissions to delete
     * data in the project containing the specified resource.
     *
     * @param requester  The user requesting the resource mitigation.
     * @param requestIds The IDs of the resource survey requests to be mitigated.
     * @param reason     The reason for mitigation.
     * @param comment    A comment for mitigation.
     *
     * @return A map with a list of IDs for queued requests as "queued" and non-queued requests under "invalid" and "forbidden".
     */
    Map<String, Collection<Long>> queueMitigations(final UserI requester, final List<Long> requestIds, final String reason, final String comment) throws NotFoundException;

    /**
     * Uses the resource survey report for the specified resource to mitigate mismatched file names and duplicate DICOM
     * instances. The requesting user <i>must</i> have sufficient permissions to delete data in the project containing
     * the specified resource.
     *
     * @param request The resource survey request to mitigate.
     *
     * @throws InsufficientPrivilegesException When the requesting user has insufficient permissions to request resource mitigation.
     * @throws NotFoundException               When the specified resource doesn't exist.
     */
    void mitigateResource(final ResourceSurveyRequest request) throws NotFoundException, InsufficientPrivilegesException, InitializationException, ConflictedStateException;

    /**
     * Remove details from all survey and mitigation reports for the specified resource.
     *
     * @param requester  The user requesting to clean the survey and mitigation reports.
     * @param resourceId The resource ID to check.
     */
    void cleanResourceReports(final UserI requester, final int resourceId) throws InsufficientPrivilegesException, NotFoundException;

    /**
     * Remove details from the specified survey and mitigation report.
     *
     * @param requester The user requesting to clean the survey report.
     * @param requestId The ID of the request to clean.
     */
    void cleanRequestReports(final UserI requester, final long requestId) throws InsufficientPrivilegesException, NotFoundException;

    /**
     * Convert a list of resource survey requests to a string in the CSV format
     *
     * @param requests A list of resource survey requests
     *
     * @return A string in the CSV format
     */
    String convertRequestsToCsv(List<ResourceSurveyRequest> requests);

    /**
     * Get a list of resourceIds from the uploaded csv file
     *
     * @param csv The uploaded csv file
     *
     * @return A list of ResourceIds
     *
     * @throws NoContentException  There is no content in the CSV file
     * @throws DataFormatException The format of the CSV file is wrong.
     * @throws ServerException     An error is happened when reading the CSV file.
     */
    List<Integer> getResourceIds(MultipartFile csv) throws NoContentException, DataFormatException, ServerException;

    /**
     * Cancels the open resource survey request with the specified ID, if one exists.
     *
     * @param requester The user requesting the resource survey request cancellation.
     * @param requestId The ID of the request to be canceled.
     *
     * @return The ID of the canceled request if found, 0 if the request exists but isn't open.
     *
     * @throws InsufficientPrivilegesException When the requesting user has insufficient permissions to request resource mitigation.
     * @throws NotFoundException               When the specified request doesn't exist.
     */
    long cancelRequest(final UserI requester, final long requestId) throws InsufficientPrivilegesException, NotFoundException;

    /**
     * Cancels the open resource survey requests with the specified IDs.
     *
     * @param requester  The user requesting the resource survey request cancellation.
     * @param requestIds The IDs of the requests to be canceled.
     *
     * @return The IDs of the requests that were canceled.
     *
     * @throws InsufficientPrivilegesException When the requesting user has insufficient permissions to request resource mitigation.
     * @throws NotFoundException               When one or more specified requests doesn't exist.
     */
    List<Long> cancelRequests(final UserI requester, final List<Long> requestIds) throws InsufficientPrivilegesException, NotFoundException;

    /**
     * Cancels the open resource survey request for the specified resource, if one exists.
     *
     * @param requester  The user requesting the resource survey request cancellation.
     * @param resourceId The ID of the resource with which an associated request should be canceled.
     *
     * @return The ID of the canceled request if found, 0 if no open request is associated with the resource.
     *
     * @throws InsufficientPrivilegesException When the requesting user has insufficient permissions to request resource mitigation.
     * @throws NotFoundException               When the specified resource doesn't exist.
     */
    long cancelRequestByResourceId(final UserI requester, final int resourceId) throws InsufficientPrivilegesException, NotFoundException;

    /**
     * Cancels the open resource survey requests for the specified resources, if any exist.
     *
     * @param requester   The user requesting the resource survey request cancellation.
     * @param resourceIds The IDs of the resource with which an associated request should be canceled.
     *
     * @return A map of resource IDs with the IDs of canceled requests.
     *
     * @throws InsufficientPrivilegesException When the requesting user has insufficient permissions to request resource mitigation.
     * @throws NotFoundException               When the specified resources don't exist.
     */
    Map<Integer, Long> cancelRequestsByResourceId(final UserI requester, final List<Integer> resourceIds) throws InsufficientPrivilegesException, NotFoundException;

    /**
     * Cancels all open resource survey requests for the specified project, if any exist.
     *
     * @param requester The user requesting the resource survey request cancellation.
     * @param projectId The ID of the project with which associated requests should be canceled.
     *
     * @return A map of resource IDs with the IDs of canceled requests.
     *
     * @throws InsufficientPrivilegesException When the requesting user has insufficient permissions to request resource mitigation.
     * @throws NotFoundException               When the specified project doesn't exist.
     */
    List<Long> cancelRequestsByProjectId(final UserI requester, final String projectId) throws InsufficientPrivilegesException, NotFoundException;
}
