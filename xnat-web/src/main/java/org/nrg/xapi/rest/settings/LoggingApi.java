/*
 * web: org.nrg.xapi.rest.settings.XnatPluginApi
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.nrg.xnat.web.http.AbstractZipStreamingResponseBody.MEDIA_TYPE;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Api(description = "XNAT Logging API")
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

    @ApiOperation(value = "Resets and reloads logging configuration from all log4j configuration files located either in XNAT itself or in plugins.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT logging configuration successfully reset."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "reset", produces = TEXT_PLAIN_VALUE, method = POST, restrictTo = Admin)
    public ResponseEntity<String> resetLoggingConfiguration() {
        final Properties properties = _logging.reset();
        try (final StringWriter writer = new StringWriter()) {
            properties.store(writer, "Generated properties for XNAT log4j configuration");
            return new ResponseEntity<>(writer.getBuffer().toString(), OK);
        } catch (IOException e) {
            log.warn("An error occurred trying to write the log4j properties", e);
            return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
        }
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
        final String                     pathSpec          = parameters.get("path");
        final String                     logFileSpec       = parameters.get("logFileSpec");
        final boolean                    includeEmptyFiles = BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBooleanObject(parameters.get("includeEmptyFiles")), false);
        final Path                       path              = StringUtils.isBlank(pathSpec) ? _xnatHome.resolve("logs") : Paths.get(pathSpec);
        final FileVisitorPathResourceMap resourceMap       = StringUtils.isBlank(logFileSpec) ? new FileVisitorPathResourceMap(path) : (new FileVisitorPathResourceMap(path, logFileSpec));
        if (includeEmptyFiles) {
            resourceMap.setIncludeEmptyFiles(true);
        }
        resourceMap.process();
        log.debug("Processed resource map for requested log file download, found {} files", resourceMap.getFileCount());
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
