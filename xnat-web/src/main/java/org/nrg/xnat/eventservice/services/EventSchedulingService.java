package org.nrg.xnat.eventservice.services;

import org.nrg.xnat.eventservice.model.Subscription;

public interface EventSchedulingService {
    void scheduleEvent(Long id, String trigger, Runnable task);
    void cancelScheduledEvent(Long id);
}
