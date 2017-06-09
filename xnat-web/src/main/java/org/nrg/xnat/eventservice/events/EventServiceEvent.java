package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.EventI;

public interface EventServiceEvent<ObjectT> extends EventI {

    String getId();
    String getDisplayName();
    String getDescription();
    ObjectT getObject();
    String getObjectClass();
    String getPayloadXnatType();
    Boolean isPayloadXsiType();
    Integer getUser();
}
