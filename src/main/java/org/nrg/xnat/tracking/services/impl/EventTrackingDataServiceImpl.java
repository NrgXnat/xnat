/*
 * web: org.nrg.xnat.tracking.services.impl.EventTrackingDataServiceImpl
 * XNAT http://www.xnat.org
 * Copyright (c) 2020, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.tracking.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.Striped;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xft.XFTItem;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.event.model.BulkLaunchEvent;
import org.nrg.xnat.event.model.BulkLaunchLog;
import org.nrg.xnat.preferences.AsyncOperationsPreferences;
import org.nrg.xnat.tracking.entities.EventTrackingDataPojo;
import org.nrg.xnat.tracking.model.TrackableEvent;
import org.nrg.xnat.tracking.services.EventTrackingDataHibernateService;
import org.nrg.xnat.tracking.services.EventTrackingDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;

import static org.nrg.xnat.tracking.utils.TrackingUtils.BULK_TRACKING_KEY_PREFIX;

@Slf4j
@Service
public class EventTrackingDataServiceImpl implements EventTrackingDataService {
    private static final int           NUM_LOCKS = 256;
    private static final Striped<Lock> LOCKS     = Striped.lazyWeakLock(NUM_LOCKS);

    private final EventTrackingDataHibernateService eventTrackingDataHibernateService;
    private final AsyncOperationsPreferences asyncOperationsPreferences;
    private final ObjectMapper objectMapper;


    @Autowired
    public EventTrackingDataServiceImpl(final EventTrackingDataHibernateService eventTrackingDataHibernateService,
                                        final AsyncOperationsPreferences asyncOperationsPreferences,
                                        final NamedParameterJdbcTemplate template,
                                        final ObjectMapper objectMapper) {
        this.eventTrackingDataHibernateService = eventTrackingDataHibernateService;
        this.asyncOperationsPreferences = asyncOperationsPreferences;
        this.objectMapper = objectMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPayloadByKey(final String key, UserI user) throws NotFoundException, JsonProcessingException {
        return getPojoByKey(key, user).getPayload();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventTrackingDataPojo getPojoByKey(final String key, UserI user) throws NotFoundException, JsonProcessingException {
        if (key.startsWith(BULK_TRACKING_KEY_PREFIX)) {
           return buildEventTrackingDataPojo(key, user);
        }
        return eventTrackingDataHibernateService.findByKey(key, user.getID()).toPojo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createOrRestartWithKey(String key, UserI user) throws IllegalAccessException {
        eventTrackingDataHibernateService.createOrRestartWithKey(key, user.getID());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createOrUpdate(TrackableEvent eventData) throws IllegalAccessException {
        if (eventData instanceof BulkLaunchEvent) {
            return;
        }
        @SuppressWarnings("UnstableApiUsage")
        final Lock lock = LOCKS.get(eventData.getTrackingId());
        lock.lock();
        try {
            eventTrackingDataHibernateService.createOrUpdate(eventData);
        } finally {
            lock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanupOldEntries() {
        // Remove entries > asyncOperationsPreferences.getEventTrackingDataCleanupTtl() days ago
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1 * asyncOperationsPreferences.getEventTrackingDataCleanupTtl());
        eventTrackingDataHibernateService.deleteEntriesOlderThan(cal.getTime());
    }

    private EventTrackingDataPojo buildEventTrackingDataPojo(String key, UserI user) throws JsonProcessingException {
        BulkLaunchLog bulkLaunchLog = convertWorkflowsToBulkLaunchLog(key, user);
        EventTrackingDataPojo eventTrackingDataPojo = new EventTrackingDataPojo();
        eventTrackingDataPojo.setKey(key);
        eventTrackingDataPojo.setPayload(objectMapper.writeValueAsString(bulkLaunchLog));
        if (bulkLaunchLog.bulkLaunchComplete()) {
            eventTrackingDataPojo.setSucceeded(bulkLaunchLog.bulkLaunchSuccess());
        }
        eventTrackingDataPojo.setFinalMessage(bulkLaunchLog.bulkLaunchMessage());
        return eventTrackingDataPojo;
    }

    private BulkLaunchLog convertWorkflowsToBulkLaunchLog(final String key, final UserI user) {
        CriteriaCollection cc = new CriteriaCollection("AND");
        BulkLaunchLog bulkLaunchLog = new BulkLaunchLog();
        cc.addClause("wrk:workflowData.jobid", key);
        try {
            ItemCollection items = ItemSearch.GetItems(cc, user, false);
            ArrayList workitems = items.getItems("wrk:workflowData.launch_time", "DESC");
            Iterator iter = workitems.iterator();
            int totalWorkflows = 0;
            while(iter.hasNext()) {
                PersistentWorkflowI wrkFlow = new WrkWorkflowdata((XFTItem)iter.next());
                bulkLaunchLog.addOrUpdateWorkflow(wrkFlow);
                ++totalWorkflows;
            }
            bulkLaunchLog.setTotal(totalWorkflows);
        } catch (Exception e) {
            log.error("Querying for workflows by jobid encountered error", e);
        }
        return bulkLaunchLog;
    }



    private static final String BULK_JOB_ID = "BULK_JOB_ID";
    private static final String BULK_WORKFLOWS_QUERY = "select * from wrk_workflowdata where jobid=:" + BULK_JOB_ID;
}


