/*
 * web: org.nrg.xapi.rest.JsonYamlRestValidator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "json-yaml-rest-validator", description = "JSON / YAML REST Validator")
@RestController
@RequestMapping(value = "/validate")
public class JsonYamlRestValidator {
    private static final Logger _log = LoggerFactory.getLogger(JsonYamlRestValidator.class);

    @Operation(summary = "Validates the JSON string passed in as an escaped query variable.", description = "Query string variable is json")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Reports \"Success\" if valid or the parsing error message if not."), @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, method = RequestMethod.GET)
    public ResponseEntity<String> validateQueryJson(@Parameter(description = "the JSON string to validate", required = true) @RequestParam(value="json") String json) {
        return validate(json);
    }

    @Operation(summary = "Validates the posted JSON string.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Reports \"Success\" if valid or the parsing error message if not."), @ApiResponse(responseCode = "500", description = "Unexpected error")})
    @XapiRequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, method = RequestMethod.POST)
    public ResponseEntity<String> validatePostedJson(@Parameter(description = "the JSON string to validate", required = true) @RequestParam(value="json") String json) {
        return validate(json);
    }

    private ResponseEntity<String> validate(final String json) {
        try {
            return validateJson(json) ? new ResponseEntity<>("Success", HttpStatus.OK) : new ResponseEntity<>("Failed", HttpStatus.OK);
        } catch (Exception e) {
            _log.error("Error occurred validating JSON", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @param json the JSON string to be validated
     */
    private boolean validateJson(String json) throws Exception {
        if(json != null) {
            _log.debug("Valid JSON: ");
            return true;
        }
        _log.debug("Invalid JSON: ");
        return false;
    }
}
