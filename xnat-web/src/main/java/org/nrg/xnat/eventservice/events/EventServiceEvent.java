package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.EventI;

import java.util.Date;
import java.util.UUID;

public interface EventServiceEvent<ObjectT> extends EventI {

    // Get descriptive unique identifier for event
    String getId();

    String getDisplayName();
    String getDescription();
     // Get payload object
    ObjectT getObject();
     // Get class type of payload object
    Class getObjectClass();
     // Get string describing XNAT data type, e.g. xnat:imageSessionData
    String getPayloadXnatType();
     // Is the value returned by getPayloadXnatType() a known xsi type?
    Boolean isPayloadXsiType();
     // Get the user triggering the event
    String getUser();
     // Get the time of event triggering
    Date getEventTimestamp();
     // Get the event object UUID (generated at instantiation)
    UUID getEventUUID();
}
