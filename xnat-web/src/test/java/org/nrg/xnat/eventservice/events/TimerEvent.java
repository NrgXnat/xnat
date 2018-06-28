package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.nrg.xnat.eventservice.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.EnumSet;

@Service
@EnableScheduling
@XnatEventServiceEvent(name = "TimerEvent")
public class TimerEvent extends CombinedEventServiceEvent<TimerEvent, Date>{
    final String displayName = "Timer Event";
    final String description = "Triggers every five seconds";

    public enum Status {PER_MINUTE, PER_HOUR, PER_DAY};

    @Autowired
    EventService eventService;

    public TimerEvent(){ };

    public TimerEvent(final Date payload, final String eventUser, final Status status, final String projectId) {
        super(payload, eventUser, status, projectId);
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

    @Override
    public EnumSet getStatiStates() {
        return EnumSet.allOf(Status.class);
    }

    @Scheduled(cron = "*/60 * * * * *")
    public void everyMinute()
    {
        eventService.triggerEvent(new TimerEvent(new Date(), null, Status.PER_MINUTE, null));
    }

    @Scheduled(cron = "*/3600 * * * * *")
    public void everyHour()
    {
        eventService.triggerEvent(new TimerEvent(new Date(), null, Status.PER_HOUR, null));
    }

    @Scheduled(cron = "*/86400 * * * * *")
    public void everyDay()
    {
        eventService.triggerEvent(new TimerEvent(new Date(), null, Status.PER_DAY, null));
    }

    @Override
    public EventServiceListener getInstance() {
        return new TimerEvent();
    }
}
