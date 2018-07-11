package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@XnatEventServiceEvent(name="TestCombinedEvent")
public class TestCombinedEvent extends CombinedEventServiceEvent<TestCombinedEvent, XnatImagesessiondataI>  {
    final String displayName = "Test Combined Event";
    final String description ="Combined Event tested.";

    public enum Status {CREATED, UPDATED, DELETED};

    public TestCombinedEvent(){};

    public TestCombinedEvent(final XnatImagesessiondataI payload, final String eventUser, final Status status, final String projectId) {
        super(payload, eventUser, status, projectId);
    }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getDescription() { return description; }

    @Override
    public String getPayloadXnatType() {
        return "xnat:scan";
    }

    @Override
    public Boolean isPayloadXsiType() {
        return true;
    }

    @Override
    public List<String> getStatiStates() { return Arrays.stream(Status.values()).map(Status::name).collect(Collectors.toList()); }

    @Override
    public EventServiceListener getInstance() {
        return new TestCombinedEvent();
    }

}
