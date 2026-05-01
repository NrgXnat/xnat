/*
 * spawner: org.nrg.xnat.spawner.rest.SpawnerApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.exception.ConstraintViolationException;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.services.SerializerService;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NoContentException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.spawner.entities.SpawnerElement;
import org.nrg.xnat.spawner.exceptions.CircularReferenceException;
import org.nrg.xnat.spawner.exceptions.InvalidElementIdException;
import org.nrg.xnat.spawner.exceptions.InvalidValidationValueException;
import org.nrg.xnat.spawner.exceptions.InvalidYamlElementException;
import org.nrg.xnat.spawner.exceptions.UnknownValidationSetException;
import org.nrg.xnat.spawner.services.SpawnerService;
import org.nrg.xnat.spawner.validation.SpawnerValidationService;
import org.nrg.xnat.spawner.validation.Validated;
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
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;

@Api("XNAT Spawner API")
@XapiRestController
@RequestMapping(value = "/spawner")
@ResponseBody
@Slf4j
public class SpawnerApi extends AbstractXapiRestController {
    @Autowired
    public SpawnerApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final SpawnerService service, final SerializerService serializer, final SpawnerValidationService validationService) {
        super(userManagementService, roleHolder);
        _service = service;
        _serializer = serializer;
        _validationService = validationService;
    }

    @ApiOperation(value = "Re-initializes the system's spawner elements.")
    @ApiResponses({@ApiResponse(code = 200, message = "Spawner elements successfully re-initialized."),
                   @ApiResponse(code = 500, message = "An unexpected or unknown error occurred")})
    @XapiRequestMapping(value = "initialize", consumes = MediaType.APPLICATION_JSON_VALUE, restrictTo = AccessLevel.Admin, method = RequestMethod.POST)
    public void initialize(@ApiParam(value = "Whether the service should be purged regardless of the purgeAndRefreshOnStartup preference setting. If not specified, this defaults to true.") @RequestBody(required = false) final Boolean forcePurge) {
        _service.initialize(BooleanUtils.toBooleanDefaultIfNull(forcePurge, true));
    }

    @ApiOperation(value = "Get list of available element namespaces in the system.", notes = "The spawner element namespaces provide organizational separation of spawner elements. This call returns a list of the namespaces in the XNAT system.", response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of spawner element namespaces."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "namespaces", produces = "application/json", method = RequestMethod.GET)
    public List<String> getNamespaces() {
        final List<String> elementIds = _service.getNamespaces();
        log.debug("Found {} spawner element IDs.", elementIds.size());
        return elementIds;
    }

    @ApiOperation(value = "Get list of available element namespaces and their restrictions in the system for the requesting user.", notes = "The spawner element namespaces provide organizational separation of spawner elements. This call returns a list of the namespaces in the XNAT system.", response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A map of spawner element namespaces and their restrictions."),
            @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "namespaces/restricted", produces = "application/json", method = RequestMethod.GET)
    public List<String> getNamespacesWithRestrictions() {
        final List<Object[]> elementIds = _service.getNamespacesAndResrictions();
        log.debug("Found {} spawner element IDs.", elementIds.size());
        return getAuthorizedNamespaces(getSessionUser(), elementIds);
    }


    @ApiOperation(value = "Get list of element IDs in the system's default namespace.", notes = "The spawner element IDs function returns a list of all the IDs of spawner elements in the default namespace of the XNAT system.", response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of spawner element IDs in the default namespace."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "ids", produces = "application/json", method = RequestMethod.GET)
    public List<String> getDefaultElementIds() {
        return getNamespacedElementIds(SpawnerElement.DEFAULT_NAMESPACE);
    }

    @ApiOperation(value = "Get list of element IDs in the specified namespace.", notes = "The spawner element IDs function returns a list of all the IDs of spawner elements in the specified namespace of the XNAT system.", response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of spawner element IDs for the specified namespace."),
                   @ApiResponse(code = 204, message = "Namespace not found or no elements exist."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "ids/{namespace}", produces = "application/json", method = RequestMethod.GET)
    public List<String> getNamespacedElementIds(@PathVariable final String namespace) {
        final List<String> elementIds = _service.getNamespacedElementIds(namespace);
        log.debug("Found {} spawner element IDs in the {}.", elementIds.size(), getFormattedNamespace(namespace));
        return elementIds;
    }

    @ApiOperation(value = "Get list of spawner elements in the system's default namespace.", notes = "The default namespace spawner element function returns a list of all of the spawner elements in the default namespace of the XNAT system.", response = SpawnerElement.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of spawner elements from the default namespace."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "elements", produces = "application/json", method = RequestMethod.GET)
    public List<SpawnerElement> getDefaultElements() throws NoContentException {
        return getNamespacedElements(SpawnerElement.DEFAULT_NAMESPACE);
    }

    @ApiOperation(value = "Get list of spawner elements in the specified namespace.", notes = "The namespaced spawner element function returns a list of all of the spawner elements in the specified namespace of the XNAT system.", response = SpawnerElement.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "A list of spawner elements from the specified namespace."),
                   @ApiResponse(code = 204, message = "Namespace not found or no elements exist."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "elements/{namespace}", produces = "application/json", method = RequestMethod.GET)
    public List<SpawnerElement> getNamespacedElements(@PathVariable("namespace") final String namespace) throws NoContentException {
        final List<SpawnerElement> elements = _service.getNamespacedElements(namespace);
        if (CollectionUtils.isEmpty(elements)) {
            throw new NoContentException("No elements found for namespace " + namespace);
        }
        log.debug("Found {} spawner element IDs in the {}.", elements.size(), getFormattedNamespace(namespace));
        return elements;
    }

    @ApiOperation(value = "Refreshes the spawner elements in the specified namespace from the corresponding element definition file.", notes = "The refresh spawner namespace updates all of the spawner elements in the specified element definition file of the XNAT system. This will not delete or update any elements that are not specified in the element definition file")
    @ApiResponses({@ApiResponse(code = 200, message = "The spawner elements in the specified namespace were successfully refreshed."),
                   @ApiResponse(code = 404, message = "Namespace not found"),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "elements/{namespace}", produces = "application/json", method = RequestMethod.PUT, restrictTo = Admin)
    public void refreshNamespacedElements(@PathVariable("namespace") final String namespace) throws InvalidElementIdException {
        _service.refreshNamespace(namespace);
        log.info("User {} deleted all of the existing spawner elements in the namespace {}.", getSessionUser().getLogin(), getFormattedNamespace(namespace));
    }

    @ApiOperation(value = "Purges the existing spawner elements in the specified namespace and refreshes the elements from the corresponding element definition file.", notes = "The refresh spawner namespace updates all of the spawner elements in the specified element definition file of the XNAT system. Any elements that are not specified in the element definition file will be purged from the system.")
    @ApiResponses({@ApiResponse(code = 200, message = "The spawner elements in the specified namespace were successfully purged and refreshed."),
                   @ApiResponse(code = 404, message = "Namespace not found"),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "elements/{namespace}", produces = "application/json", method = RequestMethod.POST, restrictTo = Admin)
    public void purgeAndRefreshNamespacedElements(@PathVariable("namespace") final String namespace) throws InvalidElementIdException {
        _service.purgeAndRefreshNamespace(namespace);
        log.info("User {} deleted all of the existing spawner elements in the namespace {}.", getSessionUser().getLogin(), getFormattedNamespace(namespace));
    }

    @ApiOperation(value = "Deletes all spawner elements in the specified namespace.", notes = "The delete spawner namespace deletes all of the spawner elements in the specified namespace of the XNAT system.")
    @ApiResponses({@ApiResponse(code = 200, message = "The spawner elements in the specified namespace were successfully deleted."),
                   @ApiResponse(code = 404, message = "Namespace not found"),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "elements/{namespace}", produces = "application/json", method = RequestMethod.DELETE, restrictTo = Admin)
    public void deleteNamespacedElements(@PathVariable("namespace") final String namespace) {
        _service.deleteNamespace(namespace);
        log.info("User {} deleted all of the existing spawner elements in the namespace {}.", getSessionUser().getLogin(), getFormattedNamespace(namespace));
    }

    @ApiOperation(value = "Get the specified spawner element from the default namespace.", notes = "Retrieves the spawner element with the indicated ID from the default namespace.", response = SpawnerElement.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The spawner element with the specified ID from the default namespace."),
                   @ApiResponse(code = 204, message = "Element ID doesn't exist or has no content."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "element/{elementId}", produces = "application/json", method = RequestMethod.GET)
    public SpawnerElement getDefaultElement(@PathVariable("elementId") final String elementId) throws NoContentException {
        return getNamespacedElement(SpawnerElement.DEFAULT_NAMESPACE, elementId);
    }

    @ApiOperation(value = "Get the specified spawner element from the specified namespace.", notes = "Retrieves the spawner element with the indicated ID from the specified namespace.", response = SpawnerElement.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The spawner element with the specified ID from the default namespace."),
                   @ApiResponse(code = 204, message = "Namespace or element ID doesn't exist or has no content."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "element/{namespace}/{elementId}", produces = "application/json", method = RequestMethod.GET)
    public SpawnerElement getNamespacedElement(@PathVariable("namespace") String namespace, @PathVariable("elementId") final String elementId) throws NoContentException {
        final SpawnerElement element = _service.retrieve(namespace, elementId);
        if (element == null) {
            throw new NoContentException("The namespace " + namespace + " has no element with the ID " + elementId);
        }
        log.debug("Found spawner element with ID {} in the namespace {}.", elementId, getFormattedNamespace(namespace));
        return element;
    }

    @ApiOperation(value = "Updates the specified spawner element.", notes = "Updates the spawner element in the default namespace with the submitted data. This method returns the parsed and validated YAML content of the spawner element on success.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The specified element was successfully updated."),
                   @ApiResponse(code = 403, message = "Access denied"),
                   @ApiResponse(code = 404, message = "Namespace or element ID not found"),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "element/{elementId}", produces = "text/x-yaml", method = RequestMethod.PUT, restrictTo = Admin)
    public String updateDefaultElement(@PathVariable("elementId") final String elementId, @RequestBody final String yaml) throws NotFoundException {
        return updateNamespacedElement(SpawnerElement.DEFAULT_NAMESPACE, elementId, yaml);
    }

    @ApiOperation(value = "Updates the specified spawner element.", notes = "Updates the spawner element in the specified namespace with the submitted data. This method returns the parsed and validated YAML content of the spawner element on success.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The specified element was successfully updated."),
                   @ApiResponse(code = 403, message = "Access denied"),
                   @ApiResponse(code = 404, message = "Namespace or element ID not found"),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "element/{namespace}/{elementId}", produces = "text/x-yaml", method = RequestMethod.PUT, restrictTo = Admin)
    public String updateNamespacedElement(@PathVariable("namespace") String namespace, @PathVariable("elementId") final String elementId, @RequestBody final String yaml) throws NotFoundException {
        final SpawnerElement element = _service.retrieve(namespace, elementId);
        if (element == null) {
            throw new NotFoundException(namespace, elementId);
        }
        log.debug("Found spawner element with ID {} in the namespace {}, updating.", elementId, getFormattedNamespace(namespace));
        element.setYaml(yaml);
        _service.update(element);
        return yaml;
    }

    @ApiOperation(value = "Deletes the specified spawner element.", notes = "Deletes the spawner element in the default namespace.")
    @ApiResponses({@ApiResponse(code = 200, message = "The specified element was successfully deleted."),
                   @ApiResponse(code = 403, message = "Access denied"),
                   @ApiResponse(code = 404, message = "Namespace or element ID not found"),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "element/{elementId}", produces = "text/x-yaml", method = RequestMethod.DELETE, restrictTo = Admin)
    public void deleteDefaultElement(@PathVariable("elementId") final String elementId) throws NotFoundException {
        deleteNamespacedElement(SpawnerElement.DEFAULT_NAMESPACE, elementId);
    }

    @ApiOperation(value = "Deletes the specified spawner element.", notes = "Deletes the spawner element in the specified namespace.")
    @ApiResponses({@ApiResponse(code = 200, message = "The specified element was successfully deleted."),
                   @ApiResponse(code = 403, message = "Access denied"),
                   @ApiResponse(code = 404, message = "Namespace or element ID not found"),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "element/{namespace}/{elementId}", produces = "text/x-yaml", method = RequestMethod.DELETE, restrictTo = Admin)
    public void deleteNamespacedElement(@PathVariable("namespace") String namespace, @PathVariable("elementId") final String elementId) throws NotFoundException {
        final SpawnerElement element = _service.retrieve(elementId);
        if (element == null) {
            throw new NotFoundException(namespace, elementId);
        }
        log.debug("Found spawner element with ID {} in the namespace {}, deleting.", elementId, getFormattedNamespace(namespace));
        _service.delete(element);
    }

    @ApiOperation(value = "Create a new spawner element in the default namespace.", notes = "Creates a new spawner element in the default namespace from the submitted YAML. This method returns the parsed and validated YAML content of the spawner element on success.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The new spawner element was successfully created."),
                   @ApiResponse(code = 400, message = "Bad request, likely an invalid, missing, or duplicated element ID."),
                   @ApiResponse(code = 403, message = "Access denied"),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "element", consumes = {"text/plain", "text/x-yaml"}, produces = "text/x-yaml", method = RequestMethod.POST, restrictTo = Admin)
    public String createDefaultElement(@RequestBody final String yaml) throws IOException, InvalidElementIdException, InvalidYamlElementException, ResourceAlreadyExistsException {
        return createNamespacedElement(SpawnerElement.DEFAULT_NAMESPACE, yaml);
    }

    @ApiOperation(value = "Create a new spawner element in the specified namespace.", notes = "Creates a new spawner element in the specified namespace from the submitted YAML. This method returns the parsed and validated YAML content of the spawner element on success.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The new spawner element was successfully created."),
                   @ApiResponse(code = 400, message = "Bad request, likely an invalid, missing, or duplicated element ID."),
                   @ApiResponse(code = 403, message = "Access denied"),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "element/{namespace}", consumes = {"text/plain", "text/x-yaml"}, produces = "text/x-yaml", method = RequestMethod.POST, restrictTo = Admin)
    public String createNamespacedElement(@PathVariable("namespace") String namespace, @RequestBody final String yaml) throws IOException, InvalidElementIdException, InvalidYamlElementException, ResourceAlreadyExistsException {
        final SpawnerElement element = _service.parse(yaml);
        if (StringUtils.isBlank(element.getElementId())) {
            throw new InvalidYamlElementException(yaml);
        }
        element.setNamespace(namespace);
        log.debug("Trying to create new spawner element with ID {} in the namespace {}.", element.getElementId(), getFormattedNamespace(namespace));
        final SpawnerElement created;
        try {
            created = _service.create(element);
        } catch (ConstraintViolationException cve) {
            throw new ResourceAlreadyExistsException(SpawnerElement.class.getSimpleName(), "Save failed because spawner element with id='" + element.getElementId() + "' already exists. Try updating with PUT instead.");
        }
        return created.getYaml();
    }

    @ApiOperation(value = "Resolves the specified spawner element from the default namespace.", notes = "This method retrieves the spawner element with the indicated ID from the default namespace, then resolves all of the referenced child elements as well, and renders a fully resolved YAML document that describes a complete interface.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The specified element was successfully located and resolved."),
                   @ApiResponse(code = 204, message = "Element ID doesn't exist or has no content."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "resolve/{elementId}", produces = "application/json", method = RequestMethod.GET)
    public JsonNode resolveDefaultElement(@PathVariable("elementId") final String elementId, final @RequestParam(required = false) boolean restrict) throws CircularReferenceException, IOException, NoContentException, InsufficientPrivilegesException {
        return resolveNamespacedElement(SpawnerElement.DEFAULT_NAMESPACE, elementId, restrict);
    }

    @ApiOperation(value = "Resolves the specified spawner element from the specified namespace.", notes = "This method retrieves the spawner element with the indicated ID from the specified namespace, then resolves all of the referenced child elements as well, and renders a fully resolved YAML document that describes a complete interface.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The specified element was successfully located and resolved."),
                   @ApiResponse(code = 204, message = "Namespace or element ID doesn't exist or has no content."),
                   @ApiResponse(code = 403, message = "Namespace or element ID is restricted to role."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "resolve/{namespace}/{elementId}", produces = "application/json", method = RequestMethod.GET)
    public JsonNode resolveNamespacedElement(@PathVariable("namespace") String namespace, @PathVariable("elementId") final String elementId, final @RequestParam(required = false) boolean restrict) throws CircularReferenceException, IOException, NoContentException, InsufficientPrivilegesException {
        final SpawnerElement element = _service.retrieve(namespace, elementId);
        if (element == null) {
            throw new NoContentException("The namespace " + namespace + " has no element with the ID " + elementId);
        }
        if (restrict) {
            if (!isUserAuthorized(getSessionUser(), element.getRestricted())) {
                throw new InsufficientPrivilegesException("User does not sufficient privileges");
            }
        }
        log.debug("Found spawner element with ID {}, returning.", elementId);
        return _serializer.deserializeYaml(_service.resolve(namespace, elementId));
    }

    @ApiOperation(value = "Resolves the 'root' element in the specified spawner namespace as XML.", notes = "This method retrieves elements from the specified spawner namespace and renders an XML representation of a fully resolved YAML document that describes a complete interface.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The specified namespace was successfully located and resolved."),
                   @ApiResponse(code = 204, message = "Namespace doesn't exist or has no content."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "xml/{namespace}", produces = "text/xml", method = RequestMethod.GET)
    public String resolveNamespaceXml(@PathVariable("namespace") String namespace) throws CircularReferenceException, IOException, NoContentException {
        // look for an element named 'root' as the main entry point
        final Pair<String, SpawnerElement> elementPair = getRootElementAndElement(namespace);
        log.debug("Found spawner namespace {}, returning.", elementPair.getValue());
        return createSpawnerXml(namespace, elementPair.getKey());
    }

    @ApiOperation(value = "Resolves the specified spawner element from the specified namespace as XML.", notes = "This method retrieves the spawner element with the indicated ID from the specified namespace, then resolves all of the referenced child elements as well, and renders an XML representation of a fully resolved YAML document that describes a complete interface.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The specified element was successfully located and resolved."),
                   @ApiResponse(code = 204, message = "Namespace or element ID doesn't exist or has no content."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "xml/{namespace}/{elementId}", produces = "text/xml", method = RequestMethod.GET)
    public String resolveNamespacedElementXml(@PathVariable("namespace") String namespace, @PathVariable("elementId") final String elementId) throws CircularReferenceException, IOException, NoContentException {
        final SpawnerElement element = _service.retrieve(namespace, elementId);
        if (element == null) {
            throw new NoContentException("The namespace " + namespace + " has no element with the ID " + elementId);
        }
        log.debug("Found spawner element with ID {}, returning.", elementId);
        return createSpawnerXml(namespace, elementId);
    }

    @ApiOperation(value = "Returns all available validation methods.", notes = "This call returns a map keyed by the validation set and ID.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "Value was successfully validated."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 404, message = "The specified validation method was not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "validate/list", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public Map<String, List<String>> validateList() {
        log.debug("User {} is requesting the validation list", getSessionUser().getUsername());
        return _validationService.getValidations();
    }

    @ApiOperation(value = "Returns all available validation methods.", notes = "This call returns a map keyed by the validation set and ID.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "Value was successfully validated."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 404, message = "The specified validation method was not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "validate/list/{set}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public List<String> validateListSet(@ApiParam(value = "The set of validations to retrieve", required = true) @PathVariable("set") final String set) {
        log.debug("User {} is requesting the validation list", getSessionUser().getUsername());
        return new ArrayList<>(_validationService.getValidationSet(set).keySet());
    }

    @ApiOperation(value = "Validates the value in the request body with the indicated validation method.", notes = "Applies the given validation method to the value in the request body.", response = Validated.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Value was successfully validated."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 404, message = "The specified validation method was not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "validate/{validation}", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public Validated validateValidation(@ApiParam(value = "The validation method to be applied to the input value.", required = true) @PathVariable("validation") final String validation, @RequestBody final String value) throws UnknownValidationSetException {
        log.debug("User {} is using the validation {} to validate the value: {}", getSessionUser().getUsername(), validation, value);
        return _validationService.validate(validation, value);
    }

    @ApiOperation(value = "Validates the value in the request body with the indicated validation method.", notes = "Applies the given validation method to the value in the request body.", response = Validated.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Value was successfully validated."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 404, message = "The specified validation method was not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "validate/{set}/{validation}", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public Validated validateSetValidation(@ApiParam(value = "The validation set to reference.", required = true) @PathVariable("set") final String set, @ApiParam(value = "The validation method to be applied to the input value.", required = true) @PathVariable("validation") final String validation, @RequestBody final String value) throws UnknownValidationSetException {
        log.debug("User {} is using the validation {} to validate the value: {}", getSessionUser().getUsername(), validation, value);
        return _validationService.validate(set, validation, value);
    }

    @ApiOperation(value = "Validates the value in the request body with the indicated validation method.", notes = "Applies the given validation method to the value in the request body.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "Value was successfully validated."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 404, message = "The specified validation method was not found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "validate/batch", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public Map<String, Validated> validateBatch(@ApiParam("This is a map of element IDs, with each element ID corresponding to a map containing a value property, validation property, and optional set property. Both set and validation can specified with the validation property by using the form \"validation\": \"set:validation\".") @RequestBody final Map<String, Map<String, String>> elements) throws UnknownValidationSetException, InvalidValidationValueException {
        log.debug("User {} is using the batch validation method to validate {} elements from IDs: {}", getSessionUser().getUsername(), elements.size(), elements.keySet());
        final Map<String, Validated> validations = new HashMap<>();
        for (final String id : elements.keySet()) {
            final Map<String, String> values = elements.get(id);
            final String              value  = values.get("value");
            if (StringUtils.isBlank(value)) {
                throw new InvalidValidationValueException(id);
            }
            final String validation;
            final String set;
            if (values.get("validation").contains(":")) {
                final String[] atoms = values.get("validation").split(":", 2);
                set = atoms[0];
                validation = atoms[1];
            } else {
                set = values.getOrDefault("set", SpawnerValidationService.STANDARD_SET);
                validation = values.get("validation");
            }
            validations.put(id, _validationService.validate(set, validation, value));
        }

        return validations;
    }

    @ExceptionHandler(InvalidYamlElementException.class)
    public ResponseEntity<String> handleInvalidYamlElementException(final InvalidYamlElementException exception) {
        return new ResponseEntity<>("An invalid YAML element was submitted. The invalid YAML was: " + exception.getInvalidElement(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidElementIdException.class)
    public ResponseEntity<String> handleInvalidElementIdException(final InvalidElementIdException exception) {
        return new ResponseEntity<>("An invalid element ID was requested. The invalid ID was: " + exception.getNamespace() + "/" + exception.getInvalidElementId(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CircularReferenceException.class)
    public ResponseEntity<String> handleCircularReferenceException(final CircularReferenceException exception) {
        return new ResponseEntity<>("A circular reference was found in the requested element: " + exception.getElementId(), HttpStatus.BAD_REQUEST);
    }

    // sorry this helper method is cluttering up the API class
    private String createSpawnerXml(final String namespace, final String elementId) throws InvalidElementIdException, CircularReferenceException, IOException {
        // ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final String        resolvedYaml = _service.resolve(namespace, elementId);
        final JsonNode      yamlJSON     = _serializer.deserializeYaml(resolvedYaml);
        final XmlMapper     xmlMapper    = new XmlMapper();
        final StringBuilder resolvedXml  = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        resolvedXml.append("<spawner>\n");
        // leave a spot for more child elements of the root <spawner> element
        // maybe...
        //<spawner>
        //    <access>
        //        <allow>
        //            <!-- allow roles -->
        //            <role>Administrator</role>
        //            <role>DataManager</role>
        //            <!-- allow groups -->
        //            <group>Bogus_Project_owners</group>
        //            <group>Legit_Project_owners</group>
        //        </allow>
        //        <deny>
        //            <!-- deny roles -->
        //            <role>Anonymous</role>
        //            <!-- deny users -->
        //            <user>guest</user>
        //            <user>bob</user>
        //            <user>fred</user>
        //            <user>george</user>
        //        </deny>
        //    </access>
        //    <elements>
        //        <!-- spawner elements -->
        //    </elements>
        //</spawner>
        final String elementsXml = xmlMapper.writer().withRootName("elements").writeValueAsString(yamlJSON);
        resolvedXml.append(elementsXml).append("\n");
        resolvedXml.append("</spawner>");
        return resolvedXml.toString();
    }

    private Pair<String, SpawnerElement> getRootElementAndElement(final String namespace) throws NoContentException {
        final SpawnerElement element = _service.retrieve(namespace, DEFAULT_ROOT_ELEMENT);
        if (element != null) {
            return ImmutablePair.of(DEFAULT_ROOT_ELEMENT, element);
        }
        // if there's not a 'root' element found, look for an element that matches the file name.
        final String         rootElement = StringUtils.substringAfterLast(namespace, ":");
        final SpawnerElement secondary   = _service.retrieve(namespace, rootElement);
        // give up if an entry element is not found
        if (secondary == null) {
            throw new NoContentException("No element found for namespace " + namespace);
        }
        return ImmutablePair.of(rootElement, secondary);
    }

    private String getFormattedNamespace(final String namespace) {
        return namespace.equals(SpawnerElement.DEFAULT_NAMESPACE) ? "default namespace" : namespace + " namespace";
    }


    private boolean isUserAuthorized(final UserI requestingUser, final String elementRestrictions) {
        return isAuthorized(requestingUser, Roles.getRoles(requestingUser), elementRestrictions);
    }

    private List<String> getAuthorizedNamespaces(final UserI requestingUser, final List<Object[]> namespaces) {
        List<String> authorizedNamespaces = new ArrayList<>();
        Map<String, String> namespaceWithRestrictions = new HashMap<>();
        if (!namespaces.isEmpty()) {
            Collection<String> userRoles = Roles.getRoles(requestingUser);
            for (Object[] row: namespaces) {
                String namespace = (String)row[0];
                String elementRestrictions = (String)row[1];
                if (elementRestrictions == null) {
                    if (Roles.isSiteAdmin(requestingUser)) {
                        authorizedNamespaces.add(namespace);
                    }
                }else {
                    namespaceWithRestrictions.put(namespace, elementRestrictions);
                    if (isAuthorized(requestingUser, userRoles, elementRestrictions)) {
                        authorizedNamespaces.add(namespace);
                    }
                }
            }
            for (String ns: namespaceWithRestrictions.keySet()) {
                final String elementRestrictions = namespaceWithRestrictions.get(ns);
                //This is required as only the parent element would have meta.restricted all children dont specify
                // See container-service siteSettings.yaml
                if (!isAuthorized(requestingUser, userRoles, elementRestrictions)) {
                    authorizedNamespaces.removeIf(n -> n.equals(ns));
                }
            }
        }
        return authorizedNamespaces;
    }

    private boolean isAuthorized(final UserI requestingUser, final Collection<String> roles, final String elementRestrictions) {
        boolean isAuthorized = false;
        if (null != elementRestrictions) {
            Set<String> userRoles = new HashSet<String>(roles);
            List<String> matchingItems = Arrays.stream(StringUtils.stripAll(elementRestrictions.split(",")))
                    .filter(userRoles::contains)
                    .collect(Collectors.toList());
            isAuthorized = matchingItems.size() > 0;
        }
        if (!isAuthorized && Roles.isSiteAdmin(requestingUser)) {
            isAuthorized = true;
        }
        return isAuthorized;
    }


    private static final String DEFAULT_ROOT_ELEMENT = "root";

    private final SpawnerService           _service;
    private final SerializerService        _serializer;
    private final SpawnerValidationService _validationService;
}
