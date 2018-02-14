package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.framework.services.NrgEventService;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@EnableScheduling
@XnatEventServiceEvent(name = "TimerEvent")
public class TimerEvent extends CombinedEventServiceEvent<TimerEvent, Date>{
    final String displayName = "Timer Event";
    final String description = "Triggers every five seconds";

    @Autowired
    NrgEventService es;

    public TimerEvent(){ };

    public TimerEvent(final Date payload, final String eventUser) {
        super(payload, eventUser);
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getPayloadXnatType() {
        return null;
    }

    @Override
    public Boolean isPayloadXsiType() {
        return false;
    }

    @Scheduled(cron = "*/5 * * * * *")
    public void everyFiveSeconds()
    {
        es.triggerEvent("EveryFiveSeconds", new TimerEvent(new Date(), null));
    }

    @Override
    public EventServiceListener getInstance() {
        return new TimerEvent();
    }
}
