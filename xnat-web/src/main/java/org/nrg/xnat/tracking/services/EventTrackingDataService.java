/*
 * web: org.nrg.xnat.tracking.services.EventTrackingDataService
 * XNAT http://www.xnat.org
 * Copyright (c) 2020, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.tracking.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.preferences.AsyncOperationsPreferences;
import org.nrg.xnat.tracking.entities.EventTrackingDataPojo;
import org.nrg.xnat.tracking.model.TrackableEvent;

/**
 * Provides information about the current host.
 */
public interface EventTrackingDataService {
    /**
     * Get EventListenerData payload by key
     * @param key the key
     * @param user the user
     * @return the payload
     * @throws NotFoundException if no event listener data exists for this key or user cannot access it
     */
    String getPayloadByKey(final String key, UserI user) throws NotFoundException, JsonProcessingException;

    /**
     * Get EventListenerData entity by key
     * @param key the key
     * @param user the user
     * @return the pojo
     * @throws NotFoundException if no event listener data exists for this key or user cannot access it
     */
    EventTrackingDataPojo getPojoByKey(final String key, UserI user) throws NotFoundException, JsonProcessingException;

    /**
     * Create new EventListenerData entity with key, if entity already exists with key, restart its tracking
     * @param key the key
     * @param user the user
     * @throws IllegalAccessException if user cannot read this event tracking data
     */
    void createOrRestartWithKey(String key, UserI user) throws IllegalAccessException;

    /**
     * Create or update eventTrackingData with TrackableEvent.
     * <strong>Not safe across JVMs</strong>
     *
     * @param eventData the trackable event
     * @throws IllegalAccessException if user cannot read this event tracking data
     */
    void createOrUpdate(TrackableEvent eventData) throws IllegalAccessException;

    /**
     * Records the size of a bulk launch. Its progress is built from its workflows each time it's read, and with its
     * size it counts the batch's targets, not the workflows saved so far, so it isn't complete until each target has
     * run all its steps. Call it once the batch's targets are queued, before answering the launch request.
     *
     * @param key     the bulk launch's tracking key
     * @param user    the user who launched the batch
     * @param targets the number of targets queued
     * @param steps   the number of steps each target runs: 1, or the number of commands in an orchestration
     * @throws IllegalAccessException if user cannot read this event tracking data
     */
    void recordBulkLaunch(String key, UserI user, int targets, int steps) throws IllegalAccessException;

    /**
     * Remove entries older than the number of days configured in {@link AsyncOperationsPreferences#getEventTrackingDataCleanupTtl()}.
     */
    void cleanupOldEntries();
}
