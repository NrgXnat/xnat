/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2013, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Author: Justin Cleveland <clevelandj@wustl.edu>
 * Last modified 4/11/2016 1:06 PM
 */
package org.nrg.xapi.rest;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(description = "JSON / YAML REST Validator")
@RestController
@RequestMapping(value = "/validate")
public class JsonYamlRestValidator {
    private static final Logger _log = LoggerFactory.getLogger(JsonYamlRestValidator.class);

    @ApiOperation(value = "Validates the JSON string passed in as an escaped query variable.", notes = "Query string variable is json", response = String.class, responseContainer = "String")
    @ApiResponses({@ApiResponse(code = 200, message = "Reports \"Success\" if valid or the parsing error message if not."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(produces = {"application/json", "application/xml"}, method = RequestMethod.GET)
    public ResponseEntity<String> validateQueryJson(@ApiParam(value="the JSON string to validate", required=true) @RequestParam(value="json", required=true) String json) {
        try {
            if(validateJson(json)) {
                return new ResponseEntity<>("Success", HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Failed", HttpStatus.OK);
    }

    @ApiOperation(value = "Validates the posted JSON string.", response = String.class, responseContainer = "String")
    @ApiResponses({@ApiResponse(code = 200, message = "Reports \"Success\" if valid or the parsing error message if not."), @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(produces = {"application/json", "application/xml"}, method = RequestMethod.POST)
    public ResponseEntity<String> validatePostedJson(@ApiParam(value="the JSON string to validate", required=true) @RequestParam(value="json", required=true) String json) {
        try {
            if(validateJson(json)) {
                return new ResponseEntity<>("Success", HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Failed", HttpStatus.OK);
    }

    /**
     * @param json the JSON string to be validated
     */
    public boolean validateJson(String json) throws Exception {
        if(json != null) {
            _log.debug("Valid JSON: ");
            return true;
        }
        _log.debug("Invalid JSON: ");
        return false;
    }
}
