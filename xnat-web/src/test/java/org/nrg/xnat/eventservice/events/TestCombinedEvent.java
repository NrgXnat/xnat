package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.springframework.stereotype.Service;

@Service
@XnatEventServiceEvent(name="TestCombinedEvent")
public class TestCombinedEvent extends CombinedEventServiceEvent<TestCombinedEvent, XnatImagesessiondataI>  {
    final String displayName = "Test Combined Event";
    final String description ="Combined Event tested.";

    public TestCombinedEvent(){};

    public TestCombinedEvent(final XnatImagesessiondataI payload, final String eventUser) {
        super(payload, eventUser);
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
    public EventServiceListener getInstance() {
        return new TestCombinedEvent();
    }

}
