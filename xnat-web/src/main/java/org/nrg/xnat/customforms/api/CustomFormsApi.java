/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * @author: Mohana Ramaratnam (mohana@radiologics.com)
 * @since: 07-03-2021
 */

package org.nrg.xnat.customforms.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.AuthorizedRoles;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.customforms.exceptions.CustomVariableNameClashException;
import org.nrg.xnat.customforms.exceptions.InsufficientPermissionsException;
import org.nrg.xnat.customforms.pojo.ClientPojo;
import org.nrg.xnat.customforms.pojo.SubmissionPojo;
import org.nrg.xnat.customforms.pojo.XnatFormsIOEnv;
import org.nrg.xnat.customforms.pojo.formio.FormAppliesToPoJo;
import org.nrg.xnat.customforms.pojo.formio.PseudoConfiguration;
import org.nrg.xnat.customforms.pojo.formio.RowIdentifier;
import org.nrg.xnat.customforms.service.CustomFormManagerService;
import org.nrg.xnat.customforms.service.CustomFormPermissionsService;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.DataFormatException;

import static org.nrg.xdat.security.helpers.AccessLevel.Role;

@XapiRestController
@RequestMapping(value = "/customforms")
@Api("Custom Forms FormsIO API")
@Slf4j
public class CustomFormsApi extends AbstractXapiRestController {

    private final CustomFormManagerService formManagerService;
    private final CustomFormPermissionsService permissionsService;
    private final ObjectMapper objectMapper;
    private final ObjectMapper objectMapperNoFailOnUnknown;
    private static final String[] STRINGS_FORBIDDEN_CHARS = new String[] {"<", "&lt;", ">", "&gt;"};

