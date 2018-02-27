package org.nrg.xnat.eventservice.services;

import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.model.JsonPathFilterNode;

import java.util.Map;

public interface EventFilterService {

    Boolean matchFilter(Object eventPayloadObject, String jsonPathFilter);

    String serializePayloadObject(Object eventPayloadObject, UserI user);

    Map<String, JsonPathFilterNode> generateEventFilterNodes(EventServiceEvent eventServiceEvent);
    Map<String, JsonPathFilterNode> generateEventFilterNodes(Class eventPayloadClass);

    String generateJsonPathFilter(Map<String, JsonPathFilterNode> filterNodeMap);

}
