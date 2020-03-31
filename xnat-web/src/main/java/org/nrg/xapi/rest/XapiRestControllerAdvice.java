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
import org.jetbrains.annotations.NotNull;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.dcm.scp.exceptions.DICOMReceiverWithDuplicateTitleAndPortException;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xapi.XapiUtils;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.apache.commons.lang3.StringUtils.*;
import static org.springframework.http.HttpStatus.*;

@ControllerAdvice(annotations = XapiRestController.class)
@Slf4j
public class XapiRestControllerAdvice {
    @Autowired
    public XapiRestControllerAdvice(final SiteConfigPreferences preferences) {
        _realm = XapiUtils.getWwwAuthenticateBasicHeaders(preferences.getSiteId());
    }

    @ExceptionHandler(DICOMReceiverWithDuplicateTitleAndPortException.class)
    public ResponseEntity<?> handleEnabledDICOMReceiverWithDuplicatePort(final HttpServletRequest request, final DICOMReceiverWithDuplicateTitleAndPortException exception) {
        return getExceptionResponseEntity(request, BAD_REQUEST, exception, null);
    }

    @ExceptionHandler({DataFormatException.class, HttpMessageNotReadableException.class, HttpMessageConversionException.class, HttpMessageNotWritableException.class})
    public ResponseEntity<?> handleDataFormatException(final HttpServletRequest request, final Exception exception) {
        return getExceptionResponseEntity(request, BAD_REQUEST, exception, null);
    }

    @ExceptionHandler(InsufficientPrivilegesException.class)
    public ResponseEntity<?> handleInsufficientPrivilegesException() {
        return ResponseEntity.status(FORBIDDEN).build();
    }

    @ExceptionHandler(NotAuthenticatedException.class)
    public ResponseEntity<?> handleNotAuthenticatedException(final HttpServletRequest request) {
        return getExceptionResponseEntity(request, UNAUTHORIZED, null, null);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<?> handleDataFormatException(final HttpServletRequest request, final ResourceAlreadyExistsException exception) {
        return getExceptionResponseEntity(request, CONFLICT, exception, null);
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
        return getExceptionResponseEntity(request, BAD_REQUEST, null, message);
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
        return getExceptionResponseEntity(request, valueOf(exception.getStatus().getCode()), exception, "There was an error in the request: " + exception.getMessage());
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> handleIOException(final HttpServletRequest request, final IOException exception) {
        if (contains(exception.getClass().getName(), "ClientAbortException")) {
            return getExceptionResponseEntity(request, BAD_REQUEST, exception, "The client : " + exception.getMessage());
        }
        return getExceptionResponseEntity(request, INTERNAL_SERVER_ERROR, exception, "There was an error reading or writing the requested resource: " + exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(final HttpServletRequest request, final Exception exception) {
        return getExceptionResponseEntity(request, INTERNAL_SERVER_ERROR, exception, "There was an error in the request: " + exception.getMessage());
    }

    @NotNull
    private ResponseEntity<?> getExceptionResponseEntity(@Nonnull final HttpServletRequest request, final HttpStatus status, final Exception exception, final String message) {
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
            log.info("HTTP status {}: Request by user {} to URL {} caused an exception of type {}{}", resolvedStatus, username, requestUri, exception.getClass().getName(), defaultIfBlank(resolvedMessage, ""));
        }

        final ResponseEntity.BodyBuilder builder = ResponseEntity.status(resolvedStatus);
        if (status == UNAUTHORIZED) {
            builder.headers(_realm);
        }
        return isBlank(resolvedMessage) ? builder.contentLength(0).build() : builder.contentType(MediaType.TEXT_PLAIN).contentLength(resolvedMessage.length()).body(resolvedMessage);
    }

    private HttpStatus getExceptionResponseStatus(final Exception exception) {
        return AnnotationUtils.findAnnotation(exception.getClass(), ResponseStatus.class).value();
    }

    private static final HttpStatus DEFAULT_ERROR_STATUS = INTERNAL_SERVER_ERROR;

    private final HttpHeaders _realm;
}
