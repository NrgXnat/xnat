/*
 * web: org.nrg.xnat.helpers.processing.handlers.AbstractProcessingOperationHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.processing.handlers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.services.messaging.processing.ProcessingOperationRequestData;

import java.util.Arrays;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Getter(PROTECTED)
@Accessors(prefix = "_")
@Slf4j
public abstract class AbstractProcessingOperationHandler implements ProcessingOperationHandler {
    protected AbstractProcessingOperationHandler(final XnatUserProvider userProvider) {
        _userProvider = userProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void execute(final ProcessingOperationRequestData request);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handles(final Class<? extends ProcessingOperationRequestData> requestType) {
        final Processes annotation = getClass().getAnnotation(Processes.class);
        if (annotation == null) {
            throw new RuntimeException("If you don't provide the @Processes annotation on your implementation of ProcessingOperationHandler, you must override the handles() method from the abstract implementation.");
        }
        return Arrays.stream(annotation.value()).anyMatch(type -> type.isAssignableFrom(requestType));
    }

    @JsonIgnore
    protected UserI getUser(final String username) {
        try {
            return StringUtils.isNotBlank(username) ? Users.getUser(username) : _userProvider.get();
        } catch (UserInitException | UserNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    protected void progress(final String processingId, final int progress) {
        log.debug("Progress updated to {} for {}", progress, processingId);
    }

    protected void progress(final String processingId, final int progress, final String message) {
        log.debug("Progress updated to {} for {}: {}", progress, processingId, message);
    }

    protected void completed(final String processingId) {
        log.debug("Completed processing on {}", processingId);
    }

    protected void completed(final String processingId, final String message) {
        log.debug("Completed processing on {}: {}", processingId, message);
    }

    protected void failed(final String processingId) {
        log.debug("Failed processing on {}", processingId);
    }

    protected void failed(final String processingId, final String message) {
        log.debug("Failed processing on {}: {}", processingId, message);
    }

    @Getter(PRIVATE)
    private final XnatUserProvider _userProvider;
}
