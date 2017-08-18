/*
 * web: org.nrg.xapi.rest.XapiRestControllerAdvice
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.dcm.exceptions.DICOMReceiverWithDuplicateAeTitleException;
import org.nrg.dcm.exceptions.EnabledDICOMReceiverWithDuplicatePortException;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xapi.exceptions.*;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice(annotations = XapiRestController.class)
public class XapiRestControllerAdvice {
    @Autowired
    public XapiRestControllerAdvice(final SiteConfigPreferences preferences) {
        _preferences = preferences;
        _headers.put(NotAuthenticatedException.class, new HttpHeaders() {{
            add("WWW-Authenticate", "Basic realm=\"" + _preferences.getSiteId() + "\"");
        }});
    }

    @ExceptionHandler(EnabledDICOMReceiverWithDuplicatePortException.class)
    public ResponseEntity<?> handleEnabledDICOMReceiverWithDuplicatePort(final HttpServletRequest request, final HttpServletResponse response, final EnabledDICOMReceiverWithDuplicatePortException exception) {
        return getExceptionResponseEntity(request, exception);
    }

    @ExceptionHandler(DICOMReceiverWithDuplicateAeTitleException.class)
    public ResponseEntity<?> handleDICOMReceiverWithDuplicateAeTitle(final HttpServletRequest request, final HttpServletResponse response, final DICOMReceiverWithDuplicateAeTitleException exception) {
        return getExceptionResponseEntity(request, exception);
    }

    @ExceptionHandler(DataFormatException.class)
    public ResponseEntity<?> handleDataFormatException(final HttpServletRequest request, final HttpServletResponse response, final DataFormatException exception) {
        return getExceptionResponseEntity(request, exception);
    }

    @ExceptionHandler(InsufficientPrivilegesException.class)
    public ResponseEntity<?> handleInsufficientPrivilegesException(final HttpServletRequest request, final HttpServletResponse response, final InsufficientPrivilegesException exception) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NotAuthenticatedException.class)
    public ResponseEntity<?> handleNotAuthenticatedException(final HttpServletRequest request, final HttpServletResponse response, final NotAuthenticatedException exception) {
        return getExceptionResponseEntity(request, HttpStatus.UNAUTHORIZED, _headers.get(NotAuthenticatedException.class));
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<?> handleDataFormatException(final HttpServletRequest request, final HttpServletResponse response, final ResourceAlreadyExistsException exception) {
        return getExceptionResponseEntity(request, exception);
    }

    @ExceptionHandler(NrgServiceException.class)
    public ResponseEntity<?> handleNrgServiceException(final HttpServletRequest request, final HttpServletResponse response, final NrgServiceException exception) {
        return getExceptionResponseEntity(request, HttpStatus.INTERNAL_SERVER_ERROR, exception, "An NRG service error occurred.");
    }

    @ExceptionHandler(URISyntaxException.class)
    public ResponseEntity<?> handleUriSyntaxException(final HttpServletRequest request, final HttpServletResponse response, final URISyntaxException exception) {
        final String message = "An error occurred at index " + exception.getIndex() + " when processing the URI " + exception.getInput() + ": " + exception.getMessage();
        return getExceptionResponseEntity(request, HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<?> handleFileNotFoundException(final HttpServletRequest request, final HttpServletResponse response, final FileNotFoundException exception) {
        return getExceptionResponseEntity(request, HttpStatus.NOT_FOUND, exception, "Unable to find requested file");
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(final HttpServletRequest request, final HttpServletResponse response, final NotFoundException exception) {
        return getExceptionResponseEntity(request, HttpStatus.NOT_FOUND, exception, "Unable to find requested file or resource");
    }

    @ExceptionHandler(ConfigServiceException.class)
    public ResponseEntity<?> handleConfigServiceException(final HttpServletRequest request, final HttpServletResponse response, final ConfigServiceException exception) {
        return getExceptionResponseEntity(request, HttpStatus.INTERNAL_SERVER_ERROR, exception, "An error occurred when accessing the configuration service: " + exception.getMessage());
    }

    @ExceptionHandler(ServerException.class)
    public ResponseEntity<?> handleServerException(final HttpServletRequest request, final HttpServletResponse response, final ConfigServiceException exception) {
        return getExceptionResponseEntity(request, HttpStatus.INTERNAL_SERVER_ERROR, exception, "An error occurred on the server during the request: " + exception.getMessage());
    }

    @ExceptionHandler(ClientException.class)
    public ResponseEntity<?> handleClientException(final HttpServletRequest request, final HttpServletResponse response, final ConfigServiceException exception) {
        return getExceptionResponseEntity(request, HttpStatus.BAD_REQUEST, "There was an error in the request: " + exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(final HttpServletRequest request, final HttpServletResponse response, final Exception exception) {
        return getExceptionResponseEntity(request, HttpStatus.INTERNAL_SERVER_ERROR, exception, "There was an error in the request: " + exception.getMessage());
    }

    @NotNull
    private ResponseEntity<?> getExceptionResponseEntity(final HttpServletRequest request, final Exception exception) {
        return getExceptionResponseEntity(request, null, exception, null, null);
    }

    @NotNull
    private ResponseEntity<?> getExceptionResponseEntity(final HttpServletRequest request, final HttpStatus status, final String message) {
        return getExceptionResponseEntity(request, status, null, message, null);
    }

    @NotNull
    private ResponseEntity<?> getExceptionResponseEntity(final HttpServletRequest request, final HttpStatus status, final HttpHeaders headers) {
        return getExceptionResponseEntity(request, status, null, null, headers);
    }

    @NotNull
    private ResponseEntity<?> getExceptionResponseEntity(final HttpServletRequest request, final HttpStatus status, final Exception exception, final String message) {
        return getExceptionResponseEntity(request, status, exception, message, null);
    }

    @NotNull
    private ResponseEntity<?> getExceptionResponseEntity(@Nonnull final HttpServletRequest request, final HttpStatus status, final Exception exception, final String message, final HttpHeaders headers) {
        final String resolvedMessage;
        if (message == null && exception == null) {
            resolvedMessage = null;
        } else if (message == null) {
            resolvedMessage = exception.getMessage();
        } else if (exception == null) {
            resolvedMessage = message;
        } else {
            resolvedMessage = message + ": " + exception.getMessage();
        }

        // If there's an explicit status, use that. Otherwise try to get it off of the exception and default to 500 if not available.
        final HttpStatus resolvedStatus = status != null ? status : getExceptionResponseStatus(exception, HttpStatus.INTERNAL_SERVER_ERROR);

        // Log 500s as errors, other statuses can just be logged as info messages.
        if (resolvedStatus == HttpStatus.INTERNAL_SERVER_ERROR) {
            _log.error("HTTP status 500: Request by user " + XDAT.getUserDetails().getUsername() + " to URL " + request.getServletPath() + request.getPathInfo() + " caused an internal server error", exception);
        } else if (_log.isInfoEnabled() && exception != null) {
            _log.info("HTTP status {}: Request by user {} to URL {} caused an exception of type {}{}",
                      resolvedStatus,
                      XDAT.getUserDetails().getUsername(),
                      request.getServletPath() + request.getPathInfo(),
                      exception.getClass().getName(),
                      StringUtils.defaultIfBlank(resolvedMessage, ""));
        }

        if (headers == null && resolvedMessage == null) {
            return new ResponseEntity<>(resolvedStatus);
        } else if (headers == null) {
            return new ResponseEntity<>(resolvedMessage, resolvedStatus);
        } else {
            return new ResponseEntity<>(resolvedMessage, headers, resolvedStatus);
        }
    }

    private HttpStatus getExceptionResponseStatus(final Exception exception, final HttpStatus defaultStatus) {
        final ResponseStatus annotation = AnnotationUtils.findAnnotation(exception.getClass(), ResponseStatus.class);
        return annotation != null ? annotation.value() : defaultStatus;
    }

    private static final Logger _log = LoggerFactory.getLogger(XapiRestControllerAdvice.class);

    private final Map<Class<? extends Exception>, HttpHeaders> _headers = new HashMap<>();
    private final SiteConfigPreferences _preferences;
}
