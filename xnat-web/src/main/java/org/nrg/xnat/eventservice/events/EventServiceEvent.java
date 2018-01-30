package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.EventI;

import java.util.Date;
import java.util.UUID;

public interface EventServiceEvent<ObjectT> extends EventI {

    String getId();
    String getDisplayName();
    String getDescription();
    ObjectT getObject();
    String getObjectClass();
    String getPayloadXnatType();
    Boolean isPayloadXsiType();
    String getUser();
    Date getEventTimestamp();
    UUID getEventUUID();
}
