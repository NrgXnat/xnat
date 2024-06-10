package org.nrg.xnat.eventservice.services;

import org.nrg.xnat.eventservice.model.Subscription;

public interface EventSchedulingService {
    void scheduleEvent(Runnable task, String cronTrigger);
    void cancelScheduledEvent(String cronTrigger);
}
