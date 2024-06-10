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
    final private Map<String, ScheduledFuture> tasks = new HashMap<>();

    public EventSchedulingServiceImpl(final ThreadPoolTaskScheduler scheduler){
        this.scheduler = scheduler;
    }

    public void scheduleEvent(Runnable task, String trigger){
        if(null == task || !StringUtils.hasLength(trigger)){
            throw new IllegalArgumentException("Trigger and task must not be null.");
        }

        if(!tasks.containsKey(trigger)){
            ScheduledFuture<?> future = scheduler.schedule(task, new CronTrigger(trigger));
            tasks.put(trigger, future);
        }
    }

    public void cancelScheduledEvent(String trigger){
        if(tasks.containsKey(trigger)) {
            tasks.remove(trigger).cancel(false);
        }
    }
}
