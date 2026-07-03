package org.nrg.xapi.rest.data;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import jakarta.validation.constraints.NotNull;
import javax.annotation.Nullable;
import org.nrg.action.ServerException;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.exceptions.ConflictedStateException;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NoContentException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.entities.ResourceSurveyRequest;
import org.nrg.xnat.services.archive.ResourceMitigationReport;
import org.nrg.xnat.services.archive.ResourceSurveyReport;
import org.nrg.xnat.services.archive.ResourceSurveyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Tag(name = "XNAT Resource Survey and Mitigation API")
@XapiRestController
@RequestMapping(value = "/resources")
@Slf4j
public class ResourceSurveyApi extends AbstractXapiRestController {
    private static final String INVALID_STATUS = "The value \"%s\" is not a valid status, must be one of: " + ResourceSurveyRequest.Status.ALL_VALUES;

    private final ResourceSurveyService _resourceSurveyService;

    @Autowired
    public ResourceSurveyApi(final ResourceSurveyService resourceSurveyService, final UserManagementServiceI userManagementService, final RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
        log.debug("Starting the resource survey API service");
        _resourceSurveyService = resourceSurveyService;
    }

    @Operation(summary = "Create resource survey requests for resources in the specified project that have been modified since the specified date (if provided)", description = "Returns the IDs of newly generated survey requests for resources in the specified project. This call also queues tasks to survey the contents of "
                          + "the matching resources. To specify a date limiting survey requests to just those resources that were modified since that date, add a querystring parameter "
                          + "in the format 'since=YYYY-MM-DD' (compliant with the ISO-8601 date format).")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns a list of IDs for new resource survey requests for the specified project."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access or administer resource survey requests."),
                   @ApiResponse(responseCode = "404", description = "No project exists with the specified ID."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "survey/project/{projectId}", produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = AccessLevel.Delete)
    public List<Long> createResourceSurveyRequestsForProject(final @PathVariable String projectId, final @RequestParam(required = false) String since) throws InsufficientPrivilegesException, NotFoundException {
        return _resourceSurveyService.queueProjectSurveys(getSessionUser(), projectId, parseDate(since));
    }

    @Operation(summary = "Creates a resource survey request for the specified resource", description = "Returns the ID of the newly generated survey request for the specified resource. This call also queues a task to survey the contents of the specified resource.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns the ID of the new resource survey request for the specified resource."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access or administer resource survey requests."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    // TODO: No restrictTo here because there is no XAPI authorization for resource IDs: see XNAT-7373. The underlying service has to manage permissions checks.
    @XapiRequestMapping(value = "survey/resource/{resourceId}", produces = APPLICATION_JSON_VALUE, method = POST)
    public long createResourceSurveyRequest(final @PathVariable int resourceId) throws InsufficientPrivilegesException, NotFoundException, ConflictedStateException {
        return _resourceSurveyService.queueResourceSurvey(getSessionUser(), resourceId);
    }

    @Operation(summary = "Gets resource survey requests in the specified project that have been created or surveyed but not yet mitigated or otherwise closed", description = "This call returns the full resource survey request, including completed resource scan requests")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns resource survey requests for the specified project."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access or administer resource survey requests for the specified project."),
                   @ApiResponse(responseCode = "404", description = "No project exists with the specified ID."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "survey/project/{projectId}", produces = APPLICATION_JSON_VALUE, method = GET)
    public List<ResourceSurveyRequest> getResourceSurveyRequestsByProject(final @PathVariable String projectId) throws InsufficientPrivilegesException, NotFoundException {
        return _resourceSurveyService.getByProjectIdAndStatus(getSessionUser(), projectId, ResourceSurveyRequest.Status.SURVEY_VALUES);
    }

    @Operation(summary = "Get the resource survey requests with the indicated status for resources in the specified project. The status value can be one of the following case-insensitive options: all, created, queued_for_survey, surveying, divergent, conforming, queued_for_mitigation, mitigating, canceled, error", description = "This call returns the full resource survey request, including completed resource scan requests")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns resource survey requests with the indicated status for the specified project ID."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access or administer resource survey requests for the specified project."),
                   @ApiResponse(responseCode = "404", description = "No project exists with the specified ID."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "survey/project/{projectId}/{status}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Delete)
    public List<ResourceSurveyRequest> getResourceSurveyRequestsByProjectAndStatus(
            final @PathVariable String projectId,
            final @PathVariable @Parameter(description = "status", required = true, schema = @Schema(allowableValues = {"all", "created", "queued_for_survey", "surveying", "divergent", "conforming", "queued_for_mitigation", "mitigating", "canceled", "error"})) String status
                                                                                  ) throws InsufficientPrivilegesException, NotFoundException, DataFormatException {
        return StringUtils.isBlank(status) || StringUtils.equalsIgnoreCase("all", status)
               ? _resourceSurveyService.getAllByProjectId(getSessionUser(), projectId)
               : _resourceSurveyService.getByProjectIdAndStatus(getSessionUser(), projectId,
                                                                ResourceSurveyRequest.Status.parse(status)
                                                                                            .orElseThrow(() -> new DataFormatException(INVALID_STATUS.formatted(status))));
    }

    @Operation(summary = "Get the latest resource survey request for the specified resource", description = "Returns a resource survey request for the specified resource. If no resource with the specified ID exists or if the resource exists but no resource survey requests exist for that resource, this method returns a 404 status. If multiple resource survey requests exist for the resource, only the latest request is returned.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns the latest resource survey request for the specified resource ID."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access or administer resource survey requests in the project containing the specified resource."),
                   @ApiResponse(responseCode = "404", description = "Either no resource exists with the specified ID or there are no resource survey requests for that resource."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    // TODO: No restrictTo here because there is no XAPI authorization for resource IDs: see XNAT-7373. The underlying service has to manage permissions checks.
    @XapiRequestMapping(value = "survey/resource/{resourceId}", produces = APPLICATION_JSON_VALUE, method = GET)
    public ResourceSurveyRequest getResourceSurveyRequestByResourceId(final @PathVariable int resourceId) throws InsufficientPrivilegesException, NotFoundException {
        return _resourceSurveyService.getByResourceId(getSessionUser(), resourceId);
    }

    @Operation(summary = "Get the resource survey request with the specified ID")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns the specified resource survey request."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access or administer resource survey requests in the project containing the specified resource."),
                   @ApiResponse(responseCode = "404", description = "No resource survey request exists with the specified ID."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "survey/request/{requestId}", produces = APPLICATION_JSON_VALUE, method = GET)
    public ResourceSurveyRequest getResourceSurveyRequest(final @PathVariable long requestId) throws InsufficientPrivilegesException, NotFoundException {
        return _resourceSurveyService.getById(getSessionUser(), requestId);
    }

    @Operation(summary = "Cancels the specified resource survey request", description = "Returns the ID of the canceled survey request")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns the canceled resource survey request ID."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access or administer resource survey requests."),
                   @ApiResponse(responseCode = "404", description = "The specified ID does not exist as a resource survey request ID."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "survey/request/{requestId}", produces = APPLICATION_JSON_VALUE, method = DELETE)
    public long cancelResourceSurveyRequest(final @PathVariable long requestId) throws InsufficientPrivilegesException, NotFoundException {
        return _resourceSurveyService.cancelRequest(getSessionUser(), requestId);
    }

    @Operation(summary = "Cancels the specified resource survey requests", description = "Returns the IDs of the canceled survey requests. If one or more of the IDs doesn't exist as a resource survey request ID, none of the requests are canceled and 404 is returned.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns the canceled resource survey request IDs."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access or administer resource survey requests."),
                   @ApiResponse(responseCode = "404", description = "One or more of the specified IDs do not exist as resource survey request IDs."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "survey/request", produces = APPLICATION_JSON_VALUE, method = DELETE)
    public List<Long> cancelResourceSurveyRequest(final @RequestBody List<Long> requestIds) throws InsufficientPrivilegesException, NotFoundException {
        return _resourceSurveyService.cancelRequests(getSessionUser(), requestIds);
    }

    @Operation(summary = "Cancels the resource survey request for the specified resource", description = "Returns the ID of the canceled resource survey request. If the ID doesn't exist as a resource survey request ID, 404 is returned.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns the ID of the canceled resource survey request for the specified resource ID."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access or administer resource survey requests in the project containing the specified resource."),
                   @ApiResponse(responseCode = "404", description = "Either no resource exists with the specified ID or there are no resource survey requests for that resource."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    // TODO: No restrictTo here because there is no XAPI authorization for resource IDs: see XNAT-7373. The underlying service has to manage permissions checks.
    @XapiRequestMapping(value = "survey/resource/{resourceId}", method = DELETE)
    public long cancelResourceSurveyRequestByResourceId(final @PathVariable int resourceId) throws InsufficientPrivilegesException, NotFoundException {
        return _resourceSurveyService.cancelRequestByResourceId(getSessionUser(), resourceId);
    }

    @Operation(summary = "Cancels the resource survey requests for the specified resources", description = "Returns a map of the resource IDs and canceled resource survey request IDs. If one or more of the IDs doesn't exist as a resource ID, none of the requests are canceled and 404 is returned.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns a map of the resource IDs and canceled resource survey request IDs."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access or administer resource survey requests in the project(s) containing the specified resources."),
                   @ApiResponse(responseCode = "404", description = "Either one or more of the specified IDs are not valid resource IDs or have no associated resource survey requests."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    // TODO: No restrictTo here because there is no XAPI authorization for resource IDs: see XNAT-7373. The underlying service has to manage permissions checks.
    @XapiRequestMapping(value = "survey/resource", method = DELETE)
    public Map<Integer, Long> cancelResourceSurveyRequestsByResourceIds(final @RequestBody List<Integer> resourceIds) throws InsufficientPrivilegesException, NotFoundException {
        return _resourceSurveyService.cancelRequestsByResourceId(getSessionUser(), resourceIds);
    }

    @Operation(summary = "Cancels all open resource survey requests in the specified project", description = "Returns a list of the IDs of the canceled resource survey requests. If the project contains no open resource survey requests to be canceled, an empty list is returned.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns a list of the canceled resource survey requests in the project with the specified ID."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access or administer resource survey requests in the specified project."),
                   @ApiResponse(responseCode = "404", description = "No project exists with the specified ID."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "survey/project/{projectId}", produces = APPLICATION_JSON_VALUE, method = DELETE, restrictTo = AccessLevel.Delete)
    public List<Long> cancelResourceSurveyRequestsByProject(final @PathVariable String projectId) throws InsufficientPrivilegesException, NotFoundException {
        return _resourceSurveyService.cancelRequestsByProjectId(getSessionUser(), projectId);
    }

    @Operation(summary = "Gets the status of survey requests for the specified project that have been created or surveyed but not yet mitigated or otherwise closed", description = "Returns a map containing the ID of each resource in the specified project with at least one resource survey request, along with the status of the latest resource survey request for each resource.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns the status of the latest survey operations for the specified project."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access or administer resource survey requests for the specified project."),
                   @ApiResponse(responseCode = "404", description = "No project exists with the specified ID."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "survey/project/{projectId}/status", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Delete)
    public Map<Integer, ResourceSurveyRequest.Status> getRequestStatus(final @PathVariable String projectId) throws InsufficientPrivilegesException, NotFoundException {
        return _resourceSurveyService.getRequestStatus(getSessionUser(), projectId);
    }

    @Operation(summary = "Gets the survey status for the specified resources", description = "Returns a map containing the ID of each resource with at least one resource survey request, along with the status of the latest resource survey request for each resource. If one or more of the IDs doesn't exist as a resource ID, 404 is returned. If one or more of the existing resources doesn't have any associated resource survey requests, that resource is omitted from the results.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns the resource ID and status of the latest resource survey request for each specified resource."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access or administer resource survey requests in the project(s) containing the specified resources."),
                   @ApiResponse(responseCode = "404", description = "Either one or more of the specified IDs are not valid resource IDs or have no associated resource survey requests."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    // TODO: No restrictTo here because there is no XAPI authorization for resource IDs: see XNAT-7373. The underlying service has to manage permissions checks.
    @XapiRequestMapping(value = "survey/resource/status", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = POST)
    public Map<Integer, ResourceSurveyRequest.Status> getRequestStatus(final @RequestBody List<Integer> resourceIds) throws InsufficientPrivilegesException, NotFoundException {
        return _resourceSurveyService.getRequestStatus(getSessionUser(), resourceIds);
    }

    @Operation(summary = "Gets the survey status for the specified resource", description = "Returns the status of the latest resource survey request for the specified resource. If the ID doesn't exist as a resource ID or there is no resource survey request associated with the specified resource, 404 is returned.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns the status of the latest survey for the specified resource."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access or administer resource survey requests in the project containing the resource."),
                   @ApiResponse(responseCode = "404", description = "Either the specified ID is not a valid resource ID or the resource has no associated resource survey requests."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    // TODO: No restrictTo here because there is no XAPI authorization for resource IDs: see XNAT-7373. The underlying service has to manage permissions checks.
    @XapiRequestMapping(value = "survey/resource/{resourceId}/status", method = GET)
    public ResourceSurveyRequest.Status getRequestStatus(final @PathVariable Integer resourceId) throws InsufficientPrivilegesException, NotFoundException {
        return _resourceSurveyService.getRequestStatus(getSessionUser(), resourceId);
    }

    @Operation(summary = "Gets the survey status for the specified request", description = "Returns the status of the resource survey request with the specified ID. If the ID doesn't exist as a resource survey request ID, 404 is returned.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns the status of the latest survey for the specified resource."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access or administer resource survey requests in the project containing the resource."),
                   @ApiResponse(responseCode = "404", description = "Either the specified ID is not a valid resource ID or the resource has no associated resource survey requests."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "survey/request/{requestId}/status", method = GET)
    public ResourceSurveyRequest.Status getRequestStatus(final @PathVariable long requestId) throws InsufficientPrivilegesException, NotFoundException {
        return _resourceSurveyService.getRequestStatus(getSessionUser(), requestId);
    }

    @Operation(summary = "Downloads all the summarized survey reports associated with the project if a project ID is provided, regardless of their status; otherwise, download all the summarized survey reports for the whole site.", description = "Returns the summarized survey report for a project or the whole site ")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Downloads the summarized survey report for a project or the site."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access or administer resource survey requests."),
                   @ApiResponse(responseCode = "404", description = "No resource survey request exists on the server."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "survey/report/download", method = GET, produces = APPLICATION_OCTET_STREAM_VALUE, restrictTo = AccessLevel.Delete)
    public ResponseEntity<Resource> downloadSurveyReports(final @RequestParam(required = false) String projectId) throws InsufficientPrivilegesException, NotFoundException, InitializationException {
        final List<ResourceSurveyRequest> requests = getResourceSurveyRequests(projectId);
        return requests.isEmpty()
               ? ResponseEntity.noContent().build()
               : ResponseEntity.ok()
                               .contentType(APPLICATION_OCTET_STREAM)
                               .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + ((projectId == null) ? "request.csv" : "request_" + projectId + ".csv") + "\"")
                               .body(new InputStreamResource(new ByteArrayInputStream(_resourceSurveyService.convertRequestsToCsv(requests).getBytes(StandardCharsets.UTF_8))));
    }

    @Operation(summary = "Returns the summarized survey report for the specified project")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Return the summarized survey report for a project or the site."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access or administer resource survey requests."),
                   @ApiResponse(responseCode = "404", description = "No resource survey request exists on the server."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "survey/project/{projectId}/report/summary", method = GET, produces = APPLICATION_JSON_VALUE, restrictTo = AccessLevel.Delete)
    public SurveyReportSummary summarizeSurveyReport(final @PathVariable String projectId) throws InsufficientPrivilegesException, NotFoundException, InitializationException {
        final List<SurveyReportSummary> summaries = summarizeSurveyReports(projectId);
        return summaries.isEmpty() ? null : summaries.getFirst();
    }

    @Operation(summary = "Returns the summarized survey reports for a project or the entire site", description = "The response is limited to a single project if the projectId querystring parameter is specified, but reports for all projects on the site if not.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Return the summarized survey reports for a project or the site."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access or administer resource survey requests."),
                   @ApiResponse(responseCode = "404", description = "No resource survey request exists on the server."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "survey/report/summary", method = GET, produces = APPLICATION_JSON_VALUE, restrictTo = AccessLevel.Delete)
    public List<SurveyReportSummary> summarizeSurveyReports(final @RequestParam(required = false) String projectId) throws InsufficientPrivilegesException, NotFoundException, InitializationException {
        return getResourceSurveyRequests(projectId)
                .stream()
                .collect(Collectors.groupingBy(ResourceSurveyRequest::getProjectId))
                .entrySet()
                .stream()
                .map(ResourceSurveyApi::generateSurveyReportSummary)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Gets the survey reports for resource survey requests associated with the specified project that have been created or surveyed but not yet mitigated or otherwise closed", description = "This call returns a map of resource IDs for the associated resource for each resource survey request in the specified project that have a survey report but not yet mitigated or otherwise closed, along with the survey report itself.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Retrieved the survey reports for the specified project."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access resource mitigation report."),
                   @ApiResponse(responseCode = "404", description = "No project exists for the specified ID."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "survey/project/{projectId}/report", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Delete)
    public Map<Integer, ResourceSurveyReport> getResourceReport(final @PathVariable String projectId) throws InsufficientPrivilegesException, NotFoundException {
        return _resourceSurveyService.getOpenByProjectId(getSessionUser(), projectId).stream().filter(request -> request.getSurveyReport() != null).collect(Collectors.toMap(ResourceSurveyRequest::getResourceId, ResourceSurveyRequest::getSurveyReport));
    }

    @Operation(summary = "Gets the survey report for the latest resource survey request associated with the specified resource")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Retrieved the survey report for the specified resource ID."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access resource mitigation report."),
                   @ApiResponse(responseCode = "404", description = "No resource exists for the specified resource ID."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    // TODO: No restrictTo here because there is no XAPI authorization for resource IDs: see XNAT-7373. The underlying service has to manage permissions checks.
    @XapiRequestMapping(value = "survey/resource/{resourceId}/report", produces = APPLICATION_JSON_VALUE, method = GET)
    public ResourceSurveyReport getResourceReport(final @PathVariable int resourceId) throws InsufficientPrivilegesException, NotFoundException {
        final ResourceSurveyRequest request = _resourceSurveyService.getByResourceId(getSessionUser(), resourceId);
        return Optional.ofNullable(request.getSurveyReport()).orElseThrow(() -> new NotFoundException("The resource survey request ID " + request.getId() + " for resource with ID " + resourceId + " does not have an associated resource survey report."));
    }

    @Operation(summary = "Gets the survey report for the specified resource survey request")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Retrieved the survey report for the specified resource survey request."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access resource survey report."),
                   @ApiResponse(responseCode = "404", description = "No resource survey request exists for the specified request ID."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "survey/request/{requestId}/report", produces = APPLICATION_JSON_VALUE, method = GET)
    public ResourceSurveyReport getRequestReport(final @PathVariable long requestId) throws InsufficientPrivilegesException, NotFoundException {
        return Optional.ofNullable(_resourceSurveyService.getById(getSessionUser(), requestId).getSurveyReport()).orElseThrow(() -> new NotFoundException("The resource survey request with ID " + requestId + " does not have an associated resource survey report."));
    }

    @Operation(summary = "Clean the survey and mitigation reports for all resource survey requests associated with the specified resource", description = "This removes filenames and other possible sources of PHI from the survey and mitigation reports. The optional querystring parameters \"reason\" and \"comment\" are used when committing the workflow entries recording the report cleaning.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Cleaned all survey and mitigation reports for the specified resource ID."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access resource mitigation report."),
                   @ApiResponse(responseCode = "404", description = "No resource exists for the specified resource ID."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    // TODO: No restrictTo here because there is no XAPI authorization for resource IDs: see XNAT-7373. The underlying service has to manage permissions checks.
    @XapiRequestMapping(value = "survey/resource/{resourceId}/report", produces = APPLICATION_JSON_VALUE, method = DELETE)
    public void cleanResourceReports(final @PathVariable int resourceId,
                                     final @RequestParam(required = false) String reason,
                                     final @RequestParam(required = false) String comment) throws InsufficientPrivilegesException, NotFoundException {
        _resourceSurveyService.cleanResourceReports(getSessionUser(), resourceId, reason, comment);
    }

    @Operation(summary = "Clean the survey and mitigation reports for the specified resource survey request", description = "This removes filenames and other possible sources of PHI from the survey and mitigation reports. The optional querystring parameters \"reason\" and \"comment\" are used when committing the workflow entries recording the report cleaning.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Cleaned the survey and mitigation reports for the specified resource survey request."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access resource survey report."),
                   @ApiResponse(responseCode = "404", description = "No resource survey request exists for the specified request ID."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "survey/request/{requestId}/report", produces = APPLICATION_JSON_VALUE, method = DELETE)
    public void cleanRequestReports(final @PathVariable long requestId,
                                    final @RequestParam(required = false) String reason,
                                    final @RequestParam(required = false) String comment) throws InsufficientPrivilegesException, NotFoundException {
        _resourceSurveyService.cleanRequestReports(getSessionUser(), requestId, reason, comment);
    }

    @Operation(summary = "Queues resource survey requests for the specified project for mitigation operations", description = "Returns a list of IDs for the queued requests. The optional querystring parameters \"reason\" and \"comment\" are used when committing the workflow entries recording the file mitigation.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns a list of IDs for newly resource survey requests for the specified project."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access or administer resource survey requests for the specified project."),
                   @ApiResponse(responseCode = "404", description = "No project exists with the specified ID."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "mitigate/project/{projectId}", produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = AccessLevel.Delete)
    public List<Long> mitigateProjectResources(final @PathVariable String projectId,
                                               final @RequestParam(required = false) String reason,
                                               final @RequestParam(required = false) String comment) throws InsufficientPrivilegesException, NotFoundException {
        return _resourceSurveyService.queueProjectMitigation(getSessionUser(), projectId, reason, comment);
    }

    @Operation(summary = "Queues the latest open resource survey request for the specified resource for mitigation", description = "Returns the ID of the queued request. If the specified ID doesn't exist as a resource ID or no open resource survey request is associated with the specified resource, 404 is returned. The optional querystring parameters \"reason\" and \"comment\" are used when committing the workflow entries recording the file mitigation.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns the ID of the newly queued resource survey request."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access or administer resource survey requests in the project containing the specified resource."),
                   @ApiResponse(responseCode = "404", description = "Either the specified ID is not a valid resource ID or the resource has no associated resource survey requests."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    // TODO: No restrictTo here because there is no XAPI authorization for resource IDs: see XNAT-7373. The underlying service has to manage permissions checks.
    @XapiRequestMapping(value = "mitigate/resource/{resourceId}", produces = APPLICATION_JSON_VALUE, method = POST)
    public long mitigateResource(final @PathVariable int resourceId,
                                 final @RequestParam(required = false) String reason,
                                 final @RequestParam(required = false) String comment) throws InsufficientPrivilegesException, NotFoundException, ConflictedStateException {
        return _resourceSurveyService.queueResourceMitigation(getSessionUser(), resourceId, reason, comment);
    }

    @Operation(summary = "Queues the latest open resource survey requests for the specified resources for mitigation", description = "Returns a map with a list of IDs for newly queued requests as \"queued\" and non-queued requests under \"invalid\" and \"forbidden\".")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns a map with a list of IDs for newly queued requests as \"queued\" and non-queued requests under \"invalid\" and \"forbidden\"."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access or administer resource survey requests in the project(s) containing the specified resource."),
                   @ApiResponse(responseCode = "404", description = "Either one or more ID are not valid resource IDs or have no associated resource survey requests."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    // TODO: No restrictTo here because there is no XAPI authorization for resource IDs: see XNAT-7373. The underlying service has to manage permissions checks.
    @XapiRequestMapping(value = "mitigate/resources", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = POST)
    public Map<String, Collection<Long>> mitigateResources(final @RequestBody List<Integer> resourceIds) throws NotFoundException, ConflictedStateException {
        return _resourceSurveyService.queueResourceMitigation(getSessionUser(), resourceIds);
    }

    @Operation(summary = "Queues the resource survey requests from the uploaded csv file for mitigation")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns a list of resource survey requests for the specified project."),
                   @ApiResponse(responseCode = "204", description = "No resources were specified."),
                   @ApiResponse(responseCode = "400", description = "Something is wrong with the request format."),
                   @ApiResponse(responseCode = "403", description = "The user is not authorized to access one or more of the specified resources."),
                   @ApiResponse(responseCode = "404", description = "The request was valid but one or more of the specified resources was not found."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    // TODO: No restrictTo here because there is no XAPI authorization for resource IDs: see XNAT-7373. The underlying service has to manage permissions checks.
    @XapiRequestMapping(value = "mitigate/resources/csv", consumes = MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE, method = POST)
    public Map<String, Collection<Long>> mitigateResources(@RequestParam final MultipartFile uploadFile) throws NoContentException, DataFormatException, NotFoundException, ServerException, ConflictedStateException {
        return _resourceSurveyService.queueResourceMitigation(getSessionUser(), _resourceSurveyService.getResourceIds(uploadFile));
    }

    @Operation(summary = "Queues the specified resource survey request for mitigation", description = "Returns the ID of the queued request. If the specified ID doesn't exist as a resource survey request, 404 is returned. If the resource survey request exists but the status is not DIVERGENT, 400 is returned. The optional querystring parameters \"reason\" and \"comment\" are used when committing the workflow entries recording the file mitigation.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns the ID of the newly queued resource survey request."),
                   @ApiResponse(responseCode = "403", description = "The resource survey request is not in a divergent state so mitigation can't be performed."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access or administer resource survey requests in the project containing the specified resource."),
                   @ApiResponse(responseCode = "404", description = "Either the specified ID is not a valid resource survey request."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "mitigate/request/{requestId}", produces = APPLICATION_JSON_VALUE, method = POST)
    public long mitigateRequest(final @PathVariable long requestId,
                                final @RequestParam(required = false) String reason,
                                final @RequestParam(required = false) String comment) throws InsufficientPrivilegesException, NotFoundException, ConflictedStateException {
        return _resourceSurveyService.queueMitigation(getSessionUser(), requestId, reason, comment);
    }

    @Operation(summary = "Get the mitigation reports for the latest resource survey requests for the specified project", description = "Returns a list of mitigation reports for the project.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns a list of mitigation reports for the specified project."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access resource mitigation report."),
                   @ApiResponse(responseCode = "404", description = "No mitigation report exists for the specified resource ID."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "mitigate/project/{projectId}/report", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Delete)
    public List<ResourceMitigationReport> getMitigationReports(final @PathVariable String projectId) throws InsufficientPrivilegesException, NotFoundException {
        return _resourceSurveyService.getByProjectIdAndStatus(getSessionUser(), projectId, ResourceSurveyRequest.Status.CONFORMING)
                                     .stream()
                                     .map(ResourceSurveyRequest::getMitigationReport)
                                     .filter(Objects::nonNull)
                                     .collect(Collectors.toList());
    }

    @Operation(summary = "Get the mitigation report for the specified resource", description = "Returns the requested mitigation report.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns the mitigation report for the specified resource ID."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access resource mitigation report."),
                   @ApiResponse(responseCode = "404", description = "No mitigation report exists for the specified resource ID."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    // TODO: No restrictTo here because there is no XAPI authorization for resource IDs: see XNAT-7373. The underlying service has to manage permissions checks.
    @XapiRequestMapping(value = "mitigate/resource/{resourceId}/report", produces = APPLICATION_JSON_VALUE, method = GET)
    public ResourceMitigationReport getMitigationReport(final @PathVariable int resourceId) throws InsufficientPrivilegesException, NotFoundException, ConflictedStateException {
        final ResourceSurveyRequest.Status status = _resourceSurveyService.getRequestStatus(getSessionUser(), resourceId);
        if (status == ResourceSurveyRequest.Status.CONFORMING) {
            return _resourceSurveyService.getByResourceId(getSessionUser(), resourceId).getMitigationReport();
        }
        throw new ConflictedStateException("Mitigation is not completed for resource " + resourceId + ", current status is: " + status);
    }

    @Operation(summary = "Get the mitigation report for the specified resource survey request", description = "Returns the requested mitigation report.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns the mitigation report for the specified resource survey request."),
                   @ApiResponse(responseCode = "403", description = "Insufficient permissions to access resource mitigation report."),
                   @ApiResponse(responseCode = "404", description = "No mitigation report exists for the specified resource ID."),
                   @ApiResponse(responseCode = "500", description = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "mitigate/request/{requestId}/report", produces = APPLICATION_JSON_VALUE, method = GET)
    public ResourceMitigationReport getMitigationReport(final @PathVariable long requestId) throws InsufficientPrivilegesException, NotFoundException, ConflictedStateException {
        final ResourceSurveyRequest.Status status = _resourceSurveyService.getRequestStatus(getSessionUser(), requestId);
        if (status == ResourceSurveyRequest.Status.CONFORMING) {
            return _resourceSurveyService.getById(getSessionUser(), requestId).getMitigationReport();
        }
        throw new ConflictedStateException("Mitigation is not completed for resource survey request " + requestId + ", current status is: " + status);
    }

    private static LocalDateTime parseDate(final String since) {
        return StringUtils.isBlank(since) ? null : LocalDateTime.of(LocalDate.parse(since), LocalTime.MIN);
    }

    @NotNull
    private List<ResourceSurveyRequest> getResourceSurveyRequests(@Nullable String projectId) throws InitializationException, InsufficientPrivilegesException, NotFoundException {
        List<ResourceSurveyRequest> requests = (projectId == null)
                                               ? _resourceSurveyService.getAllResourceSurveyRequests(getSessionUser())
                                               : _resourceSurveyService.getAllByProjectId(getSessionUser(), projectId);
        if (requests == null || requests.isEmpty()) {
            throw new NotFoundException("There is no data %s on the server".formatted((projectId == null) ? " " : "for project " + projectId));
        }
        return requests;
    }

    @NotNull
    private static SurveyReportSummary generateSurveyReportSummary(final Map.Entry<String, List<ResourceSurveyRequest>> entry) {
        final AtomicInteger totalEntries         = new AtomicInteger();
        final AtomicInteger totalUids            = new AtomicInteger();
        final AtomicInteger totalDuplicates      = new AtomicInteger();
        final AtomicInteger totalBadFiles        = new AtomicInteger();
        final AtomicInteger totalMismatchedFiles = new AtomicInteger();
        final AtomicInteger totalMovedFiles      = new AtomicInteger();
        final AtomicInteger totalRemovedFiles    = new AtomicInteger();
        final AtomicInteger totalFileErrors      = new AtomicInteger();
        final AtomicLong    nonconformingCount   = new AtomicLong();
        entry.getValue().forEach(request -> {
            final ResourceSurveyReport surveyReport = request.getSurveyReport();
            if (surveyReport != null) {
                totalEntries.addAndGet(surveyReport.getTotalEntries());
                totalUids.addAndGet(surveyReport.getTotalUids());
                totalDuplicates.addAndGet(surveyReport.getTotalDuplicates());
                totalBadFiles.addAndGet(surveyReport.getTotalBadFiles());
                totalMismatchedFiles.addAndGet(surveyReport.getTotalMismatchedFiles());
            }
            final ResourceMitigationReport mitigationReport = request.getMitigationReport();
            if (mitigationReport != null) {
                totalMovedFiles.addAndGet(mitigationReport.getTotalMovedFiles());
                totalRemovedFiles.addAndGet(mitigationReport.getTotalRemovedFiles());
                totalFileErrors.addAndGet(mitigationReport.getTotalFileErrors());
            }
            if (!ResourceSurveyRequest.Status.CLOSING_VALUES.contains(request.getRsnStatus())) {
                nonconformingCount.incrementAndGet();
            }
        });
        return new SurveyReportSummary(entry.getKey(), totalEntries.get(), totalUids.get(), totalDuplicates.get(),
                                       totalBadFiles.get(), totalMismatchedFiles.get(), totalMovedFiles.get(),
                                       totalRemovedFiles.get(), totalFileErrors.get(),
                                       nonconformingCount.get() > 0 ? "IN_PROGRESS" : "CONFORMING");
    }

    @Data
    @AllArgsConstructor
    private static class SurveyReportSummary {
        private String projectId;
        private int    totalEntries;
        private int    totalUids;
        private int    totalDuplicates;
        private int    totalBadFiles;
        private int    totalMismatchedFiles;
        private int    totalMovedFiles;
        private int    totalRemovedFiles;
        private int    totalFileErrors;
        private String overallStatus;
    }
}
