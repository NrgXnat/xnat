package org.nrg.xnat.eventservice.services.impl;

import org.nrg.xnat.eventservice.services.EventSchedulingService;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Service
public class EventSchedulingServiceImpl implements EventSchedulingService {

    final private ThreadPoolTaskScheduler scheduler;
    final private Map<Long, ScheduledFuture> tasks = new HashMap<>();

    public EventSchedulingServiceImpl(final ThreadPoolTaskScheduler scheduler){
        this.scheduler = scheduler;
    }

    public void scheduleEvent(Long id, String trigger, Runnable task){
        if(null == task || null == id || !StringUtils.hasLength(trigger)){
            throw new IllegalArgumentException("Trigger, task, and id must not be null.");
        }

        // Remove anything that was added previously
        cancelScheduledEvent(id);

        ScheduledFuture<?> future = scheduler.schedule(task, new CronTrigger(trigger));
        tasks.put(id, future);
    }

    public void cancelScheduledEvent(Long id){
        if(tasks.containsKey(id)) {
            tasks.remove(id).cancel(false);
        }
    }
}
