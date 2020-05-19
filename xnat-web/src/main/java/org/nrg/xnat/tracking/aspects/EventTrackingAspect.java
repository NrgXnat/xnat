package org.nrg.xnat.tracking.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.nrg.xnat.event.archive.ArchiveEventI;
import org.nrg.xnat.tracking.entities.EventTrackingData;
import org.nrg.xnat.tracking.entities.EventTrackingLog;
import org.nrg.xnat.tracking.services.EventTrackingDataService;
import org.nrg.xnat.tracking.services.EventTrackingLogPayloadParser;
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
    public EventTrackingAspect(final EventTrackingDataService eventTrackingDataService,
                               final EventTrackingLogPayloadParser eventTrackingLogPayloadParser) {
        this.eventTrackingDataService = eventTrackingDataService;
        this.eventTrackingLogPayloadParser = eventTrackingLogPayloadParser;
    }

    @Pointcut("@annotation(trackEvent)")
    public void trackingPointcut(final TrackEvent trackEvent) {
    }

    @Before(value = "trackingPointcut(trackEvent) && args(archiveEvent)", argNames = "trackEvent, archiveEvent")
    public void updateArchiveEventTracker(final TrackEvent trackEvent,
                                          final Event<ArchiveEventI> archiveEvent) {
        final ArchiveEventI event = archiveEvent.getData();
        String key = event.getArchiveEventId();
        if (key == null) {
            return;
        }

        synchronized (this) {
            EventTrackingData eventTrackingData = eventTrackingDataService.findOrCreateByKey(key);
            if (event.getProgress() == 100) {
                eventTrackingData.setSucceeded(event.getStatus() != ArchiveEventI.Status.Failed);
                eventTrackingData.setFinalMessage(event.getMessage());
            } else {
                try {
                    EventTrackingLog statusLog = eventTrackingLogPayloadParser.getParsedPayload(eventTrackingData);
                    if (statusLog == null) {
                        statusLog = new EventTrackingLog();
                    }
                    statusLog.addToEntryList(new EventTrackingLog.MessageEntry(event.getStatus(),
                            event.getEventTime(), event.getMessage()));
                    eventTrackingData.setPayload(eventTrackingLogPayloadParser.stringifyPayload(statusLog));
                } catch (IOException e) {
                    log.error("Unable to parse payload, not updating event listener data for {}", key, e);
                }
            }
            eventTrackingDataService.update(eventTrackingData);
        }
    }

    private final EventTrackingDataService eventTrackingDataService;
    private final EventTrackingLogPayloadParser eventTrackingLogPayloadParser;
}
