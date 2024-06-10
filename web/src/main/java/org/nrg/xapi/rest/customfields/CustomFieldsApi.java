package org.nrg.xapi.rest.customfields;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.Experiment;
import org.nrg.xapi.rest.Project;
import org.nrg.xapi.rest.Subject;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectassessordata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.om.base.auto.AutoXnatProjectdata;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.customfields.CustomFieldService;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@Api("Custom Fields API")
@XapiRestController
@RequestMapping(value = "/custom-fields")
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
            @ApiResponse(code = 403, message = "Cannot access data item."),
            @ApiResponse(code = 404, message = "Item not found.")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Read, value = {
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
    public JsonNode getFields(@ApiParam("The project") @PathVariable(required = false) @Project final String project,
                              @ApiParam("The subject") @PathVariable(required = false) @Subject final String subject,
                              @ApiParam("The experiment") @PathVariable(required = false) @Experiment final String experiment,
                              @ApiParam("The scan") @PathVariable(required = false) final String scan,
                              @ApiParam("The assessor") @PathVariable(required = false) final String assessor,
                              @RequestParam(required = false) final List<String> fieldNames) throws NotFoundException {
        final UserI user = getSessionUser();
        final ItemI item = getItem(user, project, subject, experiment, scan, assessor);
        return customFieldService.getFields(item, fieldNames);
    }

    @ResponseBody
    @ApiOperation(value = "Get the text value of a custom field.")
    @ApiResponses({@ApiResponse(code = 200, message = "A custom field value."),
            @ApiResponse(code = 500, message = "An unexpected error occurred."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 403, message = "Cannot access data item."),
            @ApiResponse(code = 404, message = "Item not found.")})
    @XapiRequestMapping(produces = TEXT_PLAIN_VALUE, method = GET, restrictTo = AccessLevel.Read, value = {
            "/projects/{project}/fields/{fieldName}",
            "/projects/{project}/experiments/{experiment}/fields/{fieldName}",
            "/projects/{project}/experiments/{experiment}/scans/{scan}/fields/{fieldName}",
            "/projects/{project}/experiments/{experiment}/assessors/{assessor}/fields/{fieldName}",
            "/projects/{project}/subjects/{subject}/fields/{fieldName}",
            "/projects/{project}/subjects/{subject}/experiments/{experiment}/fields/{fieldName}",
            "/projects/{project}/subjects/{subject}/experiments/{experiment}/scans/{scan}/fields/{fieldName}",
            "/projects/{project}/subjects/{subject}/experiments/{experiment}/assessors/{assessor}/fields/{fieldName}",
            "/subjects/{subject}/fields/{fieldName}",
            "/experiments/{experiment}/fields/{fieldName}",
            "/experiments/{experiment}/assessors/{assessor}/fields/{fieldName}",
            "/experiments/{experiment}/scans/{scan}/fields/{fieldName}"})
    public String getFieldValue(@ApiParam("The project") @PathVariable(required = false) @Project final String project,
                                @ApiParam("The subject") @PathVariable(required = false) @Subject final String subject,
                                @ApiParam("The experiment") @PathVariable(required = false) @Experiment final String experiment,
                                @ApiParam("The scan") @PathVariable(required = false) final String scan,
                                @ApiParam("The assessor") @PathVariable(required = false) final String assessor,
                                @PathVariable final String fieldName) throws NotFoundException {
        final UserI user = getSessionUser();
        final ItemI item = getItem(user, project, subject, experiment, scan, assessor);
        final JsonNode field = customFieldService.getFieldValue(item, fieldName);
        return field.isTextual() ? field.textValue() : field.toString();
    }

    @ResponseBody
    @ApiOperation(value = "Delete a custom field.")
    @ApiResponses({@ApiResponse(code = 200, message = "Field deleted."),
            @ApiResponse(code = 500, message = "An unexpected error occurred."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 403, message = "Not allowed."),
            @ApiResponse(code = 404, message = "Item not found.")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, method = DELETE, restrictTo = AccessLevel.Edit, value = {
            "/projects/{project}/fields/{fieldName}",
            "/projects/{project}/experiments/{experiment}/fields/{fieldName}",
            "/projects/{project}/experiments/{experiment}/scans/{scan}/fields/{fieldName}",
            "/projects/{project}/experiments/{experiment}/assessors/{assessor}/fields/{fieldName}",
            "/projects/{project}/subjects/{subject}/fields/{fieldName}",
            "/projects/{project}/subjects/{subject}/experiments/{experiment}/fields/{fieldName}",
            "/projects/{project}/subjects/{subject}/experiments/{experiment}/scans/{scan}/fields/{fieldName}",
            "/projects/{project}/subjects/{subject}/experiments/{experiment}/assessors/{assessor}/fields/{fieldName}",
            "/subjects/{subject}/fields/{fieldName}",
            "/experiments/{experiment}/fields/{fieldName}",
            "/experiments/{experiment}/assessors/{assessor}/fields/{fieldName}",
            "/experiments/{experiment}/scans/{scan}/fields/{fieldName}"})
    public JsonNode deleteField(@ApiParam("The project") @PathVariable(required = false) @Project final String project,
                                @ApiParam("The subject") @PathVariable(required = false) @Subject final String subject,
                                @ApiParam("The experiment") @PathVariable(required = false) @Experiment final String experiment,
                                @ApiParam("The scan") @PathVariable(required = false) final String scan,
                                @ApiParam("The assessor") @PathVariable(required = false) final String assessor,
                                @PathVariable final String fieldName)
            throws NotFoundException, InsufficientPrivilegesException, NrgServiceException {
        final UserI user = getSessionUser();
        final ItemI item = getItem(user, project, subject, experiment, scan, assessor);
        return customFieldService.removeField(user, item, fieldName);
    }

    @ResponseBody
    @ApiOperation(value = "Set the value for one or more custom fields.")
    @ApiResponses({@ApiResponse(code = 200, message = "The updated list of fields."),
            @ApiResponse(code = 500, message = "An unexpected error occurred."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 403, message = "Not allowed."),
            @ApiResponse(code = 404, message = "Item not found.")})
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE, method = PUT, restrictTo = AccessLevel.Edit, value = {
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
    public JsonNode updateFields(@ApiParam("The project") @PathVariable(required = false) @Project final String project,
                                 @ApiParam("The subject") @PathVariable(required = false) @Subject final String subject,
                                 @ApiParam("The experiment") @PathVariable(required = false) @Experiment final String experiment,
                                 @ApiParam("The scan") @PathVariable(required = false) final String scan,
                                 @ApiParam("The assessor") @PathVariable(required = false) final String assessor,
                                 @RequestBody final JsonNode newFields)
            throws NotFoundException, InsufficientPrivilegesException, NrgServiceException {
        final UserI user = getSessionUser();
        final ItemI item = getItem(user, project, subject, experiment, scan, assessor);
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
     * @param project    - The project
     * @param subject    - The subject
     * @param experiment - The experiment
     * @param scan       - The scan
     * @param assessor   - The assessor
     * @return
     * @throws NotFoundException if we can't find an item that matches.
     */
    private ItemI getItem(final UserI user, final String project, final String subject,
                          final String experiment, final String scan, final String assessor) throws NotFoundException {

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
        XnatExperimentdata experimentData;
        if (StringUtils.isEmpty(project)) {
            // If the project wasn't specified, try to get the experiment by id
            experimentData = XnatExperimentdata.getXnatExperimentdatasById(experiment, user, false);
        } else {
            // If the project was specified, try to get the experiment by project id + experiment label
            experimentData = XnatExperimentdata.GetExptByProjectIdentifier(project, experiment, user, false);
            if (null == experimentData) {
                // If we couldn't find it, try to find the experiment by id.
                experimentData = XnatExperimentdata.getXnatExperimentdatasById(experiment, user, false);
            }
        }

        // If we found an experiment, verify the project actually matches since we could have found the experiment by id.
        if (null == experimentData || (!StringUtils.isEmpty(project) && !project.equals(experimentData.getProject()))) {
            throw new NotFoundException("Unable to identify experiment: " + experiment);
        }

        if (!StringUtils.isEmpty(subject)) {
            // If the user specified a subject, we need to verify that the subject matches
            // either the experiment's subject id or subject label. 
            if (!subject.equals(((XnatSubjectassessordata) experimentData).getSubjectData().getLabel()) &&
                    !subject.equals(((XnatSubjectassessordata) experimentData).getSubjectId())) {
                throw new NotFoundException("Unable to identify experiment: " + experiment);
            }
        }
        return experimentData;
    }

    private XnatSubjectdata getSubject(final UserI user, final String project, final String subject) throws NotFoundException {
        XnatSubjectdata subjectData;
        if (StringUtils.isEmpty(project)) {
            // If the project wasn't specified, try to get the subject by id
            subjectData = XnatSubjectdata.getXnatSubjectdatasById(subject, user, false);
        } else {
            // If the project was specified, try to get the subject by project id + subject label
            subjectData = XnatSubjectdata.GetSubjectByProjectIdentifier(project, subject, user, false);
            if (null == subjectData) {
                // If we couldn't find it, try to find the subject by id.
                subjectData = XnatSubjectdata.getXnatSubjectdatasById(subject, user, false);
            }
        }

        // If we found a subject, verify the project actually matches since we could have found the subject by id.
        if (null == subjectData || (!StringUtils.isEmpty(project) && !project.equals(subjectData.getProject()))) {
            throw new NotFoundException("Unable to identify experiment: " + subject);
        }
        return subjectData;
    }

    private XnatProjectdata getProject(final UserI user, final String projectId) throws NotFoundException {
        final XnatProjectdata project = AutoXnatProjectdata.getXnatProjectdatasById(projectId, user, false);
        if (null != project) {
            return project;
        } else {
            final List<XnatProjectdata> matches = AutoXnatProjectdata.getXnatProjectdatasByField("xnat:projectData/aliases/alias/alias", projectId, user, false);
            if (matches != null && !matches.isEmpty()) {
                return matches.get(0);
            }
        }
        throw new NotFoundException("Unable to identify project: " + project);
    }
}
