package org.nrg.xnat.tracking.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.nrg.xnat.tracking.model.TrackableEvent;
import org.nrg.xnat.tracking.entities.EventTrackingData;
import org.nrg.xnat.tracking.services.EventTrackingDataService;
import org.nrg.xnat.tracking.TrackEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.aspectj.lang.annotation.Aspect;
import reactor.bus.Event;

import java.io.IOException;

@Slf4j
@Aspect
@Component
public class EventTrackingAspect {
    @Autowired
    public EventTrackingAspect(final EventTrackingDataService eventTrackingDataService) {
        this.eventTrackingDataService = eventTrackingDataService;
    }

    @Pointcut("@annotation(trackEvent)")
    public void trackingPointcut(final TrackEvent trackEvent) {
    }

    @Before(value = "trackingPointcut(trackEvent) && args(event)", argNames = "trackEvent, event")
    public <T extends TrackableEvent> void updateEventTracker(final TrackEvent trackEvent, final Event<T> event) {
        eventTrackingDataService.createOrUpdate(event.getData());
    }

    private final EventTrackingDataService eventTrackingDataService;
}
