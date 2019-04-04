/*
 * web: org.nrg.xapi.rest.settings.LoggingApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2019, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest.settings;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.services.archive.FileVisitorPathResourceMap;
import org.nrg.xnat.services.archive.PathResourceMap;
import org.nrg.xnat.services.logging.LoggingService;
import org.nrg.xnat.web.http.AbstractZipStreamingResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.nrg.xnat.web.http.AbstractZipStreamingResponseBody.MEDIA_TYPE;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Api("XNAT Logging API")
@XapiRestController
@RequestMapping(value = "/logs")
@Slf4j
public class LoggingApi extends AbstractXapiRestController {
    @Autowired
    public LoggingApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final LoggingService logging, final Path xnatHome) {
        super(userManagementService, roleHolder);
        _logging = logging;
        _xnatHome = xnatHome;
    }

    @ApiOperation(value = "Resets and reloads logging configuration from all logging configuration files located either in XNAT itself or in plugins.", responseContainer = "List", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT logging configurations successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "reset", produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Admin)
    public List<String> resetLoggingConfiguration() {
        return _logging.reset();
    }

    @ApiOperation(value = "Gets a list of all logging configuration files located either in XNAT itself or in plugins.", responseContainer = "Map", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT logging configurations successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "configs", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public Map<String, String> getLoggingConfigurations() {
        return _logging.getConfigurationResources();
    }

    @ApiOperation(value = "Gets the requested logging configuration file located either in XNAT itself or in plugins.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT logging configuration successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "configs/{resourceId}", produces = APPLICATION_XML_VALUE, method = GET, restrictTo = Admin)
    public String getLoggingConfiguration(@PathVariable final String resourceId) throws NotFoundException, IOException {
        final String configuration = _logging.getConfigurationResource(resourceId);
        if (StringUtils.isBlank(configuration)) {
            throw new NotFoundException("Couldn't find a logging configuration matching resource ID \"" + resourceId + "\"");
        }
        return configuration;
    }

    @ApiOperation(value = "Gets a list of the logger and appender elements defined in the primary logging configuration file in XNAT itself.", responseContainer = "Map", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT logging configuration successfully reset."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "elements", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public Map<String, List<String>> getPrimaryElements() {
        return _logging.getPrimaryElements();
    }

    @ApiOperation(value = "Downloads the XNAT log files as a zip archive.", response = StreamingResponseBody.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT logs successfully downloaded."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "The user is not authorized to access one or more of the specified resources."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "download", produces = MEDIA_TYPE, method = GET, restrictTo = Admin)
    public ResponseEntity<StreamingResponseBody> downloadLogFiles() throws IOException {
        return downloadLogFiles(Collections.<String, String>emptyMap());
    }

    @ApiOperation(value = "Downloads the XNAT log files as a zip archive.", response = StreamingResponseBody.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT logs successfully downloaded."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "The user is not authorized to access one or more of the specified resources."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "download/{logFileSpec}", produces = MEDIA_TYPE, method = GET, restrictTo = Admin)
    public ResponseEntity<StreamingResponseBody> downloadLogFiles(@PathVariable final String logFileSpec) throws IOException {
        return downloadLogFiles(ImmutableMap.of("logFileSpec", logFileSpec));
    }

    @ApiOperation(value = "Downloads the XNAT log files as a zip archive.",
                  notes = "This call takes a string map as JSON. PUT and POST are the same operation. Acceptable values in the map include: \"logFileSpec\" is a glob-style  wild card, e.g. '*.log', " +
                          "'application.*', etc. This defaults to '*'. \"path\" specifies the path to the folder containing the log files you want to access. The default value is the logs folder in " +
                          "your XNAT home directory, but you can specify other paths to which the XNAT application server user has access, e.g. \"/var/log/tomcat7\" to retrieve the Tomcat logs. " +
                          "Finally \"includeEmptyFiles\" indicates whether empty files should be included. By default only files that contain data are included.",
                  response = StreamingResponseBody.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT logs successfully downloaded."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "The user is not authorized to access one or more of the specified resources."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "download", restrictTo = Admin, method = {POST, PUT}, consumes = APPLICATION_JSON_VALUE, produces = MEDIA_TYPE)
    public ResponseEntity<StreamingResponseBody> downloadLogFiles(@RequestBody final Map<String, String> parameters) throws IOException {
        final String  pathSpec          = parameters.get("path");
        final String  logFileSpec       = parameters.get("logFileSpec");
        final boolean includeEmptyFiles = BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBooleanObject(parameters.get("includeEmptyFiles")), false);

        final Path                       path        = StringUtils.isBlank(pathSpec) ? _xnatHome.resolve("logs") : Paths.get(pathSpec);
        final FileVisitorPathResourceMap resourceMap = StringUtils.isBlank(logFileSpec) ? new FileVisitorPathResourceMap(path) : (new FileVisitorPathResourceMap(path, logFileSpec));
        if (includeEmptyFiles) {
            resourceMap.setIncludeEmptyFiles(true);
        }
        resourceMap.process();

        log.debug("Processed resourceId map for requested log file download, found {} files", resourceMap.getFileCount());
        return ResponseEntity.ok()
                             .header(CONTENT_TYPE, MEDIA_TYPE)
                             .header(CONTENT_DISPOSITION, getAttachmentDisposition("xnat-logs-", Long.toString(new Date().getTime()), "zip"))
                             .body((StreamingResponseBody) new AbstractZipStreamingResponseBody() {
                                 @Override
                                 protected PathResourceMap<String, Resource> getResourceMap() {
                                     return resourceMap;
                                 }
                             });
    }

    private final LoggingService _logging;
    private final Path           _xnatHome;
}
