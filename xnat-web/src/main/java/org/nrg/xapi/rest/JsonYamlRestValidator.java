/*
 * web: org.nrg.xapi.rest.JsonYamlRestValidator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(description = "JSON / YAML REST Validator")
@RestController
@RequestMapping(value = "/validate")
public class JsonYamlRestValidator {
    private static final Logger _log = LoggerFactory.getLogger(JsonYamlRestValidator.class);

    @ApiOperation(value = "Validates the JSON string passed in as an escaped query variable.", notes = "Query string variable is json", response = String.class, responseContainer = "String")
    @ApiResponses({@ApiResponse(code = 200, message = "Reports \"Success\" if valid or the parsing error message if not."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, method = RequestMethod.GET)
    public ResponseEntity<String> validateQueryJson(@ApiParam(value="the JSON string to validate", required=true) @RequestParam(value="json") String json) {
        return validate(json);
    }

    @ApiOperation(value = "Validates the posted JSON string.", response = String.class, responseContainer = "String")
    @ApiResponses({@ApiResponse(code = 200, message = "Reports \"Success\" if valid or the parsing error message if not."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, method = RequestMethod.POST)
    public ResponseEntity<String> validatePostedJson(@ApiParam(value="the JSON string to validate", required=true) @RequestParam(value="json") String json) {
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