    @Autowired
    public CustomFormsApi(final UserManagementServiceI userManagementService,
                          final CustomFormManagerService formManagerService,
                          final RoleHolder roleHolder,
                          final CustomFormPermissionsService permissionsService,
                          final ObjectMapper objectMapper
    ) {
        super(userManagementService, roleHolder);
        this.formManagerService = formManagerService;
        this.permissionsService = permissionsService;

        this.objectMapper = objectMapper;

        objectMapperNoFailOnUnknown = objectMapper.copy();
        objectMapperNoFailOnUnknown.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapperNoFailOnUnknown.setVisibility(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
    }

    @ApiOperation(value = "Accepts a JSON", notes = "Accepts a JSON", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.PUT, restrictTo = Role)
    @AuthorizedRoles({CustomFormsConstants.ADMIN_ROLE, CustomFormsConstants.FORM_MANAGER_ROLE})
    public ResponseEntity<String> addCustomFormsToProtocolsAndProjects(final @RequestBody String jsonbody)
            throws JsonProcessingException {
        final UserI user = getSessionUser();
        ClientPojo clientPojo = objectMapperNoFailOnUnknown.readValue(jsonbody, ClientPojo.class);
        String problem = clientPojo.validate();
        if (StringUtils.isNotBlank(problem)) {
            return new ResponseEntity<>("Invalid form submission: " + problem, HttpStatus.BAD_REQUEST);
        }
        final SubmissionPojo submission = clientPojo.getSubmission().getData();
        final JsonNode proposedFormDefinition = objectMapper.readTree(clientPojo.getBuilder());
        try {
            final String formTitle = proposedFormDefinition.get("title").asText();
            if (StringUtils.isBlank(formTitle) || StringUtils.containsAny(formTitle, STRINGS_FORBIDDEN_CHARS)) {
                return new ResponseEntity<>("Invalid characters in form title", HttpStatus.BAD_REQUEST);
            }
        } catch(Exception e) {
            return new ResponseEntity<>("Missing form title", HttpStatus.BAD_REQUEST);
        }

        final String formId;
        try {
            formId = formManagerService.save(submission, proposedFormDefinition, user);
        } catch (InsufficientPermissionsException e) {
            return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.UNAUTHORIZED);
        }
        if (formId != null) {
            return new ResponseEntity<>(formId, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("Could not create form", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Gets Custom Form JSON for a given entity", notes = "Gets Custom Form JSON for a given entity", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/element",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            method = RequestMethod.GET)
    public ResponseEntity<String> getCustomForm(final @RequestParam String xsiType,
                                                final @RequestParam(required = false) String id,
                                                final @RequestParam Boolean appendPrevNextButtons,
                                                final @RequestParam(required = false) String projectId,
                                                final @RequestParam(required = false) String visitId,
                                                final @RequestParam(required = false) String subtype
    ) {
        try {
            final UserI user = getSessionUser();
            final String customFormJson;
            try {
                customFormJson = formManagerService.getCustomForm(user, xsiType, id, projectId, visitId, subtype, appendPrevNextButtons);
            } catch (NotFoundException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
            if (null == customFormJson) {
                return new ResponseEntity<>("Custom Forms Not Found", HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(customFormJson, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Possibly Custom Form Fetcher Class had issues ", e);
            return new ResponseEntity<>("Could not fetch custom forms: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Marks a form as enabled", notes = "Marks a form as enabled", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/enable", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, restrictTo = Role)
    @AuthorizedRoles({CustomFormsConstants.ADMIN_ROLE, CustomFormsConstants.FORM_MANAGER_ROLE})
    public ResponseEntity<String> enableCustomForm(final @RequestBody String jsonbody) {
        final UserI user = getSessionUser();
        try {
            List<FormAppliesToPoJo> formAppliesToPoJos = objectMapper.readValue(jsonbody, new TypeReference<List<FormAppliesToPoJo>>() {});
            boolean success = true;
            for (FormAppliesToPoJo formByStatusPoJo : formAppliesToPoJos) {
                success = success && formManagerService.enableForm(user, formByStatusPoJo.getIdCustomVariableFormAppliesTo());
            }
            if (success) {
                return new ResponseEntity<>("Form Enabled", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Form not enabled", HttpStatus.BAD_REQUEST);
            }
        } catch (InsufficientPermissionsException ie) {
            return new ResponseEntity<>("Not enough permissions to enable form", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            log.error("Could not enable form ", e);
            return new ResponseEntity<>("Custom Form could not be enabled: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Add projects to an existing form", notes = "Add projects to an existing form", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/optin/{rowId}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> optInCustomForm(final @PathVariable String rowId, final @RequestBody String jsonbody) {
        final UserI user = getSessionUser();
        try {
            List<String> projects = objectMapper.readValue(jsonbody, new TypeReference<List<String>>() {});
            if (projects.isEmpty() || projects.stream().anyMatch(StringUtils::isBlank)) {
                return new ResponseEntity<>("Projects not passed", HttpStatus.BAD_REQUEST);
            }
            RowIdentifier rowIdentifier = RowIdentifier.Unmarshall(rowId);
            boolean success = formManagerService.optProjectsIntoForm(user, rowIdentifier, projects);
            if (success) {
                return new ResponseEntity<>("Projects opted into form", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Projects could not be opted into form", HttpStatus.BAD_REQUEST);
            }
        } catch (NotFoundException ie) {
            return new ResponseEntity<>("Not found", HttpStatus.NOT_FOUND);
        } catch (InsufficientPermissionsException ie) {
            return new ResponseEntity<>("Not enough permissions to opt into form", HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException | NullPointerException | JsonProcessingException e) {
            log.error("Could not opt  project into form ", e);
            return new ResponseEntity<>("Could not opt projects into form: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception jpe) {
            log.error("Could not parse json", jpe);
            return new ResponseEntity<>("Could not opt projects into form: " + jpe.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Opt out of a form", notes = "Opt out of a form", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/optout/{formId}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> optOutCustomForm(final @PathVariable String formId, final @RequestBody String jsonbody) {
        final UserI user = getSessionUser();
        List<String> projectIds = new ArrayList<>();
        try {
            projectIds = objectMapper.readValue(jsonbody, new TypeReference<List<String>>() {});
            if (projectIds.isEmpty() || projectIds.stream().anyMatch(StringUtils::isBlank)) {
                return new ResponseEntity<>("Projects not passed", HttpStatus.BAD_REQUEST);
            }
            boolean success = formManagerService.optOutOfForm(user, formId, projectIds);
            if (success) {
                return new ResponseEntity<>("Projects  have opted out", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Failed to opt out", HttpStatus.BAD_REQUEST);
            }
        } catch (NotFoundException ie) {
            return new ResponseEntity<>("Data not found", HttpStatus.NOT_FOUND);
        } catch (InsufficientPermissionsException ie) {
            return new ResponseEntity<>("Not enough permissions to opt out of form", HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException | NullPointerException | JsonProcessingException ia) {
            return new ResponseEntity<>("Invalid data", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Could not opt out of form ", e);
            return new ResponseEntity<>("Custom Form could not be opted out of: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Set display order of a form", notes = "The order must be within the range [-11000000, 1000000]", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/formId/{formId}", method = RequestMethod.POST, restrictTo = Role)
    @AuthorizedRoles({CustomFormsConstants.ADMIN_ROLE, CustomFormsConstants.FORM_MANAGER_ROLE})
    public ResponseEntity<String> modifyFormDisplayOrder(final @PathVariable String formId, final @RequestParam Integer displayOrder) {
        if (displayOrder == null || displayOrder < -1000000 || displayOrder > 1000000) {
            return new ResponseEntity<>("The display order you have entered is empty or outside the allowed range.", HttpStatus.BAD_REQUEST);
        }
        try {
            UUID.fromString(formId);
        } catch (IllegalArgumentException iae) {
            return new ResponseEntity<>("Invalid Form Id", HttpStatus.BAD_REQUEST);
        }
        final UserI user = getSessionUser();
        try {
            boolean success = formManagerService.modifyDisplayOrder(user, displayOrder, formId);
            if (success) {
                return new ResponseEntity<>("Display order updated to " + displayOrder, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Failed to modify display order", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Could not modify display order of form {}", formId, e);
            if (e instanceof  InsufficientPermissionsException) {
                return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>("Display order of Custom Form could not be modified: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @ApiOperation(value = "Promote a Project specific form to Site Repository", notes = "Promote a form to site repository", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/promote", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, restrictTo = Role)
    @AuthorizedRoles({CustomFormsConstants.ADMIN_ROLE, CustomFormsConstants.FORM_MANAGER_ROLE})
    public ResponseEntity<String> promoteform(final @RequestBody String jsonbody) {
        try {
            final UserI user = getSessionUser();
            List<FormAppliesToPoJo> formAppliesToPoJos = objectMapper.readValue(jsonbody, new TypeReference<List<FormAppliesToPoJo>>() {});
            boolean success = formManagerService.promoteForm(user, formAppliesToPoJos);
            if (success) {
                return new ResponseEntity<>("Form promoted to site repository", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Failed to promote form to site repository. Possibly same form id not provided.", HttpStatus.BAD_REQUEST);
            }
        } catch (NotFoundException ie) {
            return new ResponseEntity<>("Form  not found", HttpStatus.NOT_FOUND);
        } catch (CustomVariableNameClashException ce) {
            log.error("Name clash detected ", ce);
            return new ResponseEntity<>("Could not promote form as there exist other forms at the same level with identical property name(s): " + ce.getClashes(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Possibly failed to parse json " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @ApiOperation(value = "Marks a form as disabled", notes = "Marks a form as disabled", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/disable", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, restrictTo = Role)
    @AuthorizedRoles({CustomFormsConstants.ADMIN_ROLE, CustomFormsConstants.FORM_MANAGER_ROLE})
    public ResponseEntity<String> disableCustomForm(final @RequestBody String jsonbody) {
        final UserI user = getSessionUser();
        try {
            List<FormAppliesToPoJo> formAppliesToPoJos = objectMapper.readValue(jsonbody, new TypeReference<List<FormAppliesToPoJo>>() {});
            boolean success = true;
            for (FormAppliesToPoJo formByStatusPoJo : formAppliesToPoJos) {
                success = success && formManagerService.disableForm(user, formByStatusPoJo.getIdCustomVariableFormAppliesTo());
            }
            if (success) {
                return new ResponseEntity<>("Form disabled", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Custom Form could not be disabled", HttpStatus.BAD_REQUEST);
            }
        } catch (InsufficientPermissionsException ie) {
            return new ResponseEntity<>("Not enough permissions to disable form", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            log.error("Could not disable form", e);
            return new ResponseEntity<>("Custom Form could not be disabled: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Deletes a form", notes = "Deletes a form is not backed up by data", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(method = RequestMethod.DELETE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, restrictTo = Role)
    @AuthorizedRoles({CustomFormsConstants.ADMIN_ROLE, CustomFormsConstants.FORM_MANAGER_ROLE})
    public ResponseEntity<String> deleteCustomForm(final @RequestBody String jsonbody) {
        final UserI user = getSessionUser();
        try {
            List<FormAppliesToPoJo> formAppliesToPoJos = objectMapper.readValue(jsonbody, new TypeReference<List<FormAppliesToPoJo>>() {});
            List<String> deleteStatuses = new ArrayList<>();
            for (FormAppliesToPoJo formAppliesToPoJo : formAppliesToPoJos) {
                String status = formManagerService.deleteForm(user, formAppliesToPoJo.getIdCustomVariableFormAppliesTo());
                if (null != status) {
                    deleteStatuses.add(formAppliesToPoJo.getEntityId() == null ? "Site Wide: " + status : formAppliesToPoJo.getEntityId() + ": " + status);
                }
            }
            if (deleteStatuses.size() > 0) {
                return new ResponseEntity<>("Form " + String.join(",", deleteStatuses), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Custom Form could not be deleted", HttpStatus.BAD_REQUEST);
            }
        } catch (InsufficientPermissionsException ie) {
            return new ResponseEntity<>("Not enough permissions to disable form", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            log.error("Could not delete form", e);
            return new ResponseEntity<>("Custom Form could not be deleted: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //TODO: Are the configurations open to all?
    @ApiOperation(value = "Gets list of all site wide and projects which have custom forms", notes = "Gets Site wide and project Custom Form configurations", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public ResponseEntity<List<PseudoConfiguration>> getAllCustomFormConfigurations(
            final @RequestParam(required = false) String projectId) {
        try {
            List<PseudoConfiguration> configurations = formManagerService.getAllCustomForms(projectId);
            if (null == configurations) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(configurations, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Could not fetch forms ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Get the XNAT Deployment environment related to FormsIO", notes = "Get the XNAT Deployment environment related to FormsIO", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/env", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public ResponseEntity<XnatFormsIOEnv> getXnatEnvironmentForFormsIO(
            final @RequestParam(required = false) String projectId) {
        try {
            final UserI user = getSessionUser();
            if (!permissionsService.isUserAdminOrDataManager(user) && !permissionsService.isUserProjectOwner(user, projectId)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            return new ResponseEntity<>(formManagerService.getFormsEnvironment(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Checks to see if any data has been created and associated with a given custom form.", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/hasdata/{rowId}", method = RequestMethod.GET)
    public boolean checkForCustomFormHasData(final @PathVariable String rowId) throws DataFormatException {
        try {
            RowIdentifier rowIdentifier = RowIdentifier.Unmarshall(rowId);
            return formManagerService.checkCustomFormForData(rowIdentifier);
        } catch (Exception e) {
            throw new DataFormatException("Invalid row ID " + rowId);
        }
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(JsonProcessingException.class)
    public String handleJackson(final JsonProcessingException e) {
        log.info("Invalid JSON", e);
        return "Invalid input JSON. " + e.getLocalizedMessage();
    }
}