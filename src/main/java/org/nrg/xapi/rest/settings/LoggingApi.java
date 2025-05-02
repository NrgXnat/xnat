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
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.utilities.SanitizeUtils;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.services.archive.FileVisitorPathResourceMap;
import org.nrg.xnat.services.archive.PathResourceMap;
import org.nrg.xnat.services.logging.LoggingService;
import org.nrg.xnat.services.logging.impl.AppenderHierarchy;
import org.nrg.xnat.web.http.AbstractZipStreamingResponseBody;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

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
    private static final Set<String> VALID_LEVELS = Stream.of("ALL", "DEBUG", "ERROR", "FATAL", "INFO", "OFF", "TRACE", "WARN").collect(java.util.stream.Collectors.toSet());

    @Autowired
    public LoggingApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder, final LoggingService logging, final Path xnatHome) {
        super(userManagementService, roleHolder);
        _logging  = logging;
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

    @ApiOperation(value = "Gets the location of the default folder for logs on the system.", response = Path.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT log folder successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "home", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public Path getLogsFolder() {
        return _logging.getLogsFolder();
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

    @ApiOperation(value = "Gets a list of the logger and appender elements defined in XNAT's logging configurations.", responseContainer = "Map", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT logger and appender elements successfully returned."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "elements", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public Map<String, Set<String>> getPrimaryElements() {
        return _logging.getPrimaryElements();
    }

    @ApiOperation(value = "Gets a map of the appenders defined in XNAT's logging configurations, along with the loggers for each appender and the current log level for that logger.", responseContainer = "List", response = AppenderHierarchy.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT appenders, loggers, and levels successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "appenders", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public List<AppenderHierarchy> getAppenders() {
        return _logging.getAppenders();
    }

    @ApiOperation(value = "Gets the specified appender along with the associated loggers and the current log level for each logger.", response = AppenderHierarchy.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT appender, loggers, and levels successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "appenders/{appenderName}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public AppenderHierarchy getAppender(final @PathVariable String appenderName) throws NotFoundException {
        return _logging.getAppender(appenderName);
    }

    @ApiOperation(value = "Gets a map of the loggers defined in the primary logging configuration file in XNAT itself, along with the current log level for that logger.", responseContainer = "Map", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT loggers and levels successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "levels", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public Map<String, Level> getLoggerLevels() {
        return _logging.getLoggerLevels();
    }

    @ApiOperation(value = "Gets the current logging level for the logger with the specified name.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT logger logging level successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 404, message = "No logger with the specified name was found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "levels/{logger}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public String getLoggerLevel(final @PathVariable String logger) throws NotFoundException {
        return _logging.getLoggerLevel(logger).toString();
    }

    @ApiOperation(value = "Sets the logging level for the logger with the specified name.", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT logging level successfully set."),
                   @ApiResponse(code = 400, message = "Invalid value specified for logging level."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 404, message = "No logger with the specified name was found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "loggers/{logger}/{level}", produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = Admin)
    public String setLoggerLevel(final @PathVariable String logger, final @PathVariable String level) throws NotFoundException, DataFormatException {
        final String capsLevel = StringUtils.upperCase(level);
        if (!VALID_LEVELS.contains(capsLevel)) {
            throw new DataFormatException("Invalid logging level: " + level + ". Valid levels are: " + String.join(", ", VALID_LEVELS));
        }
        return _logging.setLoggerLevel(logger, Level.valueOf(capsLevel)).toString();
    }

    @ApiOperation(value = "Gets a list of the loggers defined in the primary logging configuration file in XNAT itself, along with the location of the logger's output file.",
                  notes = "Note that not all loggers necessarily have a log file, e.g. a logger that only routes to a console appender won't have a log file. Those loggers will " +
                          "be omitted from this map. Additionally, some loggers may have multiple appenders, each with its own log file. This method returns the path to the log " +
                          "file for the first file-based appender in each logger's list of appenders.",
                  responseContainer = "Map", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT loggers and paths successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "paths", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public Map<String, Path> getLoggerPaths() {
        return _logging.getLoggerPaths();
    }

    @ApiOperation(value = "Gets the location of the log file for the logger with the specified name.",
                  notes = "Note that not all loggers necessarily have a log file, e.g. a logger that only routes to a console appender won't have a log file. This endpoint will return " +
                          "null for those loggers. Additionally, some loggers may have multiple appenders, each with its own log file. This method returns the path to the log file for " +
                          "the first file-based appender in each logger's list of appenders.",
                  response = Path.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT logger logging path successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 404, message = "No logger with the specified name was found."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "paths/{logger}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Admin)
    public Path getLoggerPath(final @PathVariable String logger) throws NotFoundException {
        return _logging.getLoggerPath(logger);
    }

    @ApiOperation(value = "Downloads the XNAT log files as a zip archive.", response = StreamingResponseBody.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT logs successfully downloaded."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "The user is not authorized to access one or more of the specified resources."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "download", produces = MEDIA_TYPE, method = GET, restrictTo = Admin)
    public ResponseEntity<StreamingResponseBody> downloadLogFiles() throws IOException {
        return downloadLogFiles(true, Collections.emptyMap());
    }

    @ApiOperation(value = "Downloads the XNAT log files as a zip archive.", response = StreamingResponseBody.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT logs successfully downloaded."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "The user is not authorized to access one or more of the specified resources."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "download/{logFileSpec}", produces = MEDIA_TYPE, method = GET, restrictTo = Admin)
    public ResponseEntity<StreamingResponseBody> downloadLogFiles(final @RequestParam(required = false, defaultValue = "false") boolean asZip, @PathVariable final String logFileSpec) throws IOException {
        return downloadLogFiles(asZip, ImmutableMap.of("logFileSpec", logFileSpec));
    }

    @ApiOperation(value = "Downloads the XNAT log files as a zip archive.",
                  notes = "This call takes a string map as JSON. PUT and POST are the same operation. Acceptable values in the map include: \"logFileSpec\" is a glob-style  wild card, e.g. '*.log', " +
                          "'application.*', etc. This defaults to '*'. \"path\" specifies the path to the folder containing the log files you want to access. The default value is the logs folder in " +
                          "your XNAT home directory, but you can specify other paths to which the XNAT application server user has access, e.g. \"/var/log/tomcat\" to retrieve the Tomcat logs. " +
                          "The \"asZip\" parameter indicates whether the log file(s) should be downloaded as a zip. By default, a single file is downloaded as plain text unless asZip is explicitly " +
                          "set to true, while multiple files are always downloaded as a zip. Finally \"includeEmptyFiles\" indicates whether empty files should be included. By default only files " +
                          "that contain data are included.",
                  response = StreamingResponseBody.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT logs successfully downloaded."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "The user is not authorized to access one or more of the specified resources."),
                   @ApiResponse(code = 500, message = "An unexpected error occurred.")})
    @XapiRequestMapping(value = "download", restrictTo = Admin, method = {POST, PUT}, consumes = APPLICATION_JSON_VALUE, produces = MEDIA_TYPE)
    public ResponseEntity<StreamingResponseBody> downloadLogFiles(final @ApiParam(name = "asZip", defaultValue = "false") @RequestParam(required = false, defaultValue = "false") boolean asZip, @RequestBody final Map<String, String> parameters) throws IOException {
        final String pathSpec    = parameters.get("path");
        final String logFileSpec = parameters.get("logFileSpec");
        if (SanitizeUtils.containsPathTraversal(pathSpec) || SanitizeUtils.containsPathTraversal(logFileSpec)) {
            throw new IllegalArgumentException("Invalid path or log file specification");
        }
        final boolean includeEmptyFiles = BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBooleanObject(parameters.get("includeEmptyFiles")), false);

        final FileVisitorPathResourceMap resourceMap = getFileVisitorPathResourceMap(pathSpec, logFileSpec);
        if (includeEmptyFiles) {
            resourceMap.setIncludeEmptyFiles(true);
        }
        resourceMap.process();
        if (resourceMap.getFileCount() == 0) {
            return ResponseEntity.notFound().build();
        }

        log.debug("Processed resourceId map for requested log file download, found {} files", resourceMap.getFileCount());
        final String                mediaType;
        final String                attachmentDisposition;
        final StreamingResponseBody response;
        if (asZip || resourceMap.getFileCount() > 1) {
            if (!asZip) {
                log.info("Got a request for a non-zip download of {} files, but can't do that so zipping them up", resourceMap.getFileCount());
            }
            mediaType             = MEDIA_TYPE;
            attachmentDisposition = getAttachmentDisposition("xnat-logs-", Long.toString(new Date().getTime()), "zip");
            response              = new AbstractZipStreamingResponseBody() {
                @Override
                protected PathResourceMap<String, Resource> getResourceMap() {
                    return resourceMap;
                }
            };
        } else {
            final PathResourceMap.Mapping<String, Resource> next = resourceMap.next();
            mediaType             = TEXT_PLAIN_VALUE;
            attachmentDisposition = getAttachmentDisposition(StringUtils.removeEnd(next.getPath(), ".log"), "log");
            response              = output -> Files.copy(next.getResource().getFile().toPath(), output);
        }

        return ResponseEntity.ok()
                             .header(CONTENT_TYPE, mediaType)
                             .header(CONTENT_DISPOSITION, attachmentDisposition)
                             .body(response);
    }

    private FileVisitorPathResourceMap getFileVisitorPathResourceMap(String pathSpec, String logFileSpec) {
        final Path path = StringUtils.isBlank(pathSpec) ? _xnatHome.resolve("logs") : Paths.get(pathSpec);
        return StringUtils.isBlank(logFileSpec) ? new FileVisitorPathResourceMap(path) : (new FileVisitorPathResourceMap(path, logFileSpec));
    }

    private final LoggingService _logging;
    private final Path           _xnatHome;
}
