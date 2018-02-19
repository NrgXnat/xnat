/*
 * web: org.nrg.xapi.rest.settings.XnatPluginApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.settings;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.services.logging.LoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;

@Api(description = "XNAT Logging API")
@XapiRestController
@RequestMapping(value = "/logs")
@Slf4j
public class LoggingApi extends AbstractXapiRestController {
    @Autowired
    public LoggingApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final LoggingService logging) {
        super(userManagementService, roleHolder);
        _logging = logging;
    }

    @ApiOperation(value = "Resets and reloads logging configuration from all log4j configuration files located either in XNAT itself or in plugins.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT logging configuration successfully reset."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "reset", produces = MediaType.TEXT_PLAIN_VALUE, method= RequestMethod.POST, restrictTo = Admin)
    public ResponseEntity<String> resetLoggingConfiguration() {
        final Properties properties = _logging.reset();
        try (final StringWriter writer = new StringWriter()) {
            properties.store(writer, "Generated properties for XNAT log4j configuration");
            return new ResponseEntity<>(writer.getBuffer().toString(), HttpStatus.OK);
        } catch (IOException e) {
            log.warn("An error occurred trying to write the log4j properties", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private final LoggingService _logging;
}
