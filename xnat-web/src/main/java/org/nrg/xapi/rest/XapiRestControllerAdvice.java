/*
 * web: org.nrg.xapi.rest.XapiRestControllerAdvice
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.rest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.dcm.scp.exceptions.DICOMReceiverWithDuplicateTitleAndPortException;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xapi.exceptions.*;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.exception.XftItemException;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice(annotations = XapiRestController.class)
@Slf4j
public class XapiRestControllerAdvice {

    @Autowired
    public XapiRestControllerAdvice(final SiteConfigPreferences preferences) {
        _preferences = preferences;
        _headers.put(NotAuthenticatedException.class, new HttpHeaders() {{
            add("WWW-Authenticate", "Basic realm=\"" + _preferences.getSiteId() + "\"");
        }});
    }

    @ExceptionHandler(DICOMReceiverWithDuplicateTitleAndPortException.class)
    public ResponseEntity<?> handleEnabledDICOMReceiverWithDuplicatePort(final HttpServletRequest request, final DICOMReceiverWithDuplicateTitleAndPortException exception) {
        return getExceptionResponseEntity(request, exception);
    }

    @ExceptionHandler(DataFormatException.class)
    public ResponseEntity<?> handleDataFormatException(final HttpServletRequest request, final DataFormatException exception) {
        return getExceptionResponseEntity(request, exception);
    }

    @ExceptionHandler(InsufficientPrivilegesException.class)
    public ResponseEntity<?> handleInsufficientPrivilegesException() {
        return new ResponseEntity<>(FORBIDDEN);
    }

    @ExceptionHandler(NotAuthenticatedException.class)
    public ResponseEntity<?> handleNotAuthenticatedException(final HttpServletRequest request) {
        return getExceptionResponseEntity(request, UNAUTHORIZED, _headers.get(NotAuthenticatedException.class));
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<?> handleDataFormatException(final HttpServletRequest request, final ResourceAlreadyExistsException exception) {
        return getExceptionResponseEntity(request, exception);
    }

    @ExceptionHandler(XFTInitException.class)
    public ResponseEntity<?> handleXFTInitException(final HttpServletRequest request, final XFTInitException exception) {
        return getExceptionResponseEntity(request, INTERNAL_SERVER_ERROR, exception, "An XFT init exception occurred.");
    }

    @ExceptionHandler(XftItemException.class)
    public ResponseEntity<?> handleXftItemException(final HttpServletRequest request, final XftItemException exception) {
        return getExceptionResponseEntity(request, INTERNAL_SERVER_ERROR, exception, "An XFT item exception occurred.");
    }

    @ExceptionHandler(NrgServiceException.class)
    public ResponseEntity<?> handleNrgServiceException(final HttpServletRequest request, final NrgServiceException exception) {
        return getExceptionResponseEntity(request, INTERNAL_SERVER_ERROR, exception, "An NRG service error occurred.");
    }

    @ExceptionHandler(URISyntaxException.class)
    public ResponseEntity<?> handleUriSyntaxException(final HttpServletRequest request, final URISyntaxException exception) {
        final String message = "An error occurred at index " + exception.getIndex() + " when processing the URI " + exception.getInput() + ": " + exception.getMessage();
        return getExceptionResponseEntity(request, BAD_REQUEST, message);
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<?> handleFileNotFoundException(final HttpServletRequest request, final FileNotFoundException exception) {
        return getExceptionResponseEntity(request, NOT_FOUND, exception, "Unable to find requested file");
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(final HttpServletRequest request, final NotFoundException exception) {
        return getExceptionResponseEntity(request, NOT_FOUND, exception, "Unable to find requested file or resource");
    }

    @ExceptionHandler(NoContentException.class)
    public ResponseEntity<?> handleNoContentException(final HttpServletRequest request, final NoContentException exception) {
        return getExceptionResponseEntity(request, NO_CONTENT, exception, "Unable to find requested file or resource");
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<?> handleServletRequestBindingException(final HttpServletRequest request, final ServletRequestBindingException exception) {
        return getExceptionResponseEntity(request, BAD_REQUEST, exception, "There was an error in the request: " + exception.getMessage());
    }

    @ExceptionHandler(ConfigServiceException.class)
    public ResponseEntity<?> handleConfigServiceException(final HttpServletRequest request, final ConfigServiceException exception) {
        return getExceptionResponseEntity(request, INTERNAL_SERVER_ERROR, exception, "An error occurred when accessing the configuration service: " + exception.getMessage());
    }

    @ExceptionHandler(ServerException.class)
    public ResponseEntity<?> handleServerException(final HttpServletRequest request, final ServerException exception) {
        return getExceptionResponseEntity(request, valueOf(exception.getStatus().getCode()), exception, "An error occurred on the server during the request: " + exception.getMessage());
    }

    @ExceptionHandler(ClientException.class)
    public ResponseEntity<?> handleClientException(final HttpServletRequest request, final ClientException exception) {
        return getExceptionResponseEntity(request, valueOf(exception.getStatus().getCode()), "There was an error in the request: " + exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(final HttpServletRequest request, final Exception exception) {
        return getExceptionResponseEntity(request, INTERNAL_SERVER_ERROR, exception, "There was an error in the request: " + exception.getMessage());
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
    private ResponseEntity<?> getExceptionResponseEntity(final HttpServletRequest request, @SuppressWarnings("SameParameterValue") final HttpStatus status, final HttpHeaders headers) {
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
        final HttpStatus resolvedStatus = status != null ? status : exception != null ? getExceptionResponseStatus(exception) : DEFAULT_ERROR_STATUS;

        // Log 500s as errors, other statuses can just be logged as info messages.
        final UserI  userDetails = XDAT.getUserDetails();
        final String username    = userDetails != null ? userDetails.getUsername() : "unauthenticated user";
        final String requestUri  = request.getServletPath() + request.getPathInfo();

        if (resolvedStatus == INTERNAL_SERVER_ERROR) {
            log.error("HTTP status 500: Request by user {} to URL {} caused an internal server error", username, requestUri, exception);
        } else if (exception != null) {
            log.info("HTTP status {}: Request by user {} to URL {} caused an exception of type {}{}", resolvedStatus, username, requestUri, exception.getClass().getName(), StringUtils.defaultIfBlank(resolvedMessage, ""));
        }

        return headers == null
               ? (resolvedMessage == null
                  ? new ResponseEntity<>(resolvedStatus)
                  : new ResponseEntity<>(resolvedMessage, resolvedStatus))
               : new ResponseEntity<>(resolvedMessage, headers, resolvedStatus);
    }

    private HttpStatus getExceptionResponseStatus(final Exception exception) {
        return AnnotationUtils.findAnnotation(exception.getClass(), ResponseStatus.class).value();
    }

    private static final HttpStatus DEFAULT_ERROR_STATUS = INTERNAL_SERVER_ERROR;

    private final Map<Class<? extends Exception>, HttpHeaders> _headers = new HashMap<>();
    private final SiteConfigPreferences                        _preferences;
}
