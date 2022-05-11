package org.nrg.xapi.rest.customfields;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.*;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.customfields.CustomFieldService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Api("Custom Fields API")
@XapiRestController
@RequestMapping(value = "/custom_fields")
@Slf4j
public class CustomFieldsApi extends AbstractXapiRestController {
    private final CustomFieldService customFieldService;

    protected CustomFieldsApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder,
                              final CustomFieldService customFieldService) {
        super(userManagementService, roleHolder);
        this.customFieldService = customFieldService;
    }

    @ResponseBody
    @ApiOperation(value = "Get custom fields for an item.")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of custom fields."),
            @ApiResponse(code = 500, message = "An unexpected error occurred."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 404, message = "Item not found.")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, method = GET, value = {
            "/projects/{project}/fields",
            "/projects/{project}/experiments/{experiment}/fields",
            "/projects/{project}/experiments/{experiment}/scans/{scan}/fields",
            "/projects/{project}/experiments/{experiment}/assessors/{assessor}/fields",
            "/projects/{project}/subjects/{subject}/fields",
            "/projects/{project}/subjects/{subject}/experiments/{experiment}/fields",
            "/projects/{project}/subjects/{subject}/experiments/{experiment}/scans/{scan}/fields",
            "/projects/{project}/subjects/{subject}/experiments/{experiment}/assessors/{assessor}/fields",
            "/subjects/{subject}/fields",
            "/experiments/{experiment}/fields",
            "/experiments/{experiment}/assessors/{assessor}/fields",
            "/experiments/{experiment}/scans/{scan}/fields"})
    public JsonNode getFields(@PathVariable final Map<String, String> pathVariables,
                              @RequestParam(required = false) final List<String> keys) throws NotFoundException {
        final UserI user = XDAT.getUserDetails();
        final ItemI item = getItem(user, pathVariables);
        return customFieldService.getFields(item, keys);
    }

    @ResponseBody
    @ApiOperation(value = "Get the text value of a custom field.")
    @ApiResponses({@ApiResponse(code = 200, message = "A custom field value."),
            @ApiResponse(code = 500, message = "An unexpected error occurred."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 404, message = "Item not found.")})
    @XapiRequestMapping(produces = TEXT_PLAIN_VALUE, method = GET, value = {
            "/projects/{project}/fields/{key}",
            "/projects/{project}/experiments/{experiment}/fields/{key}",
            "/projects/{project}/experiments/{experiment}/scans/{scan}/fields/{key}",
            "/projects/{project}/experiments/{experiment}/assessors/{assessor}/fields/{key}",
            "/projects/{project}/subjects/{subject}/fields/{key}",
            "/projects/{project}/subjects/{subject}/experiments/{experiment}/fields/{key}",
            "/projects/{project}/subjects/{subject}/experiments/{experiment}/scans/{scan}/fields/{key}",
            "/projects/{project}/subjects/{subject}/experiments/{experiment}/assessors/{assessor}/fields/{key}",
            "/subjects/{subject}/fields/{key}",
            "/experiments/{experiment}/fields/{key}",
            "/experiments/{experiment}/assessors/{assessor}/fields/{key}",
            "/experiments/{experiment}/scans/{scan}/fields/{key}"})
    public String getFieldValue(@PathVariable final Map<String, String> pathVariables,
                                @PathVariable final String key) throws NotFoundException {
        final UserI user = XDAT.getUserDetails();
        final ItemI item = getItem(user, pathVariables);
        final JsonNode field = customFieldService.getFieldValue(item, key);
        return field.isTextual() ? field.textValue() : field.toString();
    }

    @ResponseBody
    @ApiOperation(value = "Delete a custom field.")
    @ApiResponses({@ApiResponse(code = 200, message = "Field deleted."),
            @ApiResponse(code = 500, message = "An unexpected error occurred."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 403, message = "Not allowed."),
            @ApiResponse(code = 404, message = "Item not found.")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, method = DELETE, value = {
            "/projects/{project}/fields/{key}",
            "/projects/{project}/experiments/{experiment}/fields/{key}",
            "/projects/{project}/experiments/{experiment}/scans/{scan}/fields/{key}",
            "/projects/{project}/experiments/{experiment}/assessors/{assessor}/fields/{key}",
            "/projects/{project}/subjects/{subject}/fields/{key}",
            "/projects/{project}/subjects/{subject}/experiments/{experiment}/fields/{key}",
            "/projects/{project}/subjects/{subject}/experiments/{experiment}/scans/{scan}/fields/{key}",
            "/projects/{project}/subjects/{subject}/experiments/{experiment}/assessors/{assessor}/fields/{key}",
            "/subjects/{subject}/fields/{key}",
            "/experiments/{experiment}/fields/{key}",
            "/experiments/{experiment}/assessors/{assessor}/fields/{key}",
            "/experiments/{experiment}/scans/{scan}/fields/{key}"})
    public JsonNode deleteField(@PathVariable final Map<String, String> pathVariables,
                                @PathVariable final String key)
            throws NotFoundException, InsufficientPrivilegesException, NrgServiceException {
        final UserI user = XDAT.getUserDetails();
        final ItemI item = getItem(user, pathVariables);
        return customFieldService.removeField(XDAT.getUserDetails(), item, key);
    }

    @ResponseBody
    @ApiOperation(value = "Set the value for one or more custom fields.")
    @ApiResponses({@ApiResponse(code = 200, message = "The updated list of fields."),
            @ApiResponse(code = 500, message = "An unexpected error occurred."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 403, message = "Not allowed."),
            @ApiResponse(code = 404, message = "Item not found.")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE, method = PUT, value = {
            "/projects/{project}/fields",
            "/projects/{project}/experiments/{experiment}/fields",
            "/projects/{project}/experiments/{experiment}/scans/{scan}/fields",
            "/projects/{project}/experiments/{experiment}/assessors/{assessor}/fields",
            "/projects/{project}/subjects/{subject}/fields",
            "/projects/{project}/subjects/{subject}/experiments/{experiment}/fields",
            "/projects/{project}/subjects/{subject}/experiments/{experiment}/scans/{scan}/fields",
            "/projects/{project}/subjects/{subject}/experiments/{experiment}/assessors/{assessor}/fields",
            "/subjects/{subject}/fields",
            "/experiments/{experiment}/fields",
            "/experiments/{experiment}/assessors/{assessor}/fields",
            "/experiments/{experiment}/scans/{scan}/fields"})
    public JsonNode updateFields(@PathVariable final Map<String, String> pathVariables,
                                 @RequestBody final JsonNode newFields)
            throws NotFoundException, InsufficientPrivilegesException, NrgServiceException {
        final UserI user = XDAT.getUserDetails();
        final ItemI item = getItem(user, pathVariables);
        return customFieldService.setFields(user, item, newFields);
    }

    @ResponseStatus(value = BAD_REQUEST)
    @ExceptionHandler(UnsupportedOperationException.class)
    public String handleUnsupportedOperationException(final UnsupportedOperationException exception) {
        final String message = "Unsupported Operation: " + exception.getMessage();
        log.debug(message);
        return message;
    }

    /**
     * Helper method that tries to figure out which item the user is trying to modify based on the supplied parameters
     *
     * @param user       - The user doing the action
     * @param parameters - The map of parameters
     * @return
     * @throws NotFoundException if we can't find an item that matches.
     */
    private ItemI getItem(final UserI user, final Map<String, String> parameters) throws NotFoundException {
        final String project = parameters.get("project");
        final String subject = parameters.get("subject");
        final String experiment = parameters.get("experiment");
        final String assessor = parameters.get("assessor");
        final String scan = parameters.get("scan");

        if (!StringUtils.isEmpty(experiment)) {
            final XnatExperimentdata experimentData = getExperiment(user, project, subject, experiment);
            if (!StringUtils.isEmpty(assessor)) {
                if (experimentData instanceof XnatImagesessiondata) {
                    return getAssessor((XnatImagesessiondata) experimentData, assessor);
                }
                throw new NotFoundException("Unable to identify assessor: " + assessor);
            }
            if (!StringUtils.isEmpty(scan)) {
                if (experimentData instanceof XnatImagesessiondata) {
                    return getScan((XnatImagesessiondata) experimentData, scan);
                }
                throw new NotFoundException("Unable to identify scan: " + scan + " for experiment: " + experiment);
            }
            return experimentData;
        }

        if (!StringUtils.isEmpty(subject)) {
            return getSubject(user, project, subject);
        }

        if (!StringUtils.isEmpty(project)) {
            return getProject(user, project);
        }

        throw new NotFoundException("Unable to find item");
    }

    private XnatImagescandata getScan(final XnatImagesessiondata session, final String scan) throws NotFoundException {
        final XnatImagescandata scanData = session.getScanById(scan);
        if (null != scanData) {
            return scanData;
        }
        throw new NotFoundException("Unable to identify scan: " + scan + " for experiment: " + session.getId());
    }

    private XnatExperimentdata getAssessor(final XnatImagesessiondata session, final String assessor) throws NotFoundException {
        final XnatExperimentdata assessorById = session.getAssessorById(assessor);
        if (null != assessorById) {
            return assessorById;
        }

        final XnatExperimentdata assessorByLabel = session.getAssessorByLabel(assessor);
        if (null != assessorByLabel) {
            return assessorByLabel;
        }
        throw new NotFoundException("Unable to identify assessor: " + assessor);
    }

    private XnatExperimentdata getExperiment(final UserI user, final String project, final String subject, final String experiment) throws NotFoundException {
        final XnatExperimentdata experimentData = StringUtils.isEmpty(project) ?
                XnatExperimentdata.getXnatExperimentdatasById(experiment, user, false) :
                XnatExperimentdata.GetExptByProjectIdentifier(project, experiment, user, false);

        if (null != experimentData) {
            if (!StringUtils.isEmpty(subject)) {
                // Verify the experiment belongs to the subject specified by the user.
                if (!subject.equals(((XnatSubjectassessordata) experimentData).getSubjectData().getLabel())) {
                    throw new NotFoundException("Unable to identify experiment: " + experiment);
                }
            }
            return experimentData;
        }
        throw new NotFoundException("Unable to identify experiment: " + experiment);
    }

    private XnatSubjectdata getSubject(final UserI user, final String project, final String subject) throws NotFoundException {
        final XnatSubjectdata subjectData = StringUtils.isEmpty(project) ?
                XnatSubjectdata.getXnatSubjectdatasById(subject, user, false) :
                XnatSubjectdata.GetSubjectByProjectIdentifier(project, subject, user, false);

        if (null != subjectData) {
            return subjectData;
        }
        throw new NotFoundException("Unable to identify subject: " + subject);
    }

    private XnatProjectdata getProject(final UserI user, final String project) throws NotFoundException {
        final XnatProjectdata projectData = XnatProjectdata.getProjectByIDorAlias(project, user, false);
        if (null != projectData) {
            return projectData;
        }
        throw new NotFoundException("Unable to identify project: " + project);
    }
}
