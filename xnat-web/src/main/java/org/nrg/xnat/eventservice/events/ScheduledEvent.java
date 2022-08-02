package org.nrg.xnat.eventservice.events;

import lombok.NoArgsConstructor;
import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatProjectdataI;
import org.nrg.xdat.om.base.BaseXnatProjectdata;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.model.ScheduledEventPayload;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@XnatEventServiceEvent(name="ScheduledEvent")
public class ScheduledEvent extends AbstractEventServiceEvent<Object> {

    public enum Status {CRON}
    String schedule;

    public ScheduledEvent(final String schedule) {
        super(new ScheduledEventPayload(schedule), Users.getAdminUser().getUsername(), Status.CRON);
        this.schedule = schedule;
    }

    @Override
    public String getDisplayName() {
        return "Scheduled Event";
    }

    @Override
    public String getDescription() {
        return "Scheduled Event";
    }

    @Override
    public Object getObject(UserI user) {
        return new ScheduledEventPayload(schedule);
    }

    @Override
    public Boolean isPayloadXsiType() {
        return false;
    }

    @Override
    public List<String> getStatiStates() { return Arrays.stream(Status.values()).map(Status::name).collect(Collectors.toList()); }

    @Override
    public List<EventScope> getEventScope() { return Arrays.asList(EventScope.SITE); }

    public String getSchedule() { return this.schedule; }
}
